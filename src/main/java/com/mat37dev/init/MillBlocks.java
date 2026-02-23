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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public class MillBlocks {

    // =========================================================================
    // SHARED — Path blocks
    // =========================================================================

    public static final Block PATH_GRAVEL = register("path_gravel",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.6f)
                    .sound(SoundType.GRAVEL));

    public static final Block PATH_GRAVEL_SLAB = register("path_gravel_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(PATH_GRAVEL));

    public static final Block PATH_DIRT = register("path_dirt",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL));

    public static final Block PATH_DIRT_SLAB = register("path_dirt_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(PATH_DIRT));

    public static final Block DIRT_WALL = register("dirt_wall",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(1.0f, 2.0f)
                    .sound(SoundType.GRAVEL));

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
