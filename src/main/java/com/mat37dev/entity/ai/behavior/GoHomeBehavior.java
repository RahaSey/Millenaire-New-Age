package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.Map;
import java.util.Optional;

/**
 * Le villageois rentre chez lui ({@link MillMemories#HOME_POS}).
 *
 * <p>Actif en début d'activity {@code REST}. S'arrête à l'arrivée pour
 * laisser la place à {@link SleepAtHomeBehavior}.</p>
 */
public class GoHomeBehavior extends Behavior<MillVillagerEntity> {

    private static final int    ARRIVAL_DIST       = 2;
    private static final double WALK_SPEED         = 0.7;

    public GoHomeBehavior() {
        super(Map.of(
                MillMemories.HOME_POS, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 100, 300);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        Optional<BlockPos> homePos = entity.getBrain().getMemory(MillMemories.HOME_POS);
        return homePos.filter(pos -> !pos.closerThan(entity.blockPosition(), ARRIVAL_DIST)).isPresent();
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getBrain().getMemory(MillMemories.HOME_POS).ifPresent(pos ->
                entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, (float) WALK_SPEED, ARRIVAL_DIST)));
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Optional<BlockPos> homePos = entity.getBrain().getMemory(MillMemories.HOME_POS);
        return homePos.filter(pos -> !pos.closerThan(entity.blockPosition(), ARRIVAL_DIST)).isPresent();
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entity.getNavigation().stop();
    }
}
