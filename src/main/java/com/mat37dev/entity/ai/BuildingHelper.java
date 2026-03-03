package com.mat37dev.entity.ai;

import com.mat37dev.block.MillChestBlock;
import com.mat37dev.village.Building;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaires pour scanner et enregistrer les points d'intérêt d'un bâtiment.
 *
 * <p>Appelé après le placement de chaque structure NBT pour enregistrer :</p>
 * <ul>
 *   <li>Les positions des <b>lits</b> (partie HEAD uniquement) dans {@link Building#addBedPosition}</li>
 *   <li>La position de l'<b>entrée</b> (porte la plus proche de l'extérieur) dans {@link Building#setEntrancePos}</li>
 * </ul>
 */
public class BuildingHelper {

    /** Marge de scan autour de la structure (pour les portes qui débordent). */
    private static final int SCAN_MARGIN = 2;

    /**
     * Scanne la structure placée pour trouver les lits et l'entrée, et les enregistre
     * dans le {@link Building}.
     *
     * @param level    monde serveur
     * @param building bâtiment dans lequel enregistrer les résultats
     * @param size     taille de la structure NBT (obtenue via StructureTemplate.getSize())
     */
    public static void scanBuilding(ServerLevel level, Building building, Vec3i size) {
        BlockPos origin = building.getOrigin();

        int x0 = origin.getX() - SCAN_MARGIN;
        int y0 = origin.getY() - SCAN_MARGIN;
        int z0 = origin.getZ() - SCAN_MARGIN;
        int x1 = origin.getX() + size.getX() + SCAN_MARGIN;
        int y1 = origin.getY() + size.getY() + SCAN_MARGIN;
        int z1 = origin.getZ() + size.getZ() + SCAN_MARGIN;

        BlockPos nearestDoor = null;
        double nearestDoorDistSq = Double.MAX_VALUE;
        List<BlockPos> foundChests = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(x0, y0, z0), new BlockPos(x1, y1, z1))) {
            BlockState state = level.getBlockState(pos);

            // Détecter les lits (partie HEAD uniquement pour éviter les doublons)
            if (state.getBlock() instanceof BedBlock
                    && state.hasProperty(BedBlock.PART)
                    && state.getValue(BedBlock.PART) == BedPart.HEAD) {
                building.addBedPosition(pos);
            }

            // Détecter les portes (partie basse uniquement)
            if (state.getBlock() instanceof DoorBlock
                    && state.is(BlockTags.MOB_INTERACTABLE_DOORS)
                    && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {

                double distSq = pos.distSqr(origin);
                if (distSq < nearestDoorDistSq) {
                    nearestDoorDistSq = distSq;
                    nearestDoor = pos.immutable();
                }
            }

            // Détecter les coffres millénaires
            if (state.getBlock() instanceof MillChestBlock) {
                foundChests.add(pos.immutable());
            }
        }

        if (nearestDoor != null) {
            building.setEntrancePos(nearestDoor);
        }

        // Enregistrer les coffres et calculer la sellingPos
        for (BlockPos chestPos : foundChests) {
            building.addChestPosition(chestPos);
        }

        if (!foundChests.isEmpty()) {
            BlockPos sellingPos = computeSellingPos(level, foundChests, building.getEntrancePos());
            building.setSellingPos(sellingPos);
        }
    }

    /**
     * Calcule la position de dépôt (sellingPos) pour les coffres d'un bâtiment.
     *
     * <p>Pour chaque coffre, on cherche parmi ses 4 voisins horizontaux
     * le premier qui est non-solide ET a un sol solide en-dessous,
     * en préférant le voisin le plus proche de l'entrée du bâtiment.</p>
     *
     * @param level      monde serveur
     * @param chests     positions des coffres du bâtiment
     * @param entrance   position de l'entrée, peut être null
     * @return la meilleure position de dépôt, ou null si aucune trouvée
     */
    @Nullable
    private static BlockPos computeSellingPos(ServerLevel level, List<BlockPos> chests,
                                              @Nullable BlockPos entrance) {
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (BlockPos chest : chests) {
            for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                BlockPos candidate = chest.relative(dir);
                BlockState candidateState = level.getBlockState(candidate);
                BlockState floorState = level.getBlockState(candidate.below());

                // Le candidat doit être traversable (pas de collision) et avoir un sol solide
                if (!candidateState.getCollisionShape(level, candidate).isEmpty()) {
                    continue; // bloc solide = mur ou obstacle, rejeter
                }
                if (!floorState.isFaceSturdy(level, candidate.below(), Direction.UP)) {
                    continue; // pas de sol praticable
                }

                // Position non-solide avec sol : candidat valide
                // Priorité : plus proche de l'entrée
                double distSq = entrance != null
                        ? candidate.distSqr(entrance)
                        : candidate.distSqr(chests.get(0));

                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    best = candidate.immutable();
                }
            }
        }
        return best;
    }

    /**
     * Fallback : scanne un rayon fixe autour d'un point pour trouver la porte la plus proche.
     * Utilisé quand le Building n'a pas encore d'entrée enregistrée (vieux sauvegardes).
     */
    @Nullable
    public static BlockPos findNearestDoor(ServerLevel level, BlockPos origin) {
        final int RADIUS = 16;
        final int HEIGHT = 6;
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-RADIUS, -HEIGHT, -RADIUS),
                origin.offset(RADIUS, HEIGHT, RADIUS))) {

            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock
                    && state.is(BlockTags.MOB_INTERACTABLE_DOORS)
                    && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {

                double distSq = pos.distSqr(origin);
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq;
                    nearest = pos.immutable();
                }
            }
        }
        return nearest;
    }
}
