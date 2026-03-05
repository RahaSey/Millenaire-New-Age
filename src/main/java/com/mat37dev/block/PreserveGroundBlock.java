package com.mat37dev.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Bloc "Sol préservé" (Preserve Ground) de Millénaire.
 * Ce bloc est indestructible et sert de marqueur pour la génération de bâtiments.
 */
public class PreserveGroundBlock extends Block {
    public PreserveGroundBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
