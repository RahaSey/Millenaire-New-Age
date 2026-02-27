package com.mat37dev.entity.ai;

import com.mat37dev.entity.ai.sensor.NearestPlayerSensor;
import com.mat37dev.entity.ai.sensor.ThreatSensor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

/**
 * Registre des types de capteurs du Brain des villageois Millenaire.
 *
 * <p>Les capteurs s'exécutent automatiquement à chaque tick du Brain et
 * mettent à jour les mémoires correspondantes.</p>
 */
public class MillSensors {

    /** Détecte le joueur le plus proche → {@link MillMemories#NEAREST_PLAYER}. */
    public static final SensorType<NearestPlayerSensor> NEAREST_PLAYER =
            register("nearest_player", new SensorType<>(NearestPlayerSensor::new));

    /** Détecte les monstres proches → {@link MillMemories#ATTACK_TARGET}. */
    public static final SensorType<ThreatSensor> THREAT =
            register("threat", new SensorType<>(ThreatSensor::new));

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static <T extends Sensor<?>> SensorType<T> register(String name, SensorType<T> type) {
        return Registry.register(
                BuiltInRegistries.SENSOR_TYPE,
                ResourceLocation.fromNamespaceAndPath("millenaire-new-age", name),
                type);
    }

    /** Déclenche le chargement statique de la classe (initialisation du registre). */
    public static void init() {}
}
