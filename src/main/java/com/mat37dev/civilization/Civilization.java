package com.mat37dev.civilization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record Civilization(
        String id,
        String displayName,
        CultureLanguage language,
        List<String> compatibleBiomes,
        List<VillageType> villageTypes,
        List<BuildingType> buildingTypes,
        List<VillagerTypeDef> villagerTypes,
        List<TradeGoodDef> tradeGoods,
        List<String> knownCrops
) {
    /**
     * L'id est lu depuis le JSON mais aussi dérivé du nom de fichier.
     * Le loader utilise le nom de fichier comme source de vérité.
     */
    public static final Codec<Civilization> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(Civilization::id),
                    Codec.STRING.fieldOf("display_name").forGetter(Civilization::displayName),
                    CultureLanguage.CODEC.fieldOf("language").forGetter(Civilization::language),
                    Codec.STRING.listOf().optionalFieldOf("compatible_biomes", List.of()).forGetter(Civilization::compatibleBiomes),
                    VillageType.CODEC.listOf().optionalFieldOf("village_types", List.of()).forGetter(Civilization::villageTypes),
                    BuildingType.CODEC.listOf().optionalFieldOf("building_types", List.of()).forGetter(Civilization::buildingTypes),
                    VillagerTypeDef.CODEC.listOf().optionalFieldOf("villager_types", List.of()).forGetter(Civilization::villagerTypes),
                    TradeGoodDef.CODEC.listOf().optionalFieldOf("trade_goods", List.of()).forGetter(Civilization::tradeGoods),
                    Codec.STRING.listOf().optionalFieldOf("known_crops", List.of()).forGetter(Civilization::knownCrops)
            ).apply(instance, Civilization::new)
    );

    public Optional<VillageType> getVillageType(String typeId) {
        return villageTypes.stream().filter(v -> v.id().equals(typeId)).findFirst();
    }

    public Optional<BuildingType> getBuildingType(String typeId) {
        return buildingTypes.stream().filter(b -> b.id().equals(typeId)).findFirst();
    }

    public Optional<VillagerTypeDef> getVillagerType(String typeId) {
        return villagerTypes.stream().filter(v -> v.id().equals(typeId)).findFirst();
    }
}
