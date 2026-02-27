package com.mat37dev.entity.ai.sensor;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;

/**
 * Détecte les monstres (et futurs raiders) proches du villageois.
 *
 * <p>Quand une menace est détectée, la mémoire {@link MillMemories#ATTACK_TARGET}
 * est mise à jour, ce qui déclenche l'activity {@code PANIC} dans
 * {@link com.mat37dev.entity.MillVillagerEntity#customServerAiStep}.</p>
 *
 * <p>Le rayon de détection est intentionnellement plus grand que le rayon de vision
 * standard (12 blocs) pour que les villageois réagissent avant d'être au contact.</p>
 */
public class ThreatSensor extends Sensor<MillVillagerEntity> {

    private static final double THREAT_RADIUS = 12.0;

    @Override
    protected void doTick(ServerLevel level, MillVillagerEntity entity) {
        AABB searchBox = entity.getBoundingBox().inflate(THREAT_RADIUS);
        List<LivingEntity> threats = level.getEntitiesOfClass(
                LivingEntity.class, searchBox,
                e -> e instanceof Monster && !e.isDeadOrDying());

        if (threats.isEmpty()) {
            entity.getBrain().eraseMemory(MillMemories.ATTACK_TARGET);
            return;
        }

        // Garder la menace la plus proche
        LivingEntity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (LivingEntity threat : threats) {
            double distSq = threat.distanceToSqr(entity);
            if (distSq < nearestDistSq) {
                nearest = threat;
                nearestDistSq = distSq;
            }
        }
        entity.getBrain().setMemory(MillMemories.ATTACK_TARGET, nearest);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MillMemories.ATTACK_TARGET);
    }
}
