package com.mat37dev.culture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record VillagerTypeDef(
        String id,
        String displayName,
        List<String> behaviors,
        int baseHealth,
        double movementSpeed,
        String clothingTexture,
        boolean male
) {
    public static final Codec<VillagerTypeDef> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(VillagerTypeDef::id),
                    Codec.STRING.fieldOf("display_name").forGetter(VillagerTypeDef::displayName),
                    Codec.STRING.listOf().optionalFieldOf("behaviors", List.of()).forGetter(VillagerTypeDef::behaviors),
                    Codec.INT.optionalFieldOf("base_health", 20).forGetter(VillagerTypeDef::baseHealth),
                    Codec.DOUBLE.optionalFieldOf("movement_speed", 0.55).forGetter(VillagerTypeDef::movementSpeed),
                    Codec.STRING.optionalFieldOf("clothing_texture", "").forGetter(VillagerTypeDef::clothingTexture),
                    Codec.BOOL.optionalFieldOf("male", true).forGetter(VillagerTypeDef::male)
            ).apply(instance, VillagerTypeDef::new)
    );
}
