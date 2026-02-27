package com.mat37dev.entity.ai;

import com.google.common.collect.ImmutableList;
import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.behavior.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Set;

/**
 * Configuration centralisée du Brain des villageois Millenaire.
 *
 * <h3>Schedule (géré manuellement dans customServerAiStep)</h3>
 * <pre>
 *   0 ticks    (6h00)  → Activity.WORK   : va au travail
 *   11000 ticks (17h00) → Activity.IDLE  : loisir, balade
 *   14000 ticks (20h00) → Activity.REST  : rentre, dort
 *   PANIC (priorité absolue)             : fuit les monstres
 * </pre>
 *
 * <h3>Extensibilité</h3>
 * <p>Pour ajouter un behavior : créer une sous-classe de {@code Behavior<MillVillagerEntity>}
 * et l'ajouter dans la méthode d'enregistrement de l'activity correspondante.</p>
 */
public class MillVillagerAi {

    /** Types de mémoire utilisés par le Brain (déclarés au Brain.provider). */
    public static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
            MillMemories.HOME_POS,
            MillMemories.WORK_POS,
            MillMemories.HOME_BED_POS,
            MillMemories.WANDER_TARGET,
            MillMemories.NEAREST_PLAYER,
            MillMemories.ATTACK_TARGET
    );

    /** Types de capteurs actifs pour tous les villageois. */
    public static final List<SensorType<? extends Sensor<? super MillVillagerEntity>>> SENSOR_TYPES = List.of(
            MillSensors.NEAREST_PLAYER,
            MillSensors.THREAT
    );

    // ── Ticks pour le schedule manuel (en ticks MC, 0 = lever du soleil = 6h) ─

    /** Tick de début de journée de travail (6h00 = 0 ticks). */
    public static final long WORK_START   = 0L;
    /** Tick de début de loisir (17h00 ≈ 11000 ticks). */
    public static final long IDLE_START   = 11000L;
    /** Tick de début du repos nocturne (20h00 ≈ 14000 ticks). */
    public static final long REST_START   = 14000L;

    // ── Configuration du Brain ────────────────────────────────────────────────

    /**
     * Construit et configure un Brain pour un villageois.
     * Appelé depuis .
     */
    public static Brain<MillVillagerEntity> makeBrain(
            Brain.Provider<MillVillagerEntity> provider, Dynamic<?> dynamic) {

        Brain<MillVillagerEntity> brain = provider.makeBrain(dynamic);

        registerCoreActivity(brain);
        registerWorkActivity(brain);
        registerIdleActivity(brain);
        registerRestActivity(brain);
        registerPanicActivity(brain);

        // IDLE est l'activity par défaut au démarrage
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();

        return brain;
    }

    // ── Enregistrement des activities ─────────────────────────────────────────

    /** CORE — comportements de base toujours actifs (portes, etc.). */
    private static void registerCoreActivity(Brain<MillVillagerEntity> brain) {
        brain.addActivity(Activity.CORE, ImmutableList.of(
                Pair.of(0, new MillDoorInteractBehavior())
        ));
    }

    /** WORK — journée de travail : se rend sur le lieu de travail. */
    private static void registerWorkActivity(Brain<MillVillagerEntity> brain) {
        brain.addActivity(Activity.WORK, ImmutableList.of(
                Pair.of(0, new GoToWorkplaceBehavior())
        ));
    }

    /**
     * IDLE — loisir / soirée : se promène dans le village.
     * Les futures versions ajouteront ici la socialisation, les visites, etc.
     */
    private static void registerIdleActivity(Brain<MillVillagerEntity> brain) {
        brain.addActivity(Activity.IDLE, ImmutableList.of(
                Pair.of(0, new WanderAroundVillageBehavior())
        ));
    }

    /**
     * REST — repos nocturne : rentre chez soi, puis dort.
     * GoHome a priorité 0, Sleep a priorité 1 (démarre une fois GoHome terminé).
     */
    private static void registerRestActivity(Brain<MillVillagerEntity> brain) {
        brain.addActivity(Activity.REST, ImmutableList.of(
                Pair.of(0, new GoHomeBehavior()),
                Pair.of(1, new SleepAtHomeBehavior())
        ));
    }

    /**
     * PANIC — fuite : priorité absolue, déclenché par la présence de
     * {@link MillMemories#ATTACK_TARGET}.
     * Dans les phases futures, les gardes auront un DefendVillageBehavior ici.
     */
    private static void registerPanicActivity(Brain<MillVillagerEntity> brain) {
        brain.addActivity(Activity.PANIC, ImmutableList.of(
                Pair.of(0, new FleeFromThreatBehavior())
        ));
    }
}
