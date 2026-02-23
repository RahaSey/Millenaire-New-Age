package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class MillItems {

    // =========================================================================
    // SHARED — Paths (BlockItems)
    // =========================================================================

    public static final Item PATH_GRAVEL      = registerBlockItem("path_gravel",      MillBlocks.PATH_GRAVEL);
    public static final Item PATH_GRAVEL_SLAB = registerBlockItem("path_gravel_slab", MillBlocks.PATH_GRAVEL_SLAB);
    public static final Item PATH_DIRT        = registerBlockItem("path_dirt",        MillBlocks.PATH_DIRT);
    public static final Item PATH_DIRT_SLAB   = registerBlockItem("path_dirt_slab",   MillBlocks.PATH_DIRT_SLAB);
    public static final Item DIRT_WALL        = registerBlockItem("dirt_wall",        MillBlocks.DIRT_WALL);

    // =========================================================================
    // TOOLS — Wands (pure Items)
    // =========================================================================

    public static final Item WAND_OF_SUMMONING = registerItem("wand_of_summoning", Item::new);
    public static final Item WAND_OF_NEGATION  = registerItem("wand_of_negation",  Item::new);

    // =========================================================================
    // Helpers
    // =========================================================================

    private static Item registerBlockItem(String id, Block block) {
        return registerItem(id, props -> new BlockItem(block, props));
    }

    private static Item registerItem(String id, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, id));
        Item.Properties props = new Item.Properties().setId(key);
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props));
    }

    public static void initialize() {
        MillenaireNewAge.LOGGER.info("Items registered.");
    }
}
