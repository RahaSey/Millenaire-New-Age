package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Behavior permettant au villageois d'interagir avec les portes et portillons.
 *
 * <p>Contrairement au behavior vanilla, celui-ci s'assure que le villageois
 * referme la porte derrière lui une fois qu'il s'est suffisamment éloigné.</p>
 */
public class MillDoorInteractBehavior extends Behavior<MillVillagerEntity> {

    private static final int DOOR_RADIUS = 2;
    private static final int CLOSE_DISTANCE = 3;

    private final Set<BlockPos> openedDoors = new HashSet<>();

    public MillDoorInteractBehavior() {
        super(Map.of());
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        BlockPos entityPos = entity.blockPosition();

        // 1. Détecter et ouvrir les portes devant le villageois
        for (BlockPos pos : BlockPos.betweenClosed(
                entityPos.offset(-DOOR_RADIUS, -1, -DOOR_RADIUS),
                entityPos.offset(DOOR_RADIUS, 1, DOOR_RADIUS))) {
            
            BlockState state = level.getBlockState(pos);
            if (isDoor(state)) {
                if (!isOpen(state)) {
                    setDoorOpen(level, state, pos, true);
                    openedDoors.add(pos.immutable());
                } else if (!openedDoors.contains(pos)) {
                    // Si elle est déjà ouverte mais pas par nous, on l'ajoute quand même pour la refermer
                    openedDoors.add(pos.immutable());
                }
            }
        }

        // 2. Refermer les portes ouvertes par ce villageois une fois passé
        openedDoors.removeIf(pos -> {
            // Si le bloc n'est plus une porte, on oublie
            BlockState state = level.getBlockState(pos);
            if (!isDoor(state)) return true;

            // Si le villageois est assez loin, on referme
            if (pos.distSqr(entityPos) > CLOSE_DISTANCE * CLOSE_DISTANCE) {
                if (isOpen(state)) {
                    setDoorOpen(level, state, pos, false);
                }
                return true;
            }
            return false;
        });
    }

    private boolean isDoor(BlockState state) {
        return state.is(BlockTags.WOODEN_DOORS) || state.is(BlockTags.FENCE_GATES);
    }

    private boolean isOpen(BlockState state) {
        if (state.getBlock() instanceof DoorBlock) {
            return state.getValue(DoorBlock.OPEN);
        } else if (state.getBlock() instanceof FenceGateBlock) {
            return state.getValue(FenceGateBlock.OPEN);
        }
        return false;
    }

    private void setDoorOpen(ServerLevel level, BlockState state, BlockPos pos, boolean open) {
        if (state.getBlock() instanceof DoorBlock door) {
            door.setOpen(null, level, state, pos, open);
        } else if (state.getBlock() instanceof FenceGateBlock) {
            level.setBlock(pos, state.setValue(FenceGateBlock.OPEN, open), 10);
        }
    }
}
