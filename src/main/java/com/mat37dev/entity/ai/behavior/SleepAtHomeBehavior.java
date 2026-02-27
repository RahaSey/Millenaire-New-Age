package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillBedBlocks;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.entity.ai.MillVillagerAi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.Map;
import java.util.Optional;

/**
 * Le villageois cherche un lit dans sa maison et s'y couche pour la nuit.
 *
 * <p>Actif pendant l'activity {@code REST}, après {@link GoHomeBehavior}.
 * Scan les blocs de lit dans un rayon de 8 blocs autour de {@link MillMemories#HOME_POS}.
 * Si aucun lit n'est trouvé, le villageois s'immobilise sur place.</p>
 *
 * <p>La liste des blocs reconnus comme lits est définie dans {@link MillBedBlocks}
 * et peut être étendue par les add-ons pour les lits custom.</p>
 */
public class SleepAtHomeBehavior extends Behavior<MillVillagerEntity> {

    private static final int BED_SEARCH_RADIUS = 8;
    private static final int BED_SEARCH_HEIGHT = 4;
    private static final double WALK_SPEED = 0.6;
    private static final double SLEEP_DIST_SQR = 1.0;

    public SleepAtHomeBehavior() {
        super(Map.of(
                MillMemories.HOME_POS, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 200, 12000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        // Démarre quand le villageois est proche de chez lui
        Optional<BlockPos> homePos = entity.getBrain().getMemory(MillMemories.HOME_POS);
        if (homePos.isEmpty()) return false;
        
        // On ne dort que pendant l'activité REST
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < MillVillagerAi.REST_START && dayTime > 0) return false;

        return homePos.get().distSqr(entity.blockPosition()) <= 12 * 12;
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Optional<BlockPos> homePos = entity.getBrain().getMemory(MillMemories.HOME_POS);
        if (homePos.isEmpty()) return;

        // 1. Récupérer ou chercher un lit
        Optional<BlockPos> assignedBed = entity.getBrain().getMemory(MillMemories.HOME_BED_POS);
        BlockPos bedPos = assignedBed.orElse(null);

        if (bedPos == null) {
            bedPos = findFreeBed(level, homePos.get());
            if (bedPos != null) {
                entity.getBrain().setMemory(MillMemories.HOME_BED_POS, bedPos);
            }
        }

        // 2. Se diriger vers le lit (via WalkTarget)
        if (bedPos != null && !entity.isSleeping()) {
            entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(bedPos, (float) WALK_SPEED, 0));
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        long dayTime = level.getDayTime() % 24000L;
        // On autorise à rester dans ce behavior si on est en train de dormir ou si on essaie d'y aller
        return dayTime >= MillVillagerAi.REST_START || dayTime < 6000;
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Optional<BlockPos> bedPosOpt = entity.getBrain().getMemory(MillMemories.HOME_BED_POS);
        if (bedPosOpt.isEmpty()) return;

        BlockPos bedPos = bedPosOpt.get();
        
        if (entity.isSleeping()) {
            // Déjà au lit, rien à faire de spécial
            return;
        }

        // Si on est assez proche du lit, on se couche
        if (bedPos.closerThan(entity.blockPosition(), 1.5)) {
            entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            entity.getNavigation().stop();
            entity.startSleeping(bedPos);
        } else {
            // S'assurer qu'on a toujours une cible de marche si on n'est pas encore arrivé
            if (entity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).isEmpty()) {
                entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(bedPos, (float) WALK_SPEED, 0));
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        if (entity.isSleeping()) {
            entity.stopSleeping();
        }
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entity.getNavigation().stop();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BlockPos findFreeBed(ServerLevel level, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-BED_SEARCH_RADIUS, -BED_SEARCH_HEIGHT, -BED_SEARCH_RADIUS),
                origin.offset(BED_SEARCH_RADIUS,  BED_SEARCH_HEIGHT,  BED_SEARCH_RADIUS))) {
            
            if (MillBedBlocks.isBed(level.getBlockState(pos))) {
                // Vérifier si quelqu'un d'autre l'occupe déjà
                if (isBedFree(level, pos)) {
                    return pos.immutable();
                }
            }
        }
        return null;
    }

    private boolean isBedFree(ServerLevel level, BlockPos pos) {
        // Un lit est libre si aucune entité vivante ne dort dessus
        return level.getEntitiesOfClass(MillVillagerEntity.class, 
                new net.minecraft.world.phys.AABB(pos)).stream()
                .noneMatch(v -> v.isSleeping() && pos.equals(v.getSleepingPos().orElse(null)));
    }
}
