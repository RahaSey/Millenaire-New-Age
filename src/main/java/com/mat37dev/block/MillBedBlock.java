package com.mat37dev.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Bloc de paillasse (Straw Bed) de Millénaire.
 * Étend BedBlock pour hériter de toute la mécanique vanilla :
 * animation de dodo, passage du temps, réveil, point de réapparition, explosion dans le Nether.
 * newBlockEntity retourne null pour éviter que le BedRenderer vanilla rende un lit blanc par-dessus.
 */
public class MillBedBlock extends BedBlock {
    @SuppressWarnings("unchecked")
    public static final MapCodec<BedBlock> CODEC = (MapCodec<BedBlock>) (Object) simpleCodec(MillBedBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

    public MillBedBlock(BlockBehaviour.Properties properties) {
        super(DyeColor.WHITE, properties);
    }

    @Override
    public @NotNull MapCodec<BedBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @Nullable
    public BedBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Pas de block entity : on utilise notre modèle JSON statique.
        // Le BedRenderer vanilla (lié à BlockEntityType.BED) ne sera pas appelé.
        // Les mécaniques de sommeil sont dans BedBlock.useWithoutItem(), pas dans la block entity.
        return null;
    }
}
