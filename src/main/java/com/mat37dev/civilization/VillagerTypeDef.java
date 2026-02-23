package com.mat37dev.civilization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record VillagerTypeDef(
        String id,
        String displayName,
        List<String> behaviors
) {
    public static final Codec<VillagerTypeDef> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(VillagerTypeDef::id),
                    Codec.STRING.fieldOf("display_name").forGetter(VillagerTypeDef::displayName),
                    Codec.STRING.listOf().optionalFieldOf("behaviors", List.of()).forGetter(VillagerTypeDef::behaviors)
            ).apply(instance, VillagerTypeDef::new)
    );
}
