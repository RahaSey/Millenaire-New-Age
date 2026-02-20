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

/**
 * Registry for all Millenaire: New Age items.
 */
public class MillItems {

    // =========================================================================
    // NORMANS — Stone
    // =========================================================================

    public static final Item NORMAN_COBBLESTONE        = registerBlockItem("norman_cobblestone",        MillBlocks.NORMAN_COBBLESTONE);
    public static final Item NORMAN_BRICKS             = registerBlockItem("norman_bricks",             MillBlocks.NORMAN_BRICKS);
    public static final Item WET_BRICK                 = registerBlockItem("wet_brick",                 MillBlocks.WET_BRICK);
    public static final Item MUD_BRICK                 = registerBlockItem("mud_brick",                 MillBlocks.MUD_BRICK);
    public static final Item COOKED_BRICK              = registerBlockItem("cooked_brick",              MillBlocks.COOKED_BRICK);

    public static final Item NORMAN_COBBLESTONE_STAIRS = registerBlockItem("norman_cobblestone_stairs", MillBlocks.NORMAN_COBBLESTONE_STAIRS);
    public static final Item NORMAN_BRICKS_STAIRS      = registerBlockItem("norman_bricks_stairs",      MillBlocks.NORMAN_BRICKS_STAIRS);
    public static final Item MUD_BRICK_STAIRS          = registerBlockItem("mud_brick_stairs",          MillBlocks.MUD_BRICK_STAIRS);
    public static final Item COOKED_BRICK_STAIRS       = registerBlockItem("cooked_brick_stairs",       MillBlocks.COOKED_BRICK_STAIRS);

    public static final Item NORMAN_COBBLESTONE_SLAB   = registerBlockItem("norman_cobblestone_slab",   MillBlocks.NORMAN_COBBLESTONE_SLAB);
    public static final Item NORMAN_BRICKS_SLAB        = registerBlockItem("norman_bricks_slab",        MillBlocks.NORMAN_BRICKS_SLAB);
    public static final Item MUD_BRICK_SLAB            = registerBlockItem("mud_brick_slab",            MillBlocks.MUD_BRICK_SLAB);
    public static final Item COOKED_BRICK_SLAB         = registerBlockItem("cooked_brick_slab",         MillBlocks.COOKED_BRICK_SLAB);

    public static final Item NORMAN_COBBLESTONE_WALL   = registerBlockItem("norman_cobblestone_wall",   MillBlocks.NORMAN_COBBLESTONE_WALL);
    public static final Item NORMAN_BRICKS_WALL        = registerBlockItem("norman_bricks_wall",        MillBlocks.NORMAN_BRICKS_WALL);
    public static final Item MUD_BRICK_WALL            = registerBlockItem("mud_brick_wall",            MillBlocks.MUD_BRICK_WALL);
    public static final Item COOKED_BRICK_WALL         = registerBlockItem("cooked_brick_wall",         MillBlocks.COOKED_BRICK_WALL);

    // =========================================================================
    // NORMANS — Timber frame
    // =========================================================================

    public static final Item TIMBERFRAME               = registerBlockItem("timberframe",               MillBlocks.TIMBERFRAME);
    public static final Item TIMBERFRAME_CROSS         = registerBlockItem("timberframe_cross",         MillBlocks.TIMBERFRAME_CROSS);
    public static final Item WOODEN_BARS               = registerBlockItem("wooden_bars",               MillBlocks.WOODEN_BARS);
    public static final Item WOODEN_BARS_DARK          = registerBlockItem("wooden_bars_dark",          MillBlocks.WOODEN_BARS_DARK);

    public static final Item TIMBERFRAME_STAIRS        = registerBlockItem("timberframe_stairs",        MillBlocks.TIMBERFRAME_STAIRS);
    public static final Item TIMBERFRAME_SLAB          = registerBlockItem("timberframe_slab",          MillBlocks.TIMBERFRAME_SLAB);

    // =========================================================================
    // NORMANS — Thatch
    // =========================================================================

    public static final Item THATCH                    = registerBlockItem("thatch",                    MillBlocks.THATCH);
    public static final Item THATCH_STAIRS             = registerBlockItem("thatch_stairs",             MillBlocks.THATCH_STAIRS);
    public static final Item THATCH_SLAB               = registerBlockItem("thatch_slab",               MillBlocks.THATCH_SLAB);

    // =========================================================================
    // NORMANS — Decorative
    // =========================================================================

    public static final Item ROSETTE                   = registerBlockItem("rosette",                   MillBlocks.ROSETTE);

    // =========================================================================
    // SHARED — Paths
    // =========================================================================

    public static final Item PATH_DIRT                 = registerBlockItem("path_dirt",                 MillBlocks.PATH_DIRT);
    public static final Item PATH_GRAVEL               = registerBlockItem("path_gravel",               MillBlocks.PATH_GRAVEL);
    public static final Item PATH_DIRT_SLAB            = registerBlockItem("path_dirt_slab",            MillBlocks.PATH_DIRT_SLAB);
    public static final Item PATH_GRAVEL_SLAB          = registerBlockItem("path_gravel_slab",          MillBlocks.PATH_GRAVEL_SLAB);

    // =========================================================================
    // SHARED — Functional
    // =========================================================================

    public static final Item FIRE_PIT                  = registerBlockItem("fire_pit",                  MillBlocks.FIRE_PIT);
    public static final Item LOCKED_CHEST              = registerBlockItem("locked_chest",               MillBlocks.LOCKED_CHEST);
    public static final Item STRAW_BED                 = registerBlockItem("straw_bed",                  MillBlocks.STRAW_BED);

    // =========================================================================
    // NORMANS — Vegetation
    // =========================================================================

    public static final Item SAPLING_APPLETREE         = registerBlockItem("sapling_appletree",         MillBlocks.SAPLING_APPLETREE);
    public static final Item LEAVES_APPLETREE          = registerBlockItem("leaves_appletree",          MillBlocks.LEAVES_APPLETREE);
    public static final Item GRAPE_VINE                = registerBlockItem("grape_vine",                 MillBlocks.GRAPE_VINE);

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
