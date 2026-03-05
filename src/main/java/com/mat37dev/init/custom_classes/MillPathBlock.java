package com.mat37dev.init.custom_classes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MillPathBlock extends Block {

    protected static final VoxelShape SHAPE =
            Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);

    public MillPathBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
                                           CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
                                                    CollisionContext collisionContext) {
        return SHAPE;
    }
}
