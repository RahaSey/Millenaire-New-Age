package com.mat37dev.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Classe pour les blocs de source de ressources (Roche, Sable, Sol).
 * Blocs pleins, indestructibles, émettant des particules sur le dessus.
 */
public class MillSourceBlock extends Block {
    private final int particleColor;

    public MillSourceBlock(BlockBehaviour.Properties properties, int particleColor) {
        super(properties);
        this.particleColor = particleColor;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            double r = (particleColor >> 16 & 0xFF) / 255.0;
            double g = (particleColor >> 8 & 0xFF) / 255.0;
            double b = (particleColor & 0xFF) / 255.0;
            
            for (int i = 0; i < 2; i++) {
                double px = pos.getX() + random.nextDouble();
                double py = pos.getY() + 1.1; // Particules au dessus du bloc
                double pz = pos.getZ() + random.nextDouble();
                
                world.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float)r, (float)g, (float)b),
                        px, py, pz, 0, 0, 0);
            }
        }
    }
}
