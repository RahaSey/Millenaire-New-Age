package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.entity.ai.status.VillagerStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Le villageois reste au centre du village et regarde les entités proches.
 *
 * <p>S'active quand le villageois est à moins de {@value #ACTIVATION_DIST} blocs
 * du centre du village. Il reste sur place et tourne la tête vers les joueurs
 * ou autres villageois à proximité.</p>
 */
public class SocializeAtCenterBehavior extends Behavior<MillVillagerEntity> {

    private static final int ACTIVATION_DIST       = 6;
    private static final int LOOK_RANGE            = 8;
    /** Durée (ticks) pendant laquelle le villageois regarde la même cible. */
    private static final int LOOK_DURATION_TICKS   = 60;

    private int lookCooldown = 0;

    public SocializeAtCenterBehavior() {
        super(Map.of(MillMemories.VILLAGE_CENTER_POS, MemoryStatus.VALUE_PRESENT), 200, 600);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        Optional<BlockPos> centerPos = entity.getBrain().getMemory(MillMemories.VILLAGE_CENTER_POS);
        return centerPos.filter(blockPos -> blockPos.distSqr(entity.blockPosition()) <= (long) ACTIVATION_DIST * ACTIVATION_DIST).isPresent();
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.getNavigation().stop();
        entity.setStatus(VillagerStatus.SOCIALIZING);
        lookCooldown = 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Optional<BlockPos> centerPos = entity.getBrain().getMemory(MillMemories.VILLAGE_CENTER_POS);
        return centerPos.filter(blockPos -> blockPos.distSqr(entity.blockPosition()) <= (long) ACTIVATION_DIST * ACTIVATION_DIST).isPresent();
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        if (lookCooldown > 0) {
            lookCooldown--;
            return;
        }

        // Choisir une nouvelle cible à regarder
        AABB lookBox = entity.getBoundingBox().inflate(LOOK_RANGE);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, lookBox,
                e -> e != entity && !e.isDeadOrDying());
        if (!nearby.isEmpty()) {
            LivingEntity target = nearby.get(level.random.nextInt(nearby.size()));
            entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
            lookCooldown = LOOK_DURATION_TICKS;
        }
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        entity.setStatus(VillagerStatus.IDLE);
    }
}
