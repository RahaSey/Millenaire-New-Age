package com.mat37dev.village;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.culture.BuildingType;
import com.mat37dev.culture.Culture;
import com.mat37dev.config.VillageConfig;
import com.mat37dev.creator.StructureSaveManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Place les éléments de délimitation (coin, entrée, pilier) autour d'un village.
 *
 * <h3>Périmètre carré</h3>
 * Carré centré sur {@code center}, demi-côté = {@code villageSize}.
 *
 * <h3>Offsets de placement avec rotation</h3>
 * Après CW90/CCW90 les dimensions X↔Z s'échangent, donc les offsets de centrage
 * utilisent la dimension APRÈS rotation :
 * <ul>
 *   <li>NONE/CW180 : worldExtentX = sizeX, worldExtentZ = sizeZ</li>
 *   <li>CW90/CCW90 : worldExtentX = sizeZ, worldExtentZ = sizeX</li>
 * </ul>
 */
public class PerimeterElementPlacer {

    private PerimeterElementPlacer() {}

    /** Distance minimale entre un pilier et un coin/entrée (blocs). */
    private static final int MIN_SPACING = 8;

    /** Espacement maximum entre deux éléments consécutifs du périmètre (blocs). */
    private static final int MAX_PILLAR_INTERVAL = 45;

    /**
     * Place tous les éléments de délimitation pour le village.
     *
     * @param culture culture pour trouver les building types corner/entrance/pillar
     */
    public static void place(MinecraftServer server, ServerLevel level,
                              Village village, Culture culture, BlockPos center) {
        Optional<BuildingType> cornerOpt   = findPerimeterElement(culture, BuildingType.PerimeterPlacement.CORNER);
        Optional<BuildingType> entranceOpt = findPerimeterElement(culture, BuildingType.PerimeterPlacement.ENTRANCE);
        Optional<BuildingType> pillarOpt   = findPerimeterElement(culture, BuildingType.PerimeterPlacement.PILLAR);

        if (cornerOpt.isEmpty())   MillenaireNewAge.LOGGER.warn("[MNA] Aucun coin (CORNER) trouvé pour la culture '{}'.", culture.id());
        if (entranceOpt.isEmpty()) MillenaireNewAge.LOGGER.warn("[MNA] Aucune entrée (ENTRANCE) trouvée pour la culture '{}'.", culture.id());
        if (pillarOpt.isEmpty())   MillenaireNewAge.LOGGER.warn("[MNA] Aucun pilier (PILLAR) trouvé pour la culture '{}'.", culture.id());

        int r = VillageConfig.villageSize;

        // ── Coins (4) ────────────────────────────────────────────────────────
        cornerOpt.ifPresent(ct -> {
            Vec3i sz = getSize(server, ct.structureId());
            int sx = sz != null ? sz.getX() : 0;
            int sz2 = sz != null ? sz.getZ() : 0;

            placeElement(server, level, village, ct,
                center.getX() - r,       center.getZ() - r,       Rotation.CLOCKWISE_90,        "coin NW");
            placeElement(server, level, village, ct,
                center.getX() + r - sx,  center.getZ() - r,       Rotation.CLOCKWISE_180,       "coin NE");
            placeElement(server, level, village, ct,
                center.getX() + r - sx,  center.getZ() + r - sz2, Rotation.COUNTERCLOCKWISE_90, "coin SE");
            placeElement(server, level, village, ct,
                center.getX() - r,       center.getZ() + r - sz2, Rotation.NONE,                "coin SW");
        });

        // ── Entrées (4) ──────────────────────────────────────────────────────
        entranceOpt.ifPresent(et -> {
            Vec3i sz = getSize(server, et.structureId());
            int ex = sz != null ? sz.getX() : 0;
            int ez = sz != null ? sz.getZ() : 0;

            // Côté nord (z = center.z - r) : CW90 → worldExtentX=ez, worldExtentZ=ex
            placeElement(server, level, village, et,
                center.getX() - ez / 2,
                center.getZ() - r,
                Rotation.CLOCKWISE_90, "entrée nord");

            // Côté sud (z = center.z + r) : CCW90 → worldExtentX=ez, worldExtentZ=ex
            placeElement(server, level, village, et,
                center.getX() - ez / 2,
                center.getZ() + r - ex,
                Rotation.COUNTERCLOCKWISE_90, "entrée sud");

            // Côté est (x = center.x + r) : NONE → worldExtentX=ex, worldExtentZ=ez
            placeElement(server, level, village, et,
                center.getX() + r - ex,
                center.getZ() - ez / 2,
                Rotation.NONE, "entrée est");

            // Côté ouest (x = center.x - r) : CW180 → worldExtentX=ex, worldExtentZ=ez
            placeElement(server, level, village, et,
                center.getX() - r,
                center.getZ() - ez / 2,
                Rotation.CLOCKWISE_180, "entrée ouest");
        });

        // ── Piliers ──────────────────────────────────────────────────────────
        pillarOpt.ifPresent(pt -> {
            Vec3i sz = getSize(server, pt.structureId());
            int px = sz != null ? sz.getX() : 0;  // sizeX original
            int pz = sz != null ? sz.getZ() : 0;  // sizeZ original

            // Côté nord : COUNTERCLOCKWISE_90 (au lieu de CW90)
            for (int pillarX : distributePillars(-r, 0)) {
                placeElement(server, level, village, pt, center.getX() + pillarX - pz / 2, center.getZ() - r, Rotation.COUNTERCLOCKWISE_90, "pilier nord");
            }
            for (int pillarX : distributePillars(0, +r)) {
                placeElement(server, level, village, pt, center.getX() + pillarX - pz / 2, center.getZ() - r, Rotation.COUNTERCLOCKWISE_90, "pilier nord");
            }

            // Côté sud : CLOCKWISE_90 (au lieu de CCW90)
            for (int pillarX : distributePillars(-r, 0)) {
                placeElement(server, level, village, pt, center.getX() + pillarX - pz / 2, center.getZ() + r - px, Rotation.CLOCKWISE_90, "pilier sud");
            }
            for (int pillarX : distributePillars(0, +r)) {
                placeElement(server, level, village, pt, center.getX() + pillarX - pz / 2, center.getZ() + r - px, Rotation.CLOCKWISE_90, "pilier sud");
            }

            // Côté est : NONE (au lieu de CW180)
            for (int pillarZ : distributePillars(-r, 0)) {
                placeElement(server, level, village, pt, center.getX() + r - px, center.getZ() + pillarZ - pz / 2, Rotation.NONE, "pilier est");
            }
            for (int pillarZ : distributePillars(0, +r)) {
                placeElement(server, level, village, pt, center.getX() + r - px, center.getZ() + pillarZ - pz / 2, Rotation.NONE, "pilier est");
            }

            // Côté ouest : CLOCKWISE_180 (au lieu de NONE)
            for (int pillarZ : distributePillars(-r, 0)) {
                placeElement(server, level, village, pt, center.getX() - r, center.getZ() + pillarZ - pz / 2, Rotation.CLOCKWISE_180, "pilier ouest");
            }
            for (int pillarZ : distributePillars(0, +r)) {
                placeElement(server, level, village, pt, center.getX() - r, center.getZ() + pillarZ - pz / 2, Rotation.CLOCKWISE_180, "pilier ouest");
            }
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Distribue des piliers entre deux positions-clé (coin et entrée) selon MAX_PILLAR_INTERVAL.
     *
     * @param from position relative de départ (coin ou entrée) — exclue
     * @param to   position relative d'arrivée (entrée ou coin) — exclue
     * @return liste de positions relatives pour les piliers
     */
    private static List<Integer> distributePillars(int from, int to) {
        List<Integer> result = new ArrayList<>();
        int direction = Integer.signum(to - from);

        int availStart = from + direction * PerimeterElementPlacer.MIN_SPACING;
        int availEnd   = to   - direction * PerimeterElementPlacer.MIN_SPACING;
        int availSpan  = Math.abs(availEnd - availStart);

        if (availSpan <= 0) return result;

        int totalSpan = Math.abs(to - from);
        int numPillars = Math.max(0, (int) Math.ceil((double) totalSpan / PerimeterElementPlacer.MAX_PILLAR_INTERVAL) - 1);
        if (numPillars == 0) return result;

        for (int i = 1; i <= numPillars; i++) {
            int pos = from + direction * (int) ((double) Math.abs(to - from) * i / (numPillars + 1));
            result.add(pos);
        }
        return result;
    }

    /** Place un seul élément de délimitation, en snappant au terrain. */
    private static void placeElement(MinecraftServer server, ServerLevel level,
                                      Village village, BuildingType bt,
                                      int worldX, int worldZ, Rotation rotation, String label) {
        Vec3i size = getSize(server, bt.structureId());

        int sizeX = size != null ? size.getX() : 1;
        int sizeZ = size != null ? size.getZ() : 1;
        // Après CW90/CCW90, l'empreinte au sol est (sizeZ × sizeX) au lieu de (sizeX × sizeZ)
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            int tmp = sizeX; sizeX = sizeZ; sizeZ = tmp;
        }

        int targetY = computeAverageHeight(level, worldX, worldZ, sizeX, sizeZ);
        BlockPos origin = new BlockPos(worldX, targetY, worldZ);

        boolean ok = StructureSaveManager.placeStructure(server, level, bt.structureId(),
            origin, Mirror.NONE, rotation);

        if (ok) {
            Building building = new Building(UUID.randomUUID(), village.getId(),
                bt.id(), origin, Direction.NORTH, bt.maxHealth());
            village.addBuilding(building);
            MillenaireNewAge.LOGGER.debug("[MNA] {} placé en {}.", label, origin.toShortString());
        } else {
            MillenaireNewAge.LOGGER.warn("[MNA] Échec placement {} ('{}').", label, bt.structureId());
        }
    }

    /** Calcule la hauteur moyenne de surface d'une empreinte. */
    private static int computeAverageHeight(ServerLevel level, int originX, int originZ,
                                             int sizeX, int sizeZ) {
        long total = 0;
        int count  = 0;
        for (int dx = 0; dx < Math.max(1, sizeX); dx++) {
            for (int dz = 0; dz < Math.max(1, sizeZ); dz++) {
                total += level.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    originX + dx, originZ + dz);
                count++;
            }
        }
        return count > 0 ? (int)(total / count) : 64;
    }

    private static Vec3i getSize(MinecraftServer server, String structureId) {
        StructureTemplate template = StructureSaveManager.loadTemplate(server, structureId);
        return template != null ? template.getSize() : null;
    }

    private static Optional<BuildingType> findPerimeterElement(Culture culture,
                                                                BuildingType.PerimeterPlacement type) {
        return culture.buildingTypes().stream()
            .filter(bt -> bt.perimeterPlacement() == type)
            .findFirst();
    }
}
