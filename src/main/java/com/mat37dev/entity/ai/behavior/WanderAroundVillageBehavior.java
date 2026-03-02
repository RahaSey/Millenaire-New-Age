package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.entity.ai.status.VillagerStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * Le villageois se promène de façon aléatoire dans le village.
 *
 * <p>Navigation idempotente : ne recalcule le chemin que si la navigation est
 * terminée ou si le villageois est bloqué.</p>
 */
public class WanderAroundVillageBehavior extends Behavior<MillVillagerEntity> {

    private static final int    WANDER_RADIUS_XZ   = 20;
    private static final int    WANDER_RADIUS_Y    = 4;
    private static final double WALK_SPEED         = 0.6;
    private static final int    STUCK_TICKS        = 40;
    private static final double MIN_MOVE_DIST_SQ   = 0.5 * 0.5;

    private BlockPos lastCheckPos = BlockPos.ZERO;
    private int ticksSinceLastMove = 0;

    public WanderAroundVillageBehavior() {
        super(Map.of(), 100, 300);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        return entity.getBrain().getMemory(MillMemories.WANDER_TARGET).isEmpty();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        return entity.getBrain().getMemory(MillMemories.WANDER_TARGET).isPresent();
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Vec3 target = LandRandomPos.getPos(entity, WANDER_RADIUS_XZ, WANDER_RADIUS_Y);
        if (target == null) return;

        entity.setStatus(VillagerStatus.WANDERING);
        lastCheckPos = entity.blockPosition();
        ticksSinceLastMove = 0;
        BlockPos targetPos = BlockPos.containing(target);
        entity.getBrain().setMemory(MillMemories.WANDER_TARGET, targetPos);
        entity.getNavigation().moveTo(target.x, target.y, target.z, WALK_SPEED);
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getBrain().getMemory(MillMemories.WANDER_TARGET).ifPresent(target -> {
            if (target.distSqr(entity.blockPosition()) <= 4) {
                entity.getBrain().eraseMemory(MillMemories.WANDER_TARGET);
                return;
            }

            // Navigation idempotente
            if (!entity.getNavigation().isInProgress()) {
                entity.getNavigation().moveTo(
                        target.getX() + 0.5, target.getY(), target.getZ() + 0.5, WALK_SPEED);
                lastCheckPos = entity.blockPosition();
                ticksSinceLastMove = 0;
                return;
            }

            ticksSinceLastMove++;
            if (ticksSinceLastMove >= STUCK_TICKS) {
                BlockPos currentPos = entity.blockPosition();
                if (currentPos.distSqr(lastCheckPos) < MIN_MOVE_DIST_SQ) {
                    // Bloqué → choisir une nouvelle destination
                    entity.getBrain().eraseMemory(MillMemories.WANDER_TARGET);
                }
                lastCheckPos = currentPos;
                ticksSinceLastMove = 0;
            }
        });
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getBrain().eraseMemory(MillMemories.WANDER_TARGET);
        entity.getNavigation().stop();
        entity.setStatus(VillagerStatus.IDLE);
    }
}
