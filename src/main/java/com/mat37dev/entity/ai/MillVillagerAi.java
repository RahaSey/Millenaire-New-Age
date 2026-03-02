package com.mat37dev.entity.ai;

import com.google.common.collect.ImmutableList;
import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.profile.BehaviorProfile;
import com.mat37dev.entity.ai.profile.BehaviorProfileRegistry;
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
 *   0 ticks     (6h00)  → Activity.WORK : va au travail (rôle-spécifique)
 *   11000 ticks (17h00) → Activity.MEET : socialise au centre du village
 *   14000 ticks (20h00) → Activity.REST : rentre, dort
 * </pre>
 *
 * <h3>Extensibilité</h3>
 * <p>Pour ajouter un behavior : créer une sous-classe de {@code Behavior<MillVillagerEntity>},
 * l'enregistrer dans {@link BehaviorProfileRegistry}, et l'utiliser dans le JSON.</p>
 */
public class MillVillagerAi {

    /** Types de mémoire utilisés par le Brain (déclarés au Brain.provider). */
    public static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
            MillMemories.HOME_POS,
            MillMemories.WORK_POS,
            MillMemories.HOME_BED_POS,
            MillMemories.HOME_ENTRANCE_POS,
            MillMemories.WANDER_TARGET,
            MillMemories.NEAREST_PLAYER,
            MillMemories.VILLAGE_CENTER_POS
    );

    /** Types de capteurs actifs pour tous les villageois. */
    public static final List<SensorType<? extends Sensor<? super MillVillagerEntity>>> SENSOR_TYPES = List.of(
            MillSensors.NEAREST_PLAYER
    );

    // ── Ticks pour le schedule manuel (en ticks MC, 0 = lever du soleil = 6h) ─

    /** Tick de début de journée de travail (6h00 = 0 ticks). */
    public static final long WORK_START = 0L;
    /** Tick de début de socialisation (17h00 ≈ 11000 ticks). */
    public static final long MEET_START = 11000L;
    /** Tick de début du repos nocturne (20h00 ≈ 14000 ticks). */
    public static final long REST_START = 14000L;

    // ── Configuration du Brain ────────────────────────────────────────────────

    /**
     * Construit et configure un Brain pour un villageois avec un profil basé sur ses behaviors.
     *
     * @param provider    le provider de Brain (mémoires + capteurs)
     * @param dynamic     les données NBT du Brain
     * @param behaviorIds liste des identifiants de behavior du JSON (ex: ["farm", "wander", "sleep"])
     */
    public static Brain<MillVillagerEntity> makeBrain(
            Brain.Provider<MillVillagerEntity> provider, Dynamic<?> dynamic,
            List<String> behaviorIds) {

        Brain<MillVillagerEntity> brain = provider.makeBrain(dynamic);

        BehaviorProfile profile = BehaviorProfileRegistry.buildProfile(behaviorIds);

        brain.addActivity(Activity.CORE, ImmutableList.copyOf(profile.core()));
        brain.addActivity(Activity.WORK, ImmutableList.copyOf(profile.work()));
        brain.addActivity(Activity.MEET, ImmutableList.copyOf(profile.meet()));
        brain.addActivity(Activity.REST, ImmutableList.copyOf(profile.rest()));
        brain.addActivity(Activity.PANIC, ImmutableList.copyOf(profile.panic()));
        brain.addActivity(Activity.IDLE, ImmutableList.copyOf(profile.idle()));

        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();

        return brain;
    }
}
