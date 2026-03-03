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
 * Le villageois rentre chez lui en naviguant vers l'entrée (porte) du bâtiment.
 *
 * <p>Utilise {@link MillMemories#HOME_ENTRANCE_POS} (position de la porte) plutôt
 * que {@link MillMemories#HOME_POS} (coin de la structure).</p>
 */
public class GoHomeBehavior extends Behavior<MillVillagerEntity> {

    private static final int    ARRIVAL_DIST       = 3;
    private static final double WALK_SPEED         = 0.7;
    private static final int    STUCK_TICKS        = 40;
    private static final double MIN_MOVE_DIST_SQ   = 0.5 * 0.5;
    /** Cooldown (ticks) après un pathfinding sans résultat (null) avant de réessayer. */
    private static final int    PATH_FAIL_COOLDOWN = 60;

    private BlockPos lastCheckPos = BlockPos.ZERO;
    private int ticksSinceLastMove = 0;
    private long lastPathFailTick = -1000L;

    public GoHomeBehavior() {
        super(Map.of(MillMemories.HOME_POS, MemoryStatus.VALUE_PRESENT), 100, 600);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        if (entity.isSleeping()) return false;

        BlockPos target = getTargetPos(entity);
        if (target == null) return false;
        return !target.closerThan(entity.blockPosition(), ARRIVAL_DIST);
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.setStatus(VillagerStatus.GOING_HOME);
        lastCheckPos = entity.blockPosition();
        ticksSinceLastMove = 0;
        lastPathFailTick = -1000L;
        navigateTo(entity, getTargetPos(entity), gameTime);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        if (entity.isSleeping()) return false;

        BlockPos target = getTargetPos(entity);
        if (target == null) return false;
        return !target.closerThan(entity.blockPosition(), ARRIVAL_DIST);
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        BlockPos target = getTargetPos(entity);
        if (target == null) return;

        if (target.closerThan(entity.blockPosition(), ARRIVAL_DIST)) {
            entity.getNavigation().stop();
            return;
        }

        if (!entity.getNavigation().isInProgress()) {
            if (gameTime - lastPathFailTick < PATH_FAIL_COOLDOWN) return;
            navigateTo(entity, target, gameTime);
            return;
        }

        // Stuck detection
        ticksSinceLastMove++;
        if (ticksSinceLastMove >= STUCK_TICKS) {
            BlockPos currentPos = entity.blockPosition();
            if (currentPos.distSqr(lastCheckPos) < MIN_MOVE_DIST_SQ) {
                if (gameTime - lastPathFailTick >= PATH_FAIL_COOLDOWN) {
                    navigateTo(entity, target, gameTime);
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

    private BlockPos getTargetPos(MillVillagerEntity entity) {
        Optional<BlockPos> entrance = entity.getBrain().getMemory(MillMemories.HOME_ENTRANCE_POS);
        return entrance.orElseGet(() -> entity.getBrain().getMemory(MillMemories.HOME_POS).orElse(null));
    }

    /**
     * Navigue vers la cible. Utilise le chemin même s'il est tronqué (canReach=false)
     * pour au moins rapprocher le villageois. Cooldown uniquement si aucun chemin trouvé.
     */
    private void navigateTo(MillVillagerEntity entity, BlockPos target, long gameTime) {
        if (target == null) return;

        Path path = entity.getNavigation().createPath(target, 1);
        if (path != null) {
            // Utiliser le chemin même tronqué — rapproche le villageois
            entity.getNavigation().moveTo(path, WALK_SPEED);
            lastPathFailTick = -1000L;
        } else {
            // Aucun chemin → cooldown
            lastPathFailTick = gameTime;
            entity.getNavigation().stop();
        }
    }
}
