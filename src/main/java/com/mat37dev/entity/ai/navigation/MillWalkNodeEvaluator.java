package com.mat37dev.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * NodeEvaluator custom pour les villageois Millénaire.
 *
 * <p>Traite les portes, portillons et trappes comme du sol ({@link PathType#WALKABLE})
 * pour le pathfinding A*. Cela permet aux villageois de planifier des chemins
 * à travers plusieurs portes consécutives sans se bloquer.</p>
 *
 * <p>On utilise {@code WALKABLE} et non {@code OPEN} car {@code OPEN} est traité
 * comme du vide par le pathfinder (il cherche le sol en dessous via
 * {@code tryFindFirstGroundNodeBelow}). {@code WALKABLE} crée un nœud normal
 * que le villageois peut traverser directement.</p>
 *
 * <p>L'interaction physique (ouverture/fermeture) est gérée séparément par
 * {@link com.mat37dev.entity.ai.behavior.MillDoorInteractBehavior}.</p>
 */
public class MillWalkNodeEvaluator extends WalkNodeEvaluator {

    @Override
    public @NotNull PathType getPathType(PathfindingContext context, int x, int y, int z) {
        PathType type = super.getPathType(context, x, y, z);
        return remapPathType(type, context, x, y, z);
    }

    @Override
    public @NotNull Set<PathType> getPathTypeWithinMobBB(PathfindingContext context, int x, int y, int z) {
        Set<PathType> types = super.getPathTypeWithinMobBB(context, x, y, z);

        boolean needsRemap = false;
        for (PathType type : types) {
            if (isDoorLikeType(type)) {
                needsRemap = true;
                break;
            }
        }

        if (!needsRemap) {
            return types;
        }

        EnumSet<PathType> remapped = EnumSet.noneOf(PathType.class);
        for (PathType type : types) {
            remapped.add(switch (type) {
                case DOOR_WOOD_CLOSED, DOOR_OPEN, WALKABLE_DOOR, TRAPDOOR, DANGER_TRAPDOOR -> PathType.WALKABLE;
                default -> type;
            });
        }
        return remapped;
    }

    private PathType remapPathType(PathType type, PathfindingContext context, int x, int y, int z) {
        return switch (type) {
            case DOOR_WOOD_CLOSED, DOOR_OPEN, WALKABLE_DOOR, TRAPDOOR, DANGER_TRAPDOOR -> PathType.WALKABLE;
            case FENCE -> {
                if (context.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof FenceGateBlock) {
                    yield PathType.WALKABLE;
                }
                yield type;
            }
            default -> type;
        };
    }

    private static boolean isDoorLikeType(PathType type) {
        return type == PathType.DOOR_WOOD_CLOSED
                || type == PathType.DOOR_OPEN
                || type == PathType.WALKABLE_DOOR
                || type == PathType.TRAPDOOR
                || type == PathType.DANGER_TRAPDOOR;
    }
}
