package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * Le villageois se promène de façon aléatoire dans le village.
 *
 * <p>Actif pendant l'activity {@code IDLE} (loisir). Choisit une position
 * aléatoire dans un rayon de 20 blocs, navigue dessus, puis se repose quelques
 * secondes avant de choisir une nouvelle destination.</p>
 */
public class WanderAroundVillageBehavior extends Behavior<MillVillagerEntity> {

    private static final int    WANDER_RADIUS_XZ   = 20;
    private static final int    WANDER_RADIUS_Y    = 4;
    private static final double WALK_SPEED         = 0.6;
    private static final int    PATH_REFRESH_TICKS = 20;

    private long nextPathRefresh = 0L;

    public WanderAroundVillageBehavior() {
        // Pas de condition dans le constructeur : le Behavior de base utilise
        // entryCondition aussi pour canStillUse(), ce qui stopperait le behavior
        // dès que start() pose WANDER_TARGET. L'entrée est gérée par
        // checkExtraStartConditions et la continuation par canStillUse().
        super(Map.of(), 100, 300);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        // Ne démarre que si aucune cible de balade n'est mémorisée
        return entity.getBrain().getMemory(MillMemories.WANDER_TARGET).isEmpty();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        // Continue tant qu'une cible de balade est mémorisée.
        // tick() efface WANDER_TARGET à l'arrivée → arrête le behavior.
        return entity.getBrain().getMemory(MillMemories.WANDER_TARGET).isPresent();
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Vec3 target = LandRandomPos.getPos(entity, WANDER_RADIUS_XZ, WANDER_RADIUS_Y);
        if (target == null) return;

        nextPathRefresh = 0L;
        BlockPos targetPos = BlockPos.containing(target);
        entity.getBrain().setMemory(MillMemories.WANDER_TARGET, targetPos);
        entity.getNavigation().moveTo(target.x, target.y, target.z, WALK_SPEED);
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getBrain().getMemory(MillMemories.WANDER_TARGET).ifPresent(target -> {
            if (target.distSqr(entity.blockPosition()) <= 4) {
                // Arrivé → effacer la cible (arrête le behavior via canStillUse)
                entity.getBrain().eraseMemory(MillMemories.WANDER_TARGET);
            } else if (!entity.getNavigation().isInProgress() || gameTime >= nextPathRefresh) {
                nextPathRefresh = gameTime + PATH_REFRESH_TICKS;
                entity.getNavigation().moveTo(
                        target.getX() + 0.5, target.getY(), target.getZ() + 0.5, WALK_SPEED);
            }
        });
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getBrain().eraseMemory(MillMemories.WANDER_TARGET);
        entity.getNavigation().stop();
    }
}
