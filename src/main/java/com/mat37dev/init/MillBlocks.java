package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

/**
 * Registry for all Millenaire: New Age blocks.
 *
 * Blocks are organized by civilization → material family → shape variant.
 * Naming convention: <material>_<variant>
 *
 * To add blocks for a new civilization:
 *   1. Add a clearly labelled civilization section
 *   2. Register base blocks, then stairs, then slabs, then walls
 *   3. Add the corresponding BlockItem in MillItems
 *   4. Add the item to the creative tab in MillItemGroups
 *   5. Create blockstate + model JSON files
 *   6. Import textures from OldSource/external into textures/block/ (Rule 7: OldSource independence)
 *   7. Add lang entries in en_us.json AND fr_fr.json (Rule 8: continuous i18n)
 */
public class MillBlocks {

    // =========================================================================
    // NORMANS — Stone (pierre normande)
    // Rough Norman construction stone and dressed brickwork.
    // =========================================================================

    /** Norman cobblestone — rough-hewn stone typical of Norman constructions. */
    public static final Block NORMAN_COBBLESTONE = register("norman_cobblestone",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.STONE));

    /** Norman bricks — dressed stone blocks for higher-quality Norman buildings. */
    public static final Block NORMAN_BRICKS = register("norman_bricks",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5f, 6.0f)
                    .sound(SoundType.STONE));

    /** Wet brick — uncured clay brick, intermediate before firing into Cooked Brick. */
    public static final Block WET_BRICK = register("wet_brick",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f, 0.5f)
                    .sound(SoundType.STONE));

    /** Mud brick — common building material shared across civilizations. */
    public static final Block MUD_BRICK = register("mud_brick",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(1.5f, 3.0f)
                    .sound(SoundType.STONE));

    /** Cooked brick — fired clay brick, used in higher-quality Norman buildings. */
    public static final Block COOKED_BRICK = register("cooked_brick",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.STONE));

    // -------------------------------------------------------------------------
    // Norman Stone — Stairs
    // -------------------------------------------------------------------------

    public static final Block NORMAN_COBBLESTONE_STAIRS = register("norman_cobblestone_stairs",
            props -> new StairBlock(NORMAN_COBBLESTONE.defaultBlockState(), props),
            BlockBehaviour.Properties.ofFullCopy(NORMAN_COBBLESTONE));

    public static final Block NORMAN_BRICKS_STAIRS = register("norman_bricks_stairs",
            props -> new StairBlock(NORMAN_BRICKS.defaultBlockState(), props),
            BlockBehaviour.Properties.ofFullCopy(NORMAN_BRICKS));

    public static final Block MUD_BRICK_STAIRS = register("mud_brick_stairs",
            props -> new StairBlock(MUD_BRICK.defaultBlockState(), props),
            BlockBehaviour.Properties.ofFullCopy(MUD_BRICK));

    public static final Block COOKED_BRICK_STAIRS = register("cooked_brick_stairs",
            props -> new StairBlock(COOKED_BRICK.defaultBlockState(), props),
            BlockBehaviour.Properties.ofFullCopy(COOKED_BRICK));

    // -------------------------------------------------------------------------
    // Norman Stone — Slabs
    // -------------------------------------------------------------------------

    public static final Block NORMAN_COBBLESTONE_SLAB = register("norman_cobblestone_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(NORMAN_COBBLESTONE));

    public static final Block NORMAN_BRICKS_SLAB = register("norman_bricks_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(NORMAN_BRICKS));

    public static final Block MUD_BRICK_SLAB = register("mud_brick_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(MUD_BRICK));

    public static final Block COOKED_BRICK_SLAB = register("cooked_brick_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(COOKED_BRICK));

    // -------------------------------------------------------------------------
    // Norman Stone — Walls
    // -------------------------------------------------------------------------

    public static final Block NORMAN_COBBLESTONE_WALL = register("norman_cobblestone_wall",
            WallBlock::new,
            BlockBehaviour.Properties.ofFullCopy(NORMAN_COBBLESTONE));

    public static final Block NORMAN_BRICKS_WALL = register("norman_bricks_wall",
            WallBlock::new,
            BlockBehaviour.Properties.ofFullCopy(NORMAN_BRICKS));

    public static final Block MUD_BRICK_WALL = register("mud_brick_wall",
            WallBlock::new,
            BlockBehaviour.Properties.ofFullCopy(MUD_BRICK));

    public static final Block COOKED_BRICK_WALL = register("cooked_brick_wall",
            WallBlock::new,
            BlockBehaviour.Properties.ofFullCopy(COOKED_BRICK));

    // =========================================================================
    // NORMANS — Timber frame (charpente normande)
    // The hallmark half-timbered construction of Norman architecture.
    // =========================================================================

    /** Timber frame plain (colombage) — the hallmark of Norman architecture. */
    public static final Block TIMBERFRAME = register("timberframe",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD));

    /** Timber frame with cross beams — decorative variant. */
    public static final Block TIMBERFRAME_CROSS = register("timberframe_cross",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD));

    /** Wooden bars — decorative window bars used in Norman buildings. */
    public static final Block WOODEN_BARS = register("wooden_bars",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    /** Wooden bars (dark) — darker oak variant of the Norman window bars. */
    public static final Block WOODEN_BARS_DARK = register("wooden_bars_dark",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    // -------------------------------------------------------------------------
    // Timber frame — Stairs & Slabs
    // -------------------------------------------------------------------------

    public static final Block TIMBERFRAME_STAIRS = register("timberframe_stairs",
            props -> new StairBlock(TIMBERFRAME.defaultBlockState(), props),
            BlockBehaviour.Properties.ofFullCopy(TIMBERFRAME));

    public static final Block TIMBERFRAME_SLAB = register("timberframe_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(TIMBERFRAME));

    // =========================================================================
    // NORMANS — Thatch (chaume)
    // Used for roofing on Norman peasant buildings.
    // =========================================================================

    /** Thatch — used for roofing on Norman peasant buildings. */
    public static final Block THATCH = register("thatch",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.5f, 0.5f)
                    .sound(SoundType.GRASS));

    public static final Block THATCH_STAIRS = register("thatch_stairs",
            props -> new StairBlock(THATCH.defaultBlockState(), props),
            BlockBehaviour.Properties.ofFullCopy(THATCH));

    public static final Block THATCH_SLAB = register("thatch_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(THATCH));

    // =========================================================================
    // NORMANS — Decorative
    // =========================================================================

    /**
     * Rosette — decorative round window, iconic in Norman Romanesque architecture.
     * Full directional model to be added in Phase 4 when structures are built.
     */
    public static final Block ROSETTE = register("rosette",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    // =========================================================================
    // SHARED — Path blocks
    // =========================================================================

    /** Beaten earth path — main path type in Norman villages. */
    public static final Block PATH_DIRT = register("path_dirt",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL));

    /** Gravel path — used for main roads. */
    public static final Block PATH_GRAVEL = register("path_gravel",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.6f)
                    .sound(SoundType.GRAVEL));

    // -------------------------------------------------------------------------
    // Path — Slabs
    // -------------------------------------------------------------------------

    public static final Block PATH_DIRT_SLAB = register("path_dirt_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(PATH_DIRT));

    public static final Block PATH_GRAVEL_SLAB = register("path_gravel_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(PATH_GRAVEL));

    // =========================================================================
    // SHARED — Functional blocks
    // Note: Full BlockEntity logic added in Phase 7. Registered as simple
    //       blocks for now so they appear in the creative tab.
    // =========================================================================

    /** Fire pit — cooking and heating. Full implementation in Phase 7. */
    public static final Block FIRE_PIT = register("fire_pit",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.5f, 3.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 13)
                    .noOcclusion());

    /** Locked chest — access controlled by village reputation. Full impl in Phase 7. */
    public static final Block LOCKED_CHEST = register("locked_chest",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD));

    /** Straw bed — used by villagers to sleep. Full impl in Phase 5. */
    public static final Block STRAW_BED = register("straw_bed",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.2f)
                    .sound(SoundType.GRASS)
                    .noOcclusion());

    // =========================================================================
    // NORMANS — Vegetation
    // =========================================================================

    /** Apple tree sapling — Normans are famous for their orchards and cider. */
    public static final Block SAPLING_APPLETREE = register("sapling_appletree",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .noOcclusion());

    /**
     * Apple tree leaves — placeholder Block for Phase 1.
     * Replaced by a proper LeavesBlock (decay, drops) in Phase 4.
     */
    public static final Block LEAVES_APPLETREE = register("leaves_appletree",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false));

    /** Grape vine — grows on walls. Normans use grapes for wine. */
    public static final Block GRAPE_VINE = register("grape_vine",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .strength(0.2f)
                    .sound(SoundType.GRASS)
                    .noOcclusion());

    // =========================================================================
    // Helpers
    // =========================================================================

    private static <T extends Block> T register(
            String id,
            Function<BlockBehaviour.Properties, T> factory,
            BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, id));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(props.setId(key)));
    }

    private static Block register(String id, BlockBehaviour.Properties props) {
        return register(id, Block::new, props);
    }

    public static void initialize() {
        long count = BuiltInRegistries.BLOCK.stream()
                .filter(b -> MillenaireNewAge.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(b).getNamespace()))
                .count();
        MillenaireNewAge.LOGGER.info("Registered {} blocks.", count);
    }
}
