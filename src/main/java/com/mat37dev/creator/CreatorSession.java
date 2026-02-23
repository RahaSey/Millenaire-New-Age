package com.mat37dev.creator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * État créateur par joueur (côté serveur).
 * Stocke la sélection pos1/pos2 et la structure active pour le placement.
 */
public class CreatorSession {

    private static final Map<UUID, CreatorSession> SESSIONS = new HashMap<>();

    private BlockPos pos1;
    private BlockPos pos2;
    private String selectedStructureId;
    private int placerRotation; // 0..3 → 0°/90°/180°/270°

    // ── Accès ────────────────────────────────────────────────────────────────

    public static CreatorSession get(ServerPlayer player) {
        return SESSIONS.computeIfAbsent(player.getUUID(), id -> new CreatorSession());
    }

    public static void remove(UUID uuid) {
        SESSIONS.remove(uuid);
    }

    // ── Sélection ────────────────────────────────────────────────────────────

    public void setPos1(BlockPos pos) { this.pos1 = pos; }
    public void setPos2(BlockPos pos) { this.pos2 = pos; }
    public BlockPos getPos1() { return pos1; }
    public BlockPos getPos2() { return pos2; }
    public boolean hasSelection() { return pos1 != null && pos2 != null; }

    public BlockPos getMinPos() {
        if (!hasSelection()) return null;
        return new BlockPos(
            Math.min(pos1.getX(), pos2.getX()),
            Math.min(pos1.getY(), pos2.getY()),
            Math.min(pos1.getZ(), pos2.getZ())
        );
    }

    public BlockPos getMaxPos() {
        if (!hasSelection()) return null;
        return new BlockPos(
            Math.max(pos1.getX(), pos2.getX()),
            Math.max(pos1.getY(), pos2.getY()),
            Math.max(pos1.getZ(), pos2.getZ())
        );
    }

    public Vec3i getSize() {
        if (!hasSelection()) return null;
        BlockPos min = getMinPos();
        BlockPos max = getMaxPos();
        return new Vec3i(
            max.getX() - min.getX() + 1,
            max.getY() - min.getY() + 1,
            max.getZ() - min.getZ() + 1
        );
    }

    public void clearSelection() {
        pos1 = null;
        pos2 = null;
    }

    // ── Structure active ──────────────────────────────────────────────────────

    public String getSelectedStructureId() { return selectedStructureId; }
    public void setSelectedStructureId(String id) { this.selectedStructureId = id; }

    public int getPlacerRotation() { return placerRotation; }
    public void cycleRotation() { placerRotation = (placerRotation + 1) % 4; }
    public void setPlacerRotation(int r) { placerRotation = r & 3; }

    // ── Particules de sélection ───────────────────────────────────────────────

    public void spawnSelectionParticles(ServerPlayer player) {
        if (!hasSelection()) return;
        ServerLevel level = player.level();
        BlockPos min = getMinPos();
        BlockPos max = getMaxPos();

        // Coins du AABB
        spawnCornerParticles(level, min, max);
    }

    private void spawnCornerParticles(ServerLevel level, BlockPos min, BlockPos max) {
        int[][] corners = {
            {min.getX(), min.getY(), min.getZ()},
            {max.getX() + 1, min.getY(), min.getZ()},
            {min.getX(), min.getY(), max.getZ() + 1},
            {max.getX() + 1, min.getY(), max.getZ() + 1},
            {min.getX(), max.getY() + 1, min.getZ()},
            {max.getX() + 1, max.getY() + 1, min.getZ()},
            {min.getX(), max.getY() + 1, max.getZ() + 1},
            {max.getX() + 1, max.getY() + 1, max.getZ() + 1}
        };
        for (int[] c : corners) {
            level.sendParticles(ParticleTypes.END_ROD,
                c[0], c[1], c[2], 3, 0.1, 0.1, 0.1, 0.01);
        }
    }
}
