package com.mat37dev.entity.ai.behavior;

import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillBedBlocks;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.entity.ai.status.VillagerStatus;
import com.mat37dev.entity.ai.MillVillagerAi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.Optional;

/**
 * Le villageois cherche un lit dans sa maison et s'y couche pour la nuit.
 *
 * <p>Navigation par waypoints : si le chemin direct vers le lit échoue,
 * le villageois navigue d'abord vers l'entrée du bâtiment, puis retente
 * régulièrement un chemin direct vers le lit.</p>
 *
 * <p>Les chemins tronqués ({@code canReach=false}) sont quand même utilisés
 * pour rapprocher le villageois de sa destination, ce qui résout le problème
 * des bâtiments à plusieurs portes.</p>
 */
public class SleepAtHomeBehavior extends Behavior<MillVillagerEntity> {

    private static final int    BED_SEARCH_RADIUS  = 12;
    private static final int    BED_SEARCH_HEIGHT  = 4;
    private static final double WALK_SPEED         = 0.6;
    private static final double SLEEP_DIST         = 2.0;
    private static final int    STUCK_TICKS        = 40;
    private static final double MIN_MOVE_DIST_SQ   = 0.5 * 0.5;
    private static final int    PATH_FAIL_COOLDOWN = 60;
    /** Intervalle (ticks) entre les tentatives de chemin direct vers le lit en mode entrée. */
    private static final int    RETRY_DIRECT_PATH_INTERVAL = 40;

    private BlockPos lastCheckPos = BlockPos.ZERO;
    private int ticksSinceLastMove = 0;
    private boolean navigatingToEntrance = false;
    private long lastPathFailTick = -1000L;
    private long lastDirectPathAttempt = 0L;

    public SleepAtHomeBehavior() {
        super(Map.of(MillMemories.HOME_POS, MemoryStatus.VALUE_PRESENT), 1, Integer.MAX_VALUE);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MillVillagerEntity entity) {
        Optional<BlockPos> homePos = entity.getBrain().getMemory(MillMemories.HOME_POS);
        if (homePos.isEmpty()) return false;

        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= MillVillagerAi.REST_START || dayTime < MillVillagerAi.WORK_START;
    }

    @Override
    protected void start(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        Optional<BlockPos> homePos = entity.getBrain().getMemory(MillMemories.HOME_POS);
        if (homePos.isEmpty()) return;

        lastCheckPos = entity.blockPosition();
        ticksSinceLastMove = 0;
        navigatingToEntrance = false;
        lastPathFailTick = -1000L;
        lastDirectPathAttempt = 0L;
        entity.setStatus(VillagerStatus.SLEEPING);

        if (entity.isSleeping()) return;

        // Chercher ou réutiliser le lit assigné
        BlockPos bedPos = entity.getBrain().getMemory(MillMemories.HOME_BED_POS).orElse(null);
        if (bedPos == null) {
            bedPos = findFreeBed(level, entity, homePos.get());
            if (bedPos != null) {
                entity.getBrain().setMemory(MillMemories.HOME_BED_POS, bedPos);
            }
        }

        if (bedPos == null) return;

        // Essayer de naviguer directement vers le lit
        Path pathToBed = entity.getNavigation().createPath(bedPos, 1);
        if (pathToBed != null && pathToBed.canReach()) {
            entity.getNavigation().moveTo(pathToBed, WALK_SPEED);
        } else {
            // Pas de chemin direct complet → naviguer vers l'entrée d'abord
            navigatingToEntrance = true;
            navigateTo(entity, getEntrancePos(entity), gameTime);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= MillVillagerAi.REST_START || dayTime < MillVillagerAi.WORK_START;
    }

    @Override
    protected void tick(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        // Phase 3 : déjà au lit → rester couché
        if (entity.isSleeping()) return;

        Optional<BlockPos> bedPosOpt = entity.getBrain().getMemory(MillMemories.HOME_BED_POS);
        if (bedPosOpt.isEmpty()) return;
        BlockPos bedPos = bedPosOpt.get();

        // Phase 1 : naviguer vers l'entrée, retenter le lit régulièrement
        if (navigatingToEntrance) {
            // Tenter un chemin direct vers le lit à intervalles réguliers
            if (gameTime - lastDirectPathAttempt >= RETRY_DIRECT_PATH_INTERVAL) {
                lastDirectPathAttempt = gameTime;
                Path directPath = entity.getNavigation().createPath(bedPos, 1);
                if (directPath != null) {
                    // Chemin trouvé (même tronqué) → passer en mode direct
                    navigatingToEntrance = false;
                    entity.getNavigation().moveTo(directPath, WALK_SPEED);
                    resetStuckDetection(entity);
                    lastPathFailTick = -1000L;
                    return;
                }
            }

            handleIdempotentNavigation(entity, getEntrancePos(entity), gameTime);
            return;
        }

        // Phase 2 : naviguer vers le lit
        if (bedPos.closerThan(entity.blockPosition(), SLEEP_DIST)) {
            entity.getNavigation().stop();
            entity.startSleeping(bedPos);
            entity.setSleeping(true);
            return;
        }

        handleIdempotentNavigation(entity, bedPos, gameTime);
    }

    @Override
    protected void stop(ServerLevel level, MillVillagerEntity entity, long gameTime) {
        if (entity.isSleeping()) {
            entity.stopSleeping();
            entity.setSleeping(false);
        }
        entity.getNavigation().stop();
        entity.setStatus(VillagerStatus.IDLE);
        navigatingToEntrance = false;
    }

    // ── Navigation idempotente ────────────────────────────────────────────────

    private void handleIdempotentNavigation(MillVillagerEntity entity, BlockPos target, long gameTime) {
        if (target == null) return;

        if (!entity.getNavigation().isInProgress()) {
            if (gameTime - lastPathFailTick < PATH_FAIL_COOLDOWN) return;

            navigateTo(entity, target, gameTime);
            resetStuckDetection(entity);
            return;
        }

        ticksSinceLastMove++;
        if (ticksSinceLastMove >= STUCK_TICKS) {
            BlockPos currentPos = entity.blockPosition();
            if (currentPos.distSqr(lastCheckPos) < MIN_MOVE_DIST_SQ) {
                if (gameTime - lastPathFailTick >= PATH_FAIL_COOLDOWN) {
                    navigateTo(entity, target, gameTime);
                }
            }
            lastCheckPos = currentPos;
            ticksSinceLastMove = 0;
        }
    }

    /**
     * Navigue vers la cible. Utilise le chemin même tronqué pour rapprocher
     * le villageois. Cooldown uniquement si aucun chemin trouvé (null).
     */
    private void navigateTo(MillVillagerEntity entity, BlockPos target, long gameTime) {
        if (target == null) return;

        Path path = entity.getNavigation().createPath(target, 1);
        if (path != null) {
            entity.getNavigation().moveTo(path, WALK_SPEED);
            lastPathFailTick = -1000L;
        } else {
            lastPathFailTick = gameTime;
            entity.getNavigation().stop();
        }
    }

    private void resetStuckDetection(MillVillagerEntity entity) {
        lastCheckPos = entity.blockPosition();
        ticksSinceLastMove = 0;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BlockPos getEntrancePos(MillVillagerEntity entity) {
        Optional<BlockPos> entrance = entity.getBrain().getMemory(MillMemories.HOME_ENTRANCE_POS);
        if (entrance.isPresent()) return entrance.get();
        return entity.getBrain().getMemory(MillMemories.HOME_POS).orElse(null);
    }

    private BlockPos findFreeBed(ServerLevel level, MillVillagerEntity owner, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-BED_SEARCH_RADIUS, -BED_SEARCH_HEIGHT, -BED_SEARCH_RADIUS),
                origin.offset(BED_SEARCH_RADIUS,  BED_SEARCH_HEIGHT,  BED_SEARCH_RADIUS))) {

            if (MillBedBlocks.isBed(level.getBlockState(pos)) && isBedFree(level, owner, pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private boolean isBedFree(ServerLevel level, MillVillagerEntity owner, BlockPos pos) {
        AABB searchBox = new AABB(pos).inflate(BED_SEARCH_RADIUS);
        for (MillVillagerEntity other : level.getEntitiesOfClass(MillVillagerEntity.class, searchBox)) {
            if (other == owner) continue;
            if (other.isSleeping() && pos.equals(other.getSleepingPos().orElse(null))) {
                return false;
            }
            Optional<BlockPos> otherBed = other.getBrain().getMemory(MillMemories.HOME_BED_POS);
            if (otherBed.isPresent() && pos.equals(otherBed.get())) {
                return false;
            }
        }
        return true;
    }
}
