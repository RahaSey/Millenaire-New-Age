package com.mat37dev.village;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Nivelle le terrain sous l'empreinte d'un bâtiment avant placement.
 *
 * <p>Algorithme :
 * <ol>
 *   <li>Calculer la hauteur cible = moyenne des hauteurs de surface de l'empreinte</li>
 *   <li>Pour chaque colonne : supprimer arbres/végétation, puis creuser ou rembourrer</li>
 * </ol>
 */
public class TerrainAdapter {

    private TerrainAdapter() {}

    /**
     * Adapte le terrain sous {@code (sizeX × sizeZ)} blocs à partir de {@code origin} (X, Z).
     *
     * @return la hauteur Y cible (premier bloc d'air au-dessus du terrain nivelé)
     *         — à utiliser comme origine Y pour {@code StructureSaveManager.placeStructure()}
     */
    public static int adapt(ServerLevel level, BlockPos origin, int sizeX, int sizeZ) {
        // 1. Calculer la hauteur cible
        int totalH = 0;
        int count  = 0;
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                totalH += level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    origin.getX() + x, origin.getZ() + z);
                count++;
            }
        }
        int targetH = count > 0 ? totalH / count : origin.getY();

        // 2. Traiter chaque colonne
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                int wx = origin.getX() + x;
                int wz = origin.getZ() + z;

                // Supprimer végétation & arbres d'abord
                clearVegetation(level, wx, wz, targetH);

                // Recalculer la surface après nettoyage
                int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wx, wz);

                if (h > targetH) {
                    // Creuser : enlever les blocs de targetH à h-1
                    for (int y = targetH; y < h; y++) {
                        level.setBlock(new BlockPos(wx, y, wz), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                } else if (h < targetH) {
                    // Rembourrer : poser de la terre de h à targetH-1
                    for (int y = h; y < targetH; y++) {
                        level.setBlock(new BlockPos(wx, y, wz), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }

                // Remplacer l'eau en surface par de la terre
                BlockPos surfacePos = new BlockPos(wx, targetH - 1, wz);
                if (level.getBlockState(surfacePos).getBlock() == Blocks.WATER) {
                    level.setBlock(surfacePos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        return targetH;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Supprime végétation et arbres sur une colonne, de la surface jusqu'à 40 blocs au-dessus. */
    private static void clearVegetation(ServerLevel level, int x, int z, int fromY) {
        for (int y = fromY - 1; y < fromY + 40; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isRemovable(level.getBlockState(pos).getBlock())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static boolean isRemovable(Block block) {
        return block == Blocks.OAK_LOG          || block == Blocks.BIRCH_LOG
            || block == Blocks.SPRUCE_LOG       || block == Blocks.JUNGLE_LOG
            || block == Blocks.ACACIA_LOG       || block == Blocks.DARK_OAK_LOG
            || block == Blocks.CHERRY_LOG       || block == Blocks.MANGROVE_LOG
            || block == Blocks.OAK_LEAVES       || block == Blocks.BIRCH_LEAVES
            || block == Blocks.SPRUCE_LEAVES    || block == Blocks.JUNGLE_LEAVES
            || block == Blocks.ACACIA_LEAVES    || block == Blocks.DARK_OAK_LEAVES
            || block == Blocks.CHERRY_LEAVES    || block == Blocks.MANGROVE_LEAVES
            || block == Blocks.AZALEA_LEAVES    || block == Blocks.FLOWERING_AZALEA_LEAVES
            || block == Blocks.SHORT_GRASS      || block == Blocks.TALL_GRASS
            || block == Blocks.FERN             || block == Blocks.LARGE_FERN
            || block == Blocks.DEAD_BUSH        || block == Blocks.DANDELION
            || block == Blocks.POPPY            || block == Blocks.BLUE_ORCHID
            || block == Blocks.ALLIUM           || block == Blocks.AZURE_BLUET
            || block == Blocks.RED_TULIP        || block == Blocks.ORANGE_TULIP
            || block == Blocks.WHITE_TULIP      || block == Blocks.PINK_TULIP
            || block == Blocks.OXEYE_DAISY      || block == Blocks.CORNFLOWER
            || block == Blocks.LILY_OF_THE_VALLEY
            || block == Blocks.SUNFLOWER        || block == Blocks.LILAC
            || block == Blocks.ROSE_BUSH        || block == Blocks.PEONY
            || block == Blocks.BROWN_MUSHROOM   || block == Blocks.RED_MUSHROOM
            || block == Blocks.VINE             || block == Blocks.SNOW;
    }
}
