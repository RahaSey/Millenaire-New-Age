package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Behavior permettant au villageois d'interagir avec les portes et portillons.
 *
 * <p>Scanne les nœuds à venir sur le chemin (jusqu'à {@value #LOOK_AHEAD_NODES} nœuds)
 * pour ouvrir les portes AVANT que le villageois n'arrive dessus.
 * Cela résout le problème des bâtiments à plusieurs portes (château).</p>
 *
 * <p>Un fallback de rayon {@value #DETECT_RADIUS} blocs autour de l'entité permet
 * de couvrir les cas où le chemin ne passe pas exactement par le bloc porte.</p>
 */
public class MillDoorInteractBehavior extends Behavior<MillVillagerEntity> {

    /** Distance au-delà de laquelle on ferme les portes. */
    private static final double CLOSE_DISTANCE = 3.0;
    /** Nombre de ticks immobile avant de fermer les portes. */
    private static final int IDLE_CLOSE_TICKS = 30;
    /** Nombre de nœuds à scanner en avance sur le chemin. */
    private static final int LOOK_AHEAD_NODES = 4;
    /** Rayon de détection des portes autour de l'entité (fallback). */
    private static final int DETECT_RADIUS = 2;

    private final Set<BlockPos> openedDoors = new HashSet<>();
    private int idleTicks = 0;

    public MillDoorInteractBehavior() {
        super(Map.of(), 1, Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        return true;
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        // Dormeur → fermer toutes les portes
        if (entity.isSleeping()) {
            closeAllDoors(level, entity);
            return;
        }

        Path path = entity.getNavigation().getPath();

        if (path != null && !path.isDone()) {
            idleTicks = 0;

            // Scanner les prochains nœuds du chemin pour ouvrir les portes en avance
            int currentIdx = path.getNextNodeIndex();
            int start = Math.max(0, currentIdx - 1);  // nœud précédent aussi
            int end = Math.min(path.getNodeCount(), currentIdx + LOOK_AHEAD_NODES);

            for (int i = start; i < end; i++) {
                tryOpenDoorAt(level, entity, path.getNode(i).asBlockPos());
            }
        } else {
            idleTicks++;
        }

        // Fallback : détecter les portes dans un rayon autour de l'entité
        BlockPos entityPos = entity.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                entityPos.offset(-DETECT_RADIUS, -1, -DETECT_RADIUS),
                entityPos.offset(DETECT_RADIUS, 1, DETECT_RADIUS))) {
            tryOpenDoorAt(level, entity, pos);
        }

        closeDoorsBehind(level, entity);
    }

    /**
     * Tente d'ouvrir une porte (ou portillon) à la position donnée et au-dessus.
     */
    private void tryOpenDoorAt(ServerLevel level, MillVillagerEntity entity, BlockPos pos) {
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos checkPos = pos.above(dy);
            BlockState state = level.getBlockState(checkPos);

            if (state.getBlock() instanceof DoorBlock door) {
                if (state.is(BlockTags.MOB_INTERACTABLE_DOORS) && !door.isOpen(state)) {
                    door.setOpen(entity, level, state, checkPos, true);
                    openedDoors.add(checkPos.immutable());
                }
            } else if (state.getBlock() instanceof FenceGateBlock) {
                if (state.is(BlockTags.FENCE_GATES) && !state.getValue(FenceGateBlock.OPEN)) {
                    level.setBlock(checkPos, state.setValue(FenceGateBlock.OPEN, true), 10);
                    openedDoors.add(checkPos.immutable());
                }
            }
        }
    }

    /**
     * Referme les portes que ce villageois a ouvertes.
     * Critères : trop loin OU villageois immobile depuis {@value #IDLE_CLOSE_TICKS} ticks.
     */
    private void closeDoorsBehind(ServerLevel level, MillVillagerEntity entity) {
        boolean forceClose = idleTicks >= IDLE_CLOSE_TICKS;

        Iterator<BlockPos> it = openedDoors.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            BlockState state = level.getBlockState(pos);

            if (!(state.getBlock() instanceof DoorBlock) && !(state.getBlock() instanceof FenceGateBlock)) {
                it.remove();
                continue;
            }

            boolean shouldClose = forceClose || !pos.closerToCenterThan(entity.position(), CLOSE_DISTANCE);
            if (shouldClose) {
                closeDoor(level, entity, state, pos);
                it.remove();
            }
        }
    }

    private void closeAllDoors(ServerLevel level, MillVillagerEntity entity) {
        for (BlockPos pos : openedDoors) {
            BlockState state = level.getBlockState(pos);
            closeDoor(level, entity, state, pos);
        }
        openedDoors.clear();
    }

    private void closeDoor(ServerLevel level, MillVillagerEntity entity, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof DoorBlock door && door.isOpen(state)) {
            door.setOpen(entity, level, state, pos, false);
        } else if (state.getBlock() instanceof FenceGateBlock && state.getValue(FenceGateBlock.OPEN)) {
            level.setBlock(pos, state.setValue(FenceGateBlock.OPEN, false), 10);
        }
    }
}
