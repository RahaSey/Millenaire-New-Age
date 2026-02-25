package com.mat37dev.culture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record Culture(
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
    public static final Codec<Culture> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(Culture::id),
                    Codec.STRING.fieldOf("display_name").forGetter(Culture::displayName),
                    CultureLanguage.CODEC.fieldOf("language").forGetter(Culture::language),
                    Codec.STRING.listOf().optionalFieldOf("compatible_biomes", List.of()).forGetter(Culture::compatibleBiomes),
                    VillageType.CODEC.listOf().optionalFieldOf("village_types", List.of()).forGetter(Culture::villageTypes),
                    BuildingType.CODEC.listOf().optionalFieldOf("building_types", List.of()).forGetter(Culture::buildingTypes),
                    VillagerTypeDef.CODEC.listOf().optionalFieldOf("villager_types", List.of()).forGetter(Culture::villagerTypes),
                    TradeGoodDef.CODEC.listOf().optionalFieldOf("trade_goods", List.of()).forGetter(Culture::tradeGoods),
                    Codec.STRING.listOf().optionalFieldOf("known_crops", List.of()).forGetter(Culture::knownCrops)
            ).apply(instance, Culture::new)
    );

    public Optional<VillageType> getVillageType(String typeId) {
        return villageTypes.stream().filter(v -> v.id().equals(typeId)).findFirst();
    }

    /**
     * Recherche un bâtiment par ID complet ({@code "normans:barracks"}) ou court ({@code "barracks"}).
     * Un ID court est automatiquement préfixé par l'identifiant de cette culture.
     */
    public Optional<BuildingType> getBuildingType(String typeId) {
        String resolved = typeId.contains(":") ? typeId : (this.id + ":" + typeId);
        return buildingTypes.stream().filter(b -> b.id().equals(resolved)).findFirst();
    }

    /**
     * Recherche un type de villageois par ID complet ou court (voir {@link #getBuildingType}).
     */
    public Optional<VillagerTypeDef> getVillagerType(String typeId) {
        String resolved = typeId.contains(":") ? typeId : (this.id + ":" + typeId);
        return villagerTypes.stream().filter(v -> v.id().equals(resolved)).findFirst();
    }
}
