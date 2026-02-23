package com.mat37dev.village;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.civilization.BuildingType;
import com.mat37dev.civilization.Civilization;
import com.mat37dev.civilization.CivilizationRegistry;
import com.mat37dev.civilization.VillageType;
import com.mat37dev.config.VillageConfig;
import com.mat37dev.creator.StructureSaveManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Algorithme central de placement de village.
 *
 * <p>Pour chaque bâtiment sélectionné, cherche une position selon
 * {@link BuildingType.ProximityPreference}, terraformes le terrain
 * via {@link TerrainAdapter}, puis place la structure NBT.</p>
 */
public class VillagePlacer {

    private VillagePlacer() {}

    /**
     * Vérifie que {@code goldPos} est assez loin de tous les villages existants.
     *
     * @return message d'erreur localisé ou {@code null} si OK
     */
    public static String checkSpacing(ServerLevel level, BlockPos goldPos) {
        int minDist = VillageConfig.villageSpacing;
        for (Village v : VillageManager.getAllVillages(level)) {
            double dx = v.getCenter().getX() - goldPos.getX();
            double dz = v.getCenter().getZ() - goldPos.getZ();
            int dist = (int) Math.sqrt(dx * dx + dz * dz);
            if (dist < minDist) {
                return "Trop proche du village '§f" + v.getName()
                    + "§c' (distance : §f" + dist + "§c blocs, minimum : §f" + minDist + "§c blocs).";
            }
        }
        return null;
    }

    /**
     * Place un village complet centré sur {@code goldPos}.
     * Supprime le bloc d'or et crée un indicateur de nom au-dessus du centre.
     *
     * @return le village créé, ou empty si la civilisation/type est introuvable
     */
    public static Optional<Village> placeVillage(MinecraftServer server, ServerLevel level,
                                                  String civId, String villageTypeId,
                                                  BlockPos goldPos) {
        Optional<Civilization> civOpt = CivilizationRegistry.get(civId);
        if (civOpt.isEmpty()) {
            MillenaireNewAge.LOGGER.error("[MNA] Civilisation '{}' introuvable.", civId);
            return Optional.empty();
        }
        Civilization civ = civOpt.get();

        Optional<VillageType> vtOpt = civ.getVillageType(villageTypeId);
        if (vtOpt.isEmpty()) {
            MillenaireNewAge.LOGGER.error("[MNA] Type '{}' introuvable dans '{}'.", villageTypeId, civId);
            return Optional.empty();
        }
        VillageType vt = vtOpt.get();

        // 1. Sélectionner & trier les bâtiments (CENTER → NEAR → FAR)
        List<BuildingType> selected = selectBuildings(civ, vt);
        selected.sort((a, b) -> a.proximity().ordinal() - b.proximity().ordinal());

        // 2. Nommer le village
        String villageName = generateVillageName(civ);

        // 3. Créer l'instance Village
        Village village = new Village(UUID.randomUUID(), villageName, civId, villageTypeId, goldPos);

        // 4. Placer chaque bâtiment
        Random rng = new Random();
        List<PlacedBuilding> placed = new ArrayList<>();

        for (BuildingType bt : selected) {
            Vec3i size = getTemplateSize(server, bt.structureId());
            if (size == null) {
                MillenaireNewAge.LOGGER.warn("[MNA] Template '{}' introuvable, bâtiment ignoré.", bt.structureId());
                continue;
            }

            BlockPos xzPos = findPositionXZ(goldPos, bt.proximity(), size, placed, rng);
            if (xzPos == null) {
                MillenaireNewAge.LOGGER.warn("[MNA] Impossible de placer '{}', position non trouvée.", bt.id());
                continue;
            }

            // Terraformer & obtenir le Y réel
            int targetY = TerrainAdapter.adapt(level, xzPos, size.getX(), size.getZ());
            BlockPos origin = new BlockPos(xzPos.getX(), targetY, xzPos.getZ());

            // Placer la structure
            boolean ok = StructureSaveManager.placeStructure(server, level, bt.structureId(),
                origin, Mirror.NONE, Rotation.NONE);
            if (!ok) {
                MillenaireNewAge.LOGGER.warn("[MNA] Échec placement '{}' en {}.", bt.structureId(), origin);
                continue;
            }

            // Enregistrer le bâtiment dans le village
            Building building = new Building(UUID.randomUUID(), village.getId(),
                bt.id(), origin, Direction.NORTH, bt.maxHealth());
            village.addBuilding(building);
            placed.add(new PlacedBuilding(xzPos.getX(), xzPos.getZ(), size.getX(), size.getZ()));

            MillenaireNewAge.LOGGER.info("[MNA] Bâtiment '{}' placé en {}.", bt.id(), origin.toShortString());
        }

        // 5. Éléments de délimitation (coins, entrées, piliers) si le type a un périmètre
        if (vt.hasWalls()) {
            PerimeterElementPlacer.place(server, level, village, civ, goldPos);
        }

        // 6. Supprimer le bloc d'or (marqueur)
        level.setBlock(goldPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        // 7. Spawner l'indicateur de nom flottant
        spawnNameIndicator(level, goldPos, villageName);

        // 8. Enregistrer dans VillageManager
        VillageManager.addVillage(level, village);
        return Optional.of(village);
    }

    // ── Sélection ─────────────────────────────────────────────────────────────

    private static List<BuildingType> selectBuildings(Civilization civ, VillageType vt) {
        List<BuildingType> result = new ArrayList<>();

        for (String id : vt.requiredBuildingIds()) {
            civ.getBuildingType(id).ifPresent(result::add);
        }

        List<String> optIds = new ArrayList<>(vt.optionalBuildingIds());
        Collections.shuffle(optIds);
        for (String id : optIds) {
            if (result.size() >= vt.maxBuildings()) break;
            civ.getBuildingType(id).ifPresent(result::add);
        }

        return result;
    }

    // ── Recherche de position XZ ──────────────────────────────────────────────

    /**
     * Retourne une position XZ valide (Y=0) ou null après 20 tentatives.
     * Pour CENTER, la position est décalée pour centrer la structure sur goldPos.
     */
    private static BlockPos findPositionXZ(BlockPos origin,
                                            BuildingType.ProximityPreference proximity,
                                            Vec3i size,
                                            List<PlacedBuilding> placed, Random rng) {
        if (proximity == BuildingType.ProximityPreference.CENTER) {
            // Centrer la structure sur goldPos (pas le coin au bloc d'or)
            int cx = origin.getX() - size.getX() / 2;
            int cz = origin.getZ() - size.getZ() / 2;
            return new BlockPos(cx, 0, cz);
        }

        int vs      = VillageConfig.villageSize;
        int spacing = VillageConfig.buildingSpacing + 10;

        int minRadius   = (proximity == BuildingType.ProximityPreference.NEAR) ? spacing : vs / 2;
        int maxRadius   = (proximity == BuildingType.ProximityPreference.NEAR) ? vs / 2 : vs;
        int radiusRange = Math.max(1, maxRadius - minRadius);

        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            int radius   = minRadius + rng.nextInt(radiusRange);
            int dx = (int) (Math.cos(angle) * radius);
            int dz = (int) (Math.sin(angle) * radius);

            BlockPos candidate = new BlockPos(origin.getX() + dx, 0, origin.getZ() + dz);

            if (!overlapsAny(candidate, size, placed, spacing)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean overlapsAny(BlockPos pos, Vec3i size,
                                        List<PlacedBuilding> placed, int minSpacing) {
        for (PlacedBuilding pb : placed) {
            int dx = Math.abs(pos.getX() - pb.x());
            int dz = Math.abs(pos.getZ() - pb.z());
            if (dx < pb.sizeX() + size.getX() + minSpacing
             && dz < pb.sizeZ() + size.getZ() + minSpacing) {
                return true;
            }
        }
        return false;
    }

    // ── Indicateur de nom ────────────────────────────────────────────────────

    /**
     * Spawne un ArmorStand invisible avec le nom du village flottant au-dessus du centre.
     */
    private static void spawnNameIndicator(ServerLevel level, BlockPos goldPos, String villageName) {
        ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, level);
        // setPos = méthode correcte en MC 1.21.10 (moveTo n'existe pas)
        // 6 blocs au-dessus pour être visible par-dessus les structures
        stand.setPos(goldPos.getX() + 0.5, goldPos.getY() + 6.0, goldPos.getZ() + 0.5);
        stand.setCustomName(Component.literal(villageName)
            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stand.setCustomNameVisible(true);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        // setMarker() est private en 1.21.10 — l'invulnérabilité empêche la suppression accidentelle
        stand.setInvulnerable(true);
        level.addFreshEntity(stand);
    }

    // ── Taille du template ────────────────────────────────────────────────────

    private static Vec3i getTemplateSize(MinecraftServer server, String structureId) {
        StructureTemplate template = StructureSaveManager.loadTemplate(server, structureId);
        if (template == null) return null;
        return template.getSize();
    }

    // ── Nommage ───────────────────────────────────────────────────────────────

    private static String generateVillageName(Civilization civ) {
        List<String> names = civ.language().villageNames();
        if (names.isEmpty()) return civ.displayName() + " Village";
        return names.get(new Random().nextInt(names.size()));
    }

    // ── Données de placement ─────────────────────────────────────────────────

    private record PlacedBuilding(int x, int z, int sizeX, int sizeZ) {}
}
