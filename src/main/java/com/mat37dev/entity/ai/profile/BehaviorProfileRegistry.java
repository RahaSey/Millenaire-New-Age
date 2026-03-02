package com.mat37dev.entity.ai.profile;

import com.mat37dev.entity.ai.behavior.GoToWorkplaceBehavior;
import com.mat37dev.entity.ai.behavior.StayAtHomeBehavior;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Registre statique qui mappe les identifiants de behavior (du JSON) vers
 * des configurateurs de {@link BehaviorProfileBuilder}.
 *
 * <p>Les identifiants correspondent au champ {@code "behaviors"} dans les
 * définitions de villageois JSON. Exemple : {@code ["farm", "wander", "sleep"]}</p>
 *
 * <h3>Ajout d'un nouveau behavior</h3>
 * <ol>
 *   <li>Créer la classe Behavior</li>
 *   <li>Appeler {@code register("id", b -> b.addWorkBehavior(0, new MonBehavior()))}</li>
 *   <li>Utiliser {@code "id"} dans le JSON de civilisation</li>
 * </ol>
 */
public class BehaviorProfileRegistry {

    private static final Map<String, Consumer<BehaviorProfileBuilder>> REGISTRY = new LinkedHashMap<>();

    static {
        // Métiers avec lieu de travail
        register("farm",       b -> b.addWorkBehavior(0, new GoToWorkplaceBehavior()));
        register("lumberjack", b -> b.addWorkBehavior(0, new GoToWorkplaceBehavior()));
        register("smith",      b -> b.addWorkBehavior(0, new GoToWorkplaceBehavior()));
        register("trade",      b -> b.addWorkBehavior(0, new GoToWorkplaceBehavior()));
        register("supervise",  b -> b.addWorkBehavior(0, new GoToWorkplaceBehavior()));

        // Femmes au foyer
        register("housework",  b -> b.addWorkBehavior(0, new StayAtHomeBehavior()));

        // Garde (placeholder : GoToWorkplace en attendant un vrai PatrolBehavior)
        register("patrol",     b -> b.addWorkBehavior(0, new GoToWorkplaceBehavior()));

        // Universels (pas de work behavior spécifique — gérés par le builder)
        register("wander",     b -> {});
        register("sleep",      b -> {});
    }

    /**
     * Enregistre un configurateur de profil pour un identifiant de behavior.
     */
    public static void register(String id, Consumer<BehaviorProfileBuilder> configurator) {
        REGISTRY.put(id, configurator);
    }

    /**
     * Construit un {@link BehaviorProfile} complet à partir d'une liste d'identifiants.
     *
     * <p>Les identifiants inconnus sont ignorés avec un warning dans les logs.</p>
     */
    public static BehaviorProfile buildProfile(List<String> behaviorIds) {
        BehaviorProfileBuilder builder = new BehaviorProfileBuilder();
        for (String id : behaviorIds) {
            Consumer<BehaviorProfileBuilder> configurator = REGISTRY.get(id);
            if (configurator != null) {
                configurator.accept(builder);
            } else {
                com.mat37dev.MillenaireNewAge.LOGGER.warn(
                        "Unknown behavior id '{}' in villager type definition — ignored.", id);
            }
        }
        return builder.build();
    }
}
