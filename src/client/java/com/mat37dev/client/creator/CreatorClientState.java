package com.mat37dev.client.creator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * État créateur côté client uniquement.
 * Stocke la structure en cours de preview et la rotation courante.
 */
@Environment(EnvType.CLIENT)
public class CreatorClientState {

    private static String previewStructureId = null;
    private static List<BlockPos> previewBlocks = new ArrayList<>();
    private static Vec3i previewSize = new Vec3i(1, 1, 1);
    private static int rotation = 0; // 0..3

    // ── Setters ───────────────────────────────────────────────────────────────

    public static void setPreview(String structureId, List<BlockPos> blocks, Vec3i size) {
        previewStructureId = structureId;
        previewBlocks      = new ArrayList<>(blocks);
        previewSize        = size;
        rotation           = 0;
    }

    public static void clearPreview() {
        previewStructureId = null;
        previewBlocks      = new ArrayList<>();
    }

    public static void setRotation(int r) {
        rotation = r & 3;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public static boolean hasPreview() {
        return previewStructureId != null && !previewBlocks.isEmpty();
    }

    public static String getPreviewStructureId() { return previewStructureId; }
    public static List<BlockPos> getPreviewBlocks() { return previewBlocks; }
    public static Vec3i getPreviewSize() { return previewSize; }
    public static int getRotation() { return rotation; }
}
