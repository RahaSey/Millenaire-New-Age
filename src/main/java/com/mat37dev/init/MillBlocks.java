package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.block.MillChestBlock;
import com.mat37dev.block.MillMarkerBlock;
import com.mat37dev.block.PreserveGroundBlock;
import com.mat37dev.init.custom_classes.MillPathBlock;
import com.mat37dev.init.custom_classes.MillPathSlab;
import com.mat37dev.block.MillBedBlock;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public class MillBlocks {

    // =========================================================================
    // SHARED — Path blocks
    // =========================================================================

    public static final Block PATH_GRAVEL = registerPathBlock("path_gravel",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.6f)
                    .sound(SoundType.GRAVEL));

    public static final Block PATH_GRAVEL_SLAB = registerPathSlab("path_gravel_slab",
            BlockBehaviour.Properties.ofFullCopy(PATH_GRAVEL));

    public static final Block PATH_DIRT = registerPathBlock("path_dirt",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL));

    public static final Block PATH_DIRT_SLAB = registerPathSlab("path_dirt_slab",
            BlockBehaviour.Properties.ofFullCopy(PATH_DIRT));

    public static final Block DIRT_WALL = register("dirt_wall",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(1.0f, 2.0f)
                    .sound(SoundType.GRAVEL));

    public static final Block PATH_SLABS = registerPathBlock("path_slabs",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.STONE));

    public static final Block PATH_SLABS_SLAB = registerPathSlab("path_slabs_slab",
            BlockBehaviour.Properties.ofFullCopy(PATH_SLABS));
            
    public static final Block TIMBER_FRAME_PLAIN = register("timber_frame_plain",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.WOOD));

    public static final Block TIMBER_FRAME_CROSS = register("timber_frame_cross",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.WOOD));   
        
     public static final Block STAINED_GLASS_WHITE = registerGlass("stained_glass_white",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion());    
                    
    public static final Block STAINED_GLASS_YELLOW = registerGlass("stained_glass_yellow",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion());                 

    public static final Block STAINED_GLASS_YELLOW_RED = registerGlass("stained_glass_yellow_red",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion());  

    public static final Block STAINED_GLASS_RED_BLUE = registerGlass("stained_glass_red_blue",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion());      

    public static final Block STAINED_GLASS_GREEN_BLUE = registerGlass("stained_glass_green_blue",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion());       

    public static final Block BED_STRAW = register("bed_straw", MillBedBlock::new,
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(1.0f, 2.0f)
                    .sound(SoundType.BAMBOO_WOOD)
                    .noOcclusion());

    // =========================================================================
    // Creator tools — Blocs créateur
    // =========================================================================

    public static final Block PRESERVE_GROUND = register("preserve_ground", PreserveGroundBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1.0f, 3600000.0f)
                    .sound(SoundType.STONE));

    public static final Block SLEEPING_POS = register("sleeping_pos", p -> new MillMarkerBlock(p, 14680244), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).noCollision().strength(-1.0f, 3600000.0f));

    public static final Block SELLING_POS = register("selling_pos", p -> new MillMarkerBlock(p, 65484),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).noCollision().strength(-1.0f, 3600000.0f));

    public static final Block CRAFTING_POS = register("crafting_pos", p -> new MillMarkerBlock(p, 1158400),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).noCollision().strength(-1.0f, 3600000.0f));

    public static final Block DEFENDING_POS = register("defending_pos", p -> new MillMarkerBlock(p, 16711680),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noCollision().strength(-1.0f, 3600000.0f));

    public static final Block SHELTER_POS = register("shelter_pos", p -> new MillMarkerBlock(p, 8323127),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).noCollision().strength(-1.0f, 3600000.0f));

    public static final Block LEISURE_POS = register("leisure_pos", p -> new MillMarkerBlock(p, 15763456),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).noCollision().strength(-1.0f, 3600000.0f));

    public static final Block STALL_POS = register("stall_pos", p -> new MillMarkerBlock(p, 9868800),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).noCollision().strength(-1.0f, 3600000.0f));

    public static final Block PATH_START_POS = register("path_start_pos", p -> new MillMarkerBlock(p, 721110),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).noCollision().strength(-1.0f, 3600000.0f));

    // ── Plantations ─────────────────────────────────────────────────────────
    public static final Block TREE_OAK_SPAWN = register("tree_oak_spawn",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(-1.0f, 3600000.0f).sound(SoundType.GRASS));

    public static final Block TREE_DARK_OAK_SPAWN = register("tree_dark_oak_spawn",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(-1.0f, 3600000.0f).sound(SoundType.GRASS));

    public static final Block TREE_SPRUCE_SPAWN = register("tree_spruce_spawn",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(-1.0f, 3600000.0f).sound(SoundType.GRASS));

    // ── Spawns Animaux ─────────────────────────────────────────────────────
    public static final Block SPAWN_COW = register("spawn_cow",
            BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(-1.0f, 3600000.0f).sound(SoundType.WOOL));

    public static final Block SPAWN_SHEEP = register("spawn_sheep",
            BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(-1.0f, 3600000.0f).sound(SoundType.WOOL));

    public static final Block SPAWN_CHICKEN = register("spawn_chicken",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(-1.0f, 3600000.0f).sound(SoundType.WOOL));

    public static final Block SPAWN_PIG = register("spawn_pig",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(-1.0f, 3600000.0f).sound(SoundType.WOOL));

    // ── Sources et Sols ───────────────────────────────────────────────────
    public static final Block SOURCE_ROCK = register("source_rock",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(-1.0f, 3600000.0f).sound(SoundType.STONE).noOcclusion());

    public static final Block SOURCE_SAND = register("source_sand",
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(-1.0f, 3600000.0f).sound(SoundType.SAND).noOcclusion());

    public static final Block SOIL_FLOWER = register("soil_flower",
            BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(-1.0f, 3600000.0f).sound(SoundType.GRAVEL).noOcclusion());

    public static final Block SOIL_WHEAT = register("soil_wheat",
            BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(-1.0f, 3600000.0f).sound(SoundType.GRAVEL).noOcclusion());

    public static final Block SOIL_CARROT = register("soil_carrot",
            BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(-1.0f, 3600000.0f).sound(SoundType.GRAVEL).noOcclusion());

    public static final Block SOIL_POTATO = register("soil_potato",
            BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(-1.0f, 3600000.0f).sound(SoundType.GRAVEL).noOcclusion());

    public static final Block IMPORT_TABLE = register("import_table",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD));

    // =========================================================================
    // Storage — Coffre millénaire
    // =========================================================================

    public static final MillChestBlock MILL_CHEST = register("mill_chest", MillChestBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()); // les coffres n'occupent pas tout le volume du bloc

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

    private static MillPathBlock registerPathBlock(String id, BlockBehaviour.Properties props) {
        return register(id, MillPathBlock::new, props);
    }

    private static MillPathSlab registerPathSlab(String id, BlockBehaviour.Properties props) {
        return register(id, MillPathSlab::new, props);
    }

    private static IronBarsBlock registerGlass(String id, BlockBehaviour.Properties props) {
        return register(id, IronBarsBlock::new, props);
    }

    public static void initialize() {
        long count = BuiltInRegistries.BLOCK.stream()
                .filter(b -> MillenaireNewAge.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(b).getNamespace()))
                .count();
        MillenaireNewAge.LOGGER.info("Registered {} blocks.", count);
    }
}
