package com.mat37dev.civilization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record VillageType(
        String id,
        String displayName,
        int minBuildings,
        int maxBuildings,
        List<String> requiredBuildingIds,
        List<String> optionalBuildingIds,
        boolean hasWalls,
        List<String> villagerTypeIds
) {
    public static final Codec<VillageType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(VillageType::id),
                    Codec.STRING.fieldOf("display_name").forGetter(VillageType::displayName),
                    Codec.INT.fieldOf("min_buildings").forGetter(VillageType::minBuildings),
                    Codec.INT.fieldOf("max_buildings").forGetter(VillageType::maxBuildings),
                    Codec.STRING.listOf().optionalFieldOf("required_building_ids", List.of()).forGetter(VillageType::requiredBuildingIds),
                    Codec.STRING.listOf().optionalFieldOf("optional_building_ids", List.of()).forGetter(VillageType::optionalBuildingIds),
                    Codec.BOOL.optionalFieldOf("has_walls", false).forGetter(VillageType::hasWalls),
                    Codec.STRING.listOf().optionalFieldOf("villager_type_ids", List.of()).forGetter(VillageType::villagerTypeIds)
            ).apply(instance, VillageType::new)
    );
}
