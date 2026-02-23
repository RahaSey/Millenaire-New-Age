package com.mat37dev.civilization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BuildingType(
        String id,
        String displayName,
        String structureId,
        int maxHealth,
        int maxResidents,
        boolean required,
        ProximityPreference proximity,
        PerimeterPlacement perimeterPlacement
) {
    /** Placement dans le layout général du village. */
    public enum ProximityPreference {
        CENTER, NEAR, FAR;

        public static final Codec<ProximityPreference> CODEC = Codec.STRING.xmap(
            s -> ProximityPreference.valueOf(s.toUpperCase()),
            ProximityPreference::name
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
                    Codec.INT.optionalFieldOf("max_residents", 4).forGetter(BuildingType::maxResidents),
                    Codec.BOOL.optionalFieldOf("required", false).forGetter(BuildingType::required),
                    ProximityPreference.CODEC.optionalFieldOf("proximity", ProximityPreference.CENTER).forGetter(BuildingType::proximity),
                    PerimeterPlacement.CODEC.optionalFieldOf("perimeter_placement", PerimeterPlacement.NONE).forGetter(BuildingType::perimeterPlacement)
            ).apply(instance, BuildingType::new)
    );
}
