package com.mat37dev.culture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Définition immuable d'un type de village pour une culture donnée.
 *
 * <h3>Sélection des bâtiments au démarrage</h3>
 * <ol>
 *   <li>{@code townhall_id}           — Bâtiment central (CENTER), toujours placé en premier.</li>
 *   <li>{@code required_building_ids} — Tous toujours placés.</li>
 *   <li>{@code optional_building_ids} — Pool de pioche : on tire aléatoirement entre
 *       {@code min_starter_buildings} et {@code max_starter_buildings} bâtiments
 *       supplémentaires (avec remise — maisons multiples possibles).</li>
 * </ol>
 *
 * <h3>IDs courts</h3>
 * Les IDs dans les listes peuvent omettre le préfixe de culture (ex. {@code "barracks"} au lieu
 * de {@code "normans:barracks"}). La résolution est faite automatiquement par {@link com.mat37dev.culture.Culture}.
 *
 * <h3>Croissance future</h3>
 * {@code available_building_ids} liste tous les bâtiments que ce type de village
 * peut posséder. Utilisé pour le système de progression, pas pour la génération initiale.
 */
public record VillageType(
        String id,
        String displayName,
        String townhallId,
        List<String> requiredBuildingIds,
        List<String> optionalBuildingIds,
        List<String> availableBuildingIds,
        int minStarterBuildings,
        int maxStarterBuildings,
        boolean hasWalls,
        List<String> villagerTypeIds,
        int spawnWeight
) {
    public static final Codec<VillageType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(VillageType::id),
                    Codec.STRING.fieldOf("display_name").forGetter(VillageType::displayName),
                    Codec.STRING.optionalFieldOf("townhall_id", "").forGetter(VillageType::townhallId),
                    Codec.STRING.listOf().optionalFieldOf("required_building_ids", List.of()).forGetter(VillageType::requiredBuildingIds),
                    Codec.STRING.listOf().optionalFieldOf("optional_building_ids", List.of()).forGetter(VillageType::optionalBuildingIds),
                    Codec.STRING.listOf().optionalFieldOf("available_building_ids", List.of()).forGetter(VillageType::availableBuildingIds),
                    Codec.INT.optionalFieldOf("min_starter_buildings", 0).forGetter(VillageType::minStarterBuildings),
                    Codec.INT.optionalFieldOf("max_starter_buildings", 0).forGetter(VillageType::maxStarterBuildings),
                    Codec.BOOL.optionalFieldOf("has_walls", false).forGetter(VillageType::hasWalls),
                    Codec.STRING.listOf().optionalFieldOf("villager_type_ids", List.of()).forGetter(VillageType::villagerTypeIds),
                    Codec.INT.optionalFieldOf("spawn_weight", 1).forGetter(VillageType::spawnWeight)
            ).apply(instance, VillageType::new)
    );
}
