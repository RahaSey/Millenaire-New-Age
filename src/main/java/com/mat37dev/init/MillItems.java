package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.creator.StructurePlacerItem;
import com.mat37dev.creator.StructureScannerItem;
import com.mat37dev.creator.WandOfSummoningItem;
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
    public static final Item PATH_SLABS        = registerBlockItem("path_slabs",        MillBlocks.PATH_SLABS);
    public static final Item PATH_SLABS_SLAB   = registerBlockItem("path_slabs_slab",   MillBlocks.PATH_SLABS_SLAB);
    public static final Item BED_STRAW        = registerBlockItem("bed_straw",        MillBlocks.BED_STRAW);
    public static final Item TIMBER_FRAME_PLAIN   = registerBlockItem("timber_frame_plain",   MillBlocks.TIMBER_FRAME_PLAIN);
    public static final Item TIMBER_FRAME_CROSS   = registerBlockItem("timber_frame_cross",   MillBlocks.TIMBER_FRAME_CROSS);
    public static final Item STAINED_GLASS_WHITE   = registerBlockItem("stained_glass_white",   MillBlocks.STAINED_GLASS_WHITE);
    public static final Item STAINED_GLASS_YELLOW   = registerBlockItem("stained_glass_yellow",   MillBlocks.STAINED_GLASS_YELLOW);
    public static final Item STAINED_GLASS_YELLOW_RED   = registerBlockItem("stained_glass_yellow_red",   MillBlocks.STAINED_GLASS_YELLOW_RED);
    public static final Item STAINED_GLASS_RED_BLUE   = registerBlockItem("stained_glass_red_blue",   MillBlocks.STAINED_GLASS_RED_BLUE);

    // =========================================================================
    // TOOLS — Wands (pure Items)
    // =========================================================================

    public static final Item WAND_OF_SUMMONING  = registerItem("wand_of_summoning",  WandOfSummoningItem::new);
    public static final Item WAND_OF_NEGATION   = registerItem("wand_of_negation",   Item::new);

    // =========================================================================
    // CREATOR TOOLS — Baguettes créateur
    // =========================================================================

    public static final Item STRUCTURE_SCANNER = registerItem("structure_scanner", StructureScannerItem::new);
    public static final Item STRUCTURE_PLACER  = registerItem("structure_placer",  StructurePlacerItem::new);

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
