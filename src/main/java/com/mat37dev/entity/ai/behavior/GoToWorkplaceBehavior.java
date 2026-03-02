package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.entity.ai.status.VillagerStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.pathfinder.Path;

import java.util.Map;
import java.util.Optional;

/**
 * Le villageois se rend à son lieu de travail ({@link MillMemories#WORK_POS}).
 *
 * <p>Navigation idempotente avec cooldown en cas d'échec de pathfinding
 * pour éviter de foncer dans les murs.</p>
 */
public class GoToWorkplaceBehavior extends Behavior<MillVillagerEntity> {

    private static final int    ARRIVAL_DIST       = 3;
    private static final double WALK_SPEED         = 0.8;
    private static final int    STUCK_TICKS        = 40;
    private static final double MIN_MOVE_DIST_SQ   = 0.5 * 0.5;
    private static final int    PATH_FAIL_COOLDOWN = 60;

    private BlockPos lastCheckPos = BlockPos.ZERO;
    private int ticksSinceLastMove = 0;
    private long lastPathFailTick = -1000L;

    public GoToWorkplaceBehavior() {
        super(Map.of(MillMemories.WORK_POS, MemoryStatus.VALUE_PRESENT), 100, 300);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        Optional<BlockPos> workPos = entity.getBrain().getMemory(MillMemories.WORK_POS);
        if (workPos.isEmpty()) return false;
        return workPos.get().distSqr(entity.blockPosition()) > (long) ARRIVAL_DIST * ARRIVAL_DIST;
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.setStatus(VillagerStatus.GOING_TO_WORK);
        lastCheckPos = entity.blockPosition();
        ticksSinceLastMove = 0;
        lastPathFailTick = -1000L;
        entity.getBrain().getMemory(MillMemories.WORK_POS).ifPresent(pos ->
                navigateToSafe(entity, pos, gameTime));
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Optional<BlockPos> workPos = entity.getBrain().getMemory(MillMemories.WORK_POS);
        if (workPos.isEmpty()) return false;
        return workPos.get().distSqr(entity.blockPosition()) > (long) ARRIVAL_DIST * ARRIVAL_DIST;
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Optional<BlockPos> workPos = entity.getBrain().getMemory(MillMemories.WORK_POS);
        if (workPos.isEmpty()) return;
        BlockPos pos = workPos.get();

        if (pos.distSqr(entity.blockPosition()) <= (long) ARRIVAL_DIST * ARRIVAL_DIST) {
            entity.getNavigation().stop();
            return;
        }

        if (!entity.getNavigation().isInProgress()) {
            if (gameTime - lastPathFailTick < PATH_FAIL_COOLDOWN) return;

            navigateToSafe(entity, pos, gameTime);
            lastCheckPos = entity.blockPosition();
            ticksSinceLastMove = 0;
            return;
        }

        ticksSinceLastMove++;
        if (ticksSinceLastMove >= STUCK_TICKS) {
            BlockPos currentPos = entity.blockPosition();
            if (currentPos.distSqr(lastCheckPos) < MIN_MOVE_DIST_SQ) {
                if (gameTime - lastPathFailTick >= PATH_FAIL_COOLDOWN) {
                    navigateToSafe(entity, pos, gameTime);
                }
            }
            lastCheckPos = currentPos;
            ticksSinceLastMove = 0;
        }
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getNavigation().stop();
        entity.setStatus(VillagerStatus.IDLE);
    }

    private void navigateToSafe(MillVillagerEntity entity, BlockPos target, long gameTime) {
        Path path = entity.getNavigation().createPath(target, 1);
        if (path != null) {
            // Utiliser le chemin même tronqué pour rapprocher le villageois
            entity.getNavigation().moveTo(path, WALK_SPEED);
            lastPathFailTick = -1000L;
        } else {
            lastPathFailTick = gameTime;
            entity.getNavigation().stop();
        }
    }
}
