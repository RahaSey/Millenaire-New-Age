package com.mat37dev.entity.ai.sensor;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

/**
 * Détecte le joueur le plus proche dans le rayon de vision du villageois
 * et met à jour la mémoire {@link MillMemories#NEAREST_PLAYER}.
 */
public class NearestPlayerSensor extends Sensor<MillVillagerEntity> {

    private static final double VISION_RADIUS = 16.0;

    @Override
    protected void doTick(ServerLevel level, MillVillagerEntity entity) {
        Player nearest = null;
        double nearestDistSq = VISION_RADIUS * VISION_RADIUS;

        for (Player player : level.players()) {
            if (player.isSpectator()) continue;
            double distSq = player.distanceToSqr(entity);
            if (distSq < nearestDistSq) {
                nearest = player;
                nearestDistSq = distSq;
            }
        }

        if (nearest != null) {
            entity.getBrain().setMemory(MillMemories.NEAREST_PLAYER, nearest);
        } else {
            entity.getBrain().eraseMemory(MillMemories.NEAREST_PLAYER);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MillMemories.NEAREST_PLAYER);
    }
}
