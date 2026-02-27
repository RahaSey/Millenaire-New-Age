package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;
import java.util.Optional;

/**
 * Le villageois se rend à son lieu de travail ({@link MillMemories#WORK_POS}).
 *
 * <p>Actif pendant l'activity {@code WORK}. S'arrête quand le villageois
 * est à moins de 3 blocs de sa destination.</p>
 */
public class GoToWorkplaceBehavior extends Behavior<MillVillagerEntity> {

    private static final int    ARRIVAL_DIST        = 3;
    private static final double WALK_SPEED          = 0.8;
    /** Intervalle de rafraîchissement du chemin (ticks). Identique à MoveToTargetSink vanilla. */
    private static final int    PATH_REFRESH_TICKS  = 20;

    /** Prochain game-tick où recalculer le chemin (par instance = par villageois). */
    private long nextPathRefresh = 0L;

    public GoToWorkplaceBehavior() {
        super(Map.of(MillMemories.WORK_POS, MemoryStatus.VALUE_PRESENT), 100, 300);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        Optional<BlockPos> workPos = entity.getBrain().getMemory(MillMemories.WORK_POS);
        if (workPos.isEmpty()) return false;
        // Démarrer uniquement si loin de la destination
        return workPos.get().distSqr(entity.blockPosition()) > (long) ARRIVAL_DIST * ARRIVAL_DIST;
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        nextPathRefresh = 0L; // forcer le calcul dès le 1er tick
        entity.getBrain().getMemory(MillMemories.WORK_POS).ifPresent(pos ->
                entity.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, WALK_SPEED));
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
        if (workPos.isEmpty()) {
            entity.getNavigation().stop();
            return;
        }
        BlockPos pos = workPos.get();
        if (pos.distSqr(entity.blockPosition()) <= (long) ARRIVAL_DIST * ARRIVAL_DIST) {
            entity.getNavigation().stop();
        } else if (!entity.getNavigation().isInProgress() || gameTime >= nextPathRefresh) {
            // Relancer la navigation : chemin terminé/bloqué OU rafraîchissement périodique
            nextPathRefresh = gameTime + PATH_REFRESH_TICKS;
            entity.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, WALK_SPEED);
        }
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getNavigation().stop();
    }
}
