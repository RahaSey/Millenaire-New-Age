package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;

/**
 * Le villageois fuit dans la direction opposée à la menace détectée.
 *
 * <p>Actif pendant l'activity {@code PANIC}. La menace provient de
 * {@link MillMemories#ATTACK_TARGET} mise à jour par le {@code ThreatSensor}.
 * Le villageois court à vitesse maximale loin de la menace.</p>
 *
 * <p>Ce behavior est la première étape — dans les phases futures,
 * les gardes auront un behavior {@code DefendVillageBehavior} qui
 * remplacera ce behavior pour les types avec {@code helpInAttacks=true}.</p>
 */
public class FleeFromThreatBehavior extends Behavior<MillVillagerEntity> {

    private static final int FLEE_XZ   = 15;
    private static final int FLEE_Y    = 5;
    private static final double RUN_SPEED = 1.2;

    public FleeFromThreatBehavior() {
        super(Map.of(MillMemories.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 100, 300);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        return entity.getBrain().getMemory(MillMemories.ATTACK_TARGET).isPresent();
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Optional<LivingEntity> threatOpt = entity.getBrain().getMemory(MillMemories.ATTACK_TARGET);
        if (threatOpt.isEmpty()) return;

        Vec3 awayFrom = threatOpt.get().position();
        Vec3 fleeTarget = LandRandomPos.getPosAway(entity, FLEE_XZ, FLEE_Y, awayFrom);

        if (fleeTarget != null) {
            entity.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, RUN_SPEED);
        }
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        // Rafraîchir la fuite si la navigation s'est terminée mais la menace est toujours là
        Optional<LivingEntity> threatOpt = entity.getBrain().getMemory(MillMemories.ATTACK_TARGET);
        if (threatOpt.isEmpty()) return;

        if (!entity.getNavigation().isInProgress()) {
            Vec3 awayFrom = threatOpt.get().position();
            Vec3 fleeTarget = LandRandomPos.getPosAway(entity, FLEE_XZ, FLEE_Y, awayFrom);
            if (fleeTarget != null) {
                entity.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, RUN_SPEED);
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getNavigation().stop();
    }
}
