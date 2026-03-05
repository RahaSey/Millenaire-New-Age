package com.mat37dev.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * Classe de base pour les blocs marqueurs de Millénaire (positions).
 * Ces blocs sont fins comme des tapis, indestructibles et émettent des particules colorées.
 */
public class MillMarkerBlock extends Block {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private final int particleColor;

    public MillMarkerBlock(BlockBehaviour.Properties properties, int particleColor) {
        super(properties);
        this.particleColor = particleColor;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) { // Fréquence augmentée
            double r = (particleColor >> 16 & 0xFF) / 255.0;
            double g = (particleColor >> 8 & 0xFF) / 255.0;
            double b = (particleColor & 0xFF) / 255.0;
            
            for (int i = 0; i < 2; i++) { // Deux particules au lieu d'une
                double px = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                double py = pos.getY() + 0.2;
                double pz = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                
                world.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float)r, (float)g, (float)b),
                        px, py, pz, 0, 0, 0);
            }
        }
    }
}
