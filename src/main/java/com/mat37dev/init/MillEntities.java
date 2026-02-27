package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.entity.MillVillagerEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class MillEntities {

    public static final EntityType<MillVillagerEntity> VILLAGER;

    static {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "villager"));
        VILLAGER = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                EntityType.Builder.of(MillVillagerEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.8F)
                        .clientTrackingRange(10)
                        .build(key));
    }

    public static void initialize() {
        MillenaireNewAge.LOGGER.info("[MNA] Entities registered.");
    }
}
