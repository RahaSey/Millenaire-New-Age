package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.entity.ai.status.VillagerStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;

/**
 * Le villageois fuit la menace détectée par {@code ThreatSensor}.
 *
 * <p>Actif pendant l'activity {@code PANIC}. Choisit une position aléatoire
 * dans la direction opposée à la menace via {@link LandRandomPos#getPosAway}.</p>
 */
public class FleeFromThreatBehavior extends Behavior<MillVillagerEntity> {

    private static final int    FLEE_RADIUS_XZ     = 16;
    private static final int    FLEE_RADIUS_Y      = 4;
    private static final double RUN_SPEED          = 1.0;
    private static final int    PATH_REFRESH_TICKS = 10;

    private long nextPathRefresh = 0L;

    public FleeFromThreatBehavior() {
        super(Map.of(MillMemories.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 100, 200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        return entity.getBrain().getMemory(MillMemories.ATTACK_TARGET).isPresent();
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        nextPathRefresh = 0L;
        entity.setStatus(VillagerStatus.FLEEING);
        flee(entity);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        return entity.getBrain().getMemory(MillMemories.ATTACK_TARGET).isPresent();
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        if (!entity.getNavigation().isInProgress() || gameTime >= nextPathRefresh) {
            nextPathRefresh = gameTime + PATH_REFRESH_TICKS;
            flee(entity);
        }
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getNavigation().stop();
        entity.setStatus(VillagerStatus.IDLE);
    }

    private void flee(MillVillagerEntity entity) {
        Optional<LivingEntity> threatOpt = entity.getBrain().getMemory(MillMemories.ATTACK_TARGET);
        if (threatOpt.isEmpty()) return;

        Vec3 threatPos = threatOpt.get().position();
        Vec3 fleeTarget = LandRandomPos.getPosAway(entity, FLEE_RADIUS_XZ, FLEE_RADIUS_Y, threatPos);
        if (fleeTarget != null) {
            entity.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, RUN_SPEED);
        }
    }
}
