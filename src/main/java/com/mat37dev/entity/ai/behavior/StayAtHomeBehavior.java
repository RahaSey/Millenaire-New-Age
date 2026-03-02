package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.entity.ai.status.VillagerStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;
import java.util.Optional;

/**
 * Le villageois reste à la maison pendant la journée (activity WORK).
 *
 * <p>Utilisé pour les femmes au foyer : navigue vers HOME_POS puis reste
 * sur place. Set le statut {@link VillagerStatus#HOUSEWORK}.</p>
 */
public class StayAtHomeBehavior extends Behavior<MillVillagerEntity> {

    private static final int    ARRIVAL_DIST       = 3;
    private static final double WALK_SPEED         = 0.7;
    private static final int    PATH_REFRESH_TICKS = 20;

    private long nextPathRefresh = 0L;

    public StayAtHomeBehavior() {
        super(Map.of(MillMemories.HOME_POS, MemoryStatus.VALUE_PRESENT), 200, 600);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        return entity.getBrain().getMemory(MillMemories.HOME_POS).isPresent();
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        nextPathRefresh = 0L;
        entity.setStatus(VillagerStatus.HOUSEWORK);
        entity.getBrain().getMemory(MillMemories.HOME_POS).ifPresent(pos ->
                entity.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, WALK_SPEED));
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        return entity.getBrain().getMemory(MillMemories.HOME_POS).isPresent();
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Optional<BlockPos> homePos = entity.getBrain().getMemory(MillMemories.HOME_POS);
        if (homePos.isEmpty()) {
            entity.getNavigation().stop();
            return;
        }
        BlockPos pos = homePos.get();
        if (pos.distSqr(entity.blockPosition()) <= (long) ARRIVAL_DIST * ARRIVAL_DIST) {
            // Arrivé — arrêter de naviguer et rester sur place
            entity.getNavigation().stop();
        } else if (!entity.getNavigation().isInProgress() || gameTime >= nextPathRefresh) {
            nextPathRefresh = gameTime + PATH_REFRESH_TICKS;
            entity.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, WALK_SPEED);
        }
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getNavigation().stop();
        entity.setStatus(VillagerStatus.IDLE);
    }
}
