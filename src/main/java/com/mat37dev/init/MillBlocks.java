package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.init.custom_classes.MillPathBlock;
import com.mat37dev.init.custom_classes.MillPathSlab;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
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

    // TODO Make this block an EntityBlock
    public static final Block BED_STRAW = register("bed_straw", 
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(1.0f, 2.0f)
                    .sound(SoundType.BAMBOO_WOOD));

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
