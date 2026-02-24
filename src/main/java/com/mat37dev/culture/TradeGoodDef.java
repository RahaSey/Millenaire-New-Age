package com.mat37dev.culture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TradeGoodDef(
        String id,
        String category,
        int basePrice
) {
    public static final Codec<TradeGoodDef> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(TradeGoodDef::id),
                    Codec.STRING.fieldOf("category").forGetter(TradeGoodDef::category),
                    Codec.INT.fieldOf("base_price").forGetter(TradeGoodDef::basePrice)
            ).apply(instance, TradeGoodDef::new)
    );
}
