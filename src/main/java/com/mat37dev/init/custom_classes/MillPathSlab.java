package com.mat37dev.init.custom_classes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MillPathSlab extends SlabBlock {
    public MillPathSlab(Properties properties) {
        super(properties);
    }

    private VoxelShape getShape(SlabType slabType) {
        if (slabType == SlabType.TOP)
            return SlabBlock.box(0.0, 8.0, 0.0, 16.0, 15.0, 16.0);
        else if (slabType == SlabType.BOTTOM)
            return SlabBlock.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
        else if (slabType == SlabType.DOUBLE)
            return SlabBlock.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);

        return SlabBlock.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
                                           CollisionContext collisionContext) {
        
        SlabType slabType = blockState.getValue(BlockStateProperties.SLAB_TYPE);

        return getShape(slabType);
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
                                                    CollisionContext collisionContext) {

        SlabType slabType = blockState.getValue(BlockStateProperties.SLAB_TYPE);

        return getShape(slabType);
    }
}
