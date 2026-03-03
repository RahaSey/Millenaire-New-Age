package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Behavior permettant au villageois d'interagir avec portes, portillons et trappes.
 *
 * <p>Logique basée sur la proximité immédiate : ouvre les blocs interactifs
 * sur les 2 prochains nœuds du chemin quand le villageois est à portée,
 * et les referme quand il s'éloigne.</p>
 *
 * <p>Un délai minimum d'ouverture ({@value #MIN_OPEN_TICKS} ticks) empêche
 * le spam ouverture/fermeture rapide (bruit insupportable dans les couloirs
 * avec portes consécutives).</p>
 */
public class MillDoorInteractBehavior extends Behavior<MillVillagerEntity> {

    /** Distance max pour ouvrir un bloc interactif (assez tôt pour éviter la collision physique). */
    private static final double INTERACT_DISTANCE = 3.0;
    /** Distance au-delà de laquelle on ferme les blocs (> INTERACT pour les couloirs multi-portes). */
    private static final double CLOSE_DISTANCE = 4.0;
    /** Durée minimum d'ouverture en ticks (~1 seconde, anti-spam). */
    private static final int MIN_OPEN_TICKS = 20;
    /** Nombre de nœuds à scanner en avance sur le chemin. */
    private static final int LOOK_AHEAD_NODES = 3;

    /** Blocs ouverts par ce villageois : position -> tick d'ouverture. */
    private final Map<BlockPos, Long> openedBlocks = new HashMap<>();

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
        if (entity.isSleeping()) {
            closeAllBlocks(level, entity);
            return;
        }

        Path path = entity.getNavigation().getPath();
        boolean hasPath = path != null && !path.isDone();

        if (hasPath) {
            int currentIdx = path.getNextNodeIndex();
            int end = Math.min(path.getNodeCount(), currentIdx + LOOK_AHEAD_NODES);

            for (int i = currentIdx; i < end; i++) {
                BlockPos nodePos = path.getNode(i).asBlockPos();
                tryOpenAt(level, entity, nodePos, gameTime);
            }
        }

        closeDistantBlocks(level, entity, gameTime);
    }

    /**
     * Tente d'ouvrir un bloc interactif à la position donnée (et au-dessus).
     */
    private void tryOpenAt(ServerLevel level, MillVillagerEntity entity, BlockPos pos, long gameTime) {
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos checkPos = pos.above(dy);

            if (openedBlocks.containsKey(checkPos)) {
                continue;
            }

            if (!checkPos.closerToCenterThan(entity.position(), INTERACT_DISTANCE)) {
                continue;
            }

            BlockState state = level.getBlockState(checkPos);

            if (state.getBlock() instanceof DoorBlock door) {
                if (state.is(BlockTags.MOB_INTERACTABLE_DOORS) && !door.isOpen(state)) {
                    door.setOpen(entity, level, state, checkPos, true);
                    openedBlocks.put(checkPos.immutable(), gameTime);
                }
            } else if (state.getBlock() instanceof FenceGateBlock) {
                if (state.is(BlockTags.FENCE_GATES) && !state.getValue(FenceGateBlock.OPEN)) {
                    level.setBlock(checkPos, state.setValue(FenceGateBlock.OPEN, true), 10);
                    openedBlocks.put(checkPos.immutable(), gameTime);
                }
            } else if (state.getBlock() instanceof TrapDoorBlock) {
                if (state.is(BlockTags.TRAPDOORS) && !state.getValue(TrapDoorBlock.OPEN)) {
                    level.setBlock(checkPos, state.setValue(TrapDoorBlock.OPEN, true), 10);
                    openedBlocks.put(checkPos.immutable(), gameTime);
                }
            }
        }
    }

    /**
     * Referme les blocs ouverts par ce villageois quand il est assez loin
     * et que le délai minimum d'ouverture est écoulé.
     */
    private void closeDistantBlocks(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Iterator<Map.Entry<BlockPos, Long>> it = openedBlocks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Long> entry = it.next();
            BlockPos pos = entry.getKey();
            long openedAt = entry.getValue();

            BlockState state = level.getBlockState(pos);

            if (!isInteractiveBlock(state)) {
                it.remove();
                continue;
            }

            boolean farEnough = !pos.closerToCenterThan(entity.position(), CLOSE_DISTANCE);
            boolean minTimeElapsed = (gameTime - openedAt) >= MIN_OPEN_TICKS;

            if (farEnough && minTimeElapsed) {
                closeBlock(level, entity, state, pos);
                it.remove();
            }
        }
    }

    private void closeAllBlocks(ServerLevel level, MillVillagerEntity entity) {
        for (Map.Entry<BlockPos, Long> entry : openedBlocks.entrySet()) {
            BlockState state = level.getBlockState(entry.getKey());
            closeBlock(level, entity, state, entry.getKey());
        }
        openedBlocks.clear();
    }

    private void closeBlock(ServerLevel level, MillVillagerEntity entity, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof DoorBlock door && door.isOpen(state)) {
            door.setOpen(entity, level, state, pos, false);
        } else if (state.getBlock() instanceof FenceGateBlock && state.getValue(FenceGateBlock.OPEN)) {
            level.setBlock(pos, state.setValue(FenceGateBlock.OPEN, false), 10);
        } else if (state.getBlock() instanceof TrapDoorBlock && state.getValue(TrapDoorBlock.OPEN)) {
            level.setBlock(pos, state.setValue(TrapDoorBlock.OPEN, false), 10);
        }
    }

    private static boolean isInteractiveBlock(BlockState state) {
        return state.getBlock() instanceof DoorBlock
                || state.getBlock() instanceof FenceGateBlock
                || state.getBlock() instanceof TrapDoorBlock;
    }
}
