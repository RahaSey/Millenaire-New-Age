package com.mat37dev.civilization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BuildingType(
        String id,
        String displayName,
        String structureId,
        int maxHealth,
        int maxResidents,
        boolean required
) {
    public static final Codec<BuildingType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(BuildingType::id),
                    Codec.STRING.fieldOf("display_name").forGetter(BuildingType::displayName),
                    Codec.STRING.fieldOf("structure_id").forGetter(BuildingType::structureId),
                    Codec.INT.optionalFieldOf("max_health", 100).forGetter(BuildingType::maxHealth),
                    Codec.INT.optionalFieldOf("max_residents", 4).forGetter(BuildingType::maxResidents),
                    Codec.BOOL.optionalFieldOf("required", false).forGetter(BuildingType::required)
            ).apply(instance, BuildingType::new)
    );
}
