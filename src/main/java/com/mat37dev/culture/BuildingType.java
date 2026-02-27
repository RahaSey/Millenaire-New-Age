package com.mat37dev.culture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record BuildingType(
        String id,
        String displayName,
        String structureId,
        int maxHealth,
        int sleepingCapacity,
        List<String> workplaceFor,
        BuildingRole role,
        double minDistanceFactor,
        double maxDistanceFactor,
        PerimeterPlacement perimeterPlacement
) {
    /**
     * Catégorie du bâtiment dans le village — détermine la priorité de sélection
     * et (pour CENTER) le mode de placement.
     *
     * <ul>
     *   <li>{@code CENTER}    — Bâtiment central unique, toujours placé en premier au centre.</li>
     *   <li>{@code REQUIRED}  — Obligatoire à la création du village (bucheron, garde…).</li>
     *   <li>{@code CORE}      — Important pour la progression (charpentier, forgeron…), pool min/max.</li>
     *   <li>{@code SECONDARY} — Enrichissant (chapelle, marché…), pool min/max.</li>
     *   <li>{@code EXTRA}     — Remplissage pur (maisons, granges…), pool jusqu'au max.</li>
     * </ul>
     */
    public enum BuildingRole {
        CENTER, REQUIRED, CORE, SECONDARY, EXTRA;

        public static final Codec<BuildingRole> CODEC = Codec.STRING.xmap(
            s -> BuildingRole.valueOf(s.toUpperCase()),
            BuildingRole::name
        );
    }

    /**
     * Rôle dans la délimitation du village (périmètre).
     * {@code NONE} = bâtiment ordinaire, non concerné par le périmètre.
     */
    public enum PerimeterPlacement {
        NONE, CORNER, ENTRANCE, PILLAR;

        public static final Codec<PerimeterPlacement> CODEC = Codec.STRING.xmap(
            s -> PerimeterPlacement.valueOf(s.toUpperCase()),
            PerimeterPlacement::name
        );
    }

    public static final Codec<BuildingType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(BuildingType::id),
                    Codec.STRING.fieldOf("display_name").forGetter(BuildingType::displayName),
                    Codec.STRING.fieldOf("structure_id").forGetter(BuildingType::structureId),
                    Codec.INT.optionalFieldOf("max_health", 100).forGetter(BuildingType::maxHealth),
                    Codec.INT.optionalFieldOf("sleeping_capacity", 0).forGetter(BuildingType::sleepingCapacity),
                    Codec.STRING.listOf().optionalFieldOf("workplace_for", List.of()).forGetter(BuildingType::workplaceFor),
                    BuildingRole.CODEC.optionalFieldOf("role", BuildingRole.EXTRA).forGetter(BuildingType::role),
                    Codec.DOUBLE.optionalFieldOf("min_distance_factor", 0.1).forGetter(BuildingType::minDistanceFactor),
                    Codec.DOUBLE.optionalFieldOf("max_distance_factor", 0.9).forGetter(BuildingType::maxDistanceFactor),
                    PerimeterPlacement.CODEC.optionalFieldOf("perimeter_placement", PerimeterPlacement.NONE).forGetter(BuildingType::perimeterPlacement)
            ).apply(instance, BuildingType::new)
    );
}
