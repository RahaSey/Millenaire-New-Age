package com.mat37dev.village;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.culture.BuildingType;
import com.mat37dev.culture.Culture;
import com.mat37dev.culture.CultureRegistry;
import com.mat37dev.culture.VillageType;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.*;

/**
 * Algorithme central de placement de village.
 *
 * <p>Pour chaque bâtiment sélectionné, cherche une position XZ via une spirale
 * concentrique (facteurs {@code minDistanceFactor} / {@code maxDistanceFactor}),
 * terraformes le terrain via {@link TerrainAdapter}, puis place la structure NBT.</p>
 */
public class VillagePlacer {

    private VillagePlacer() {}

    /** Zone libre (en blocs) aplatie autour de chaque bâtiment pour les chemins futurs. */
    private static final int TERRAIN_PADDING = 1;

    /** Distance minimale (en blocs) entre les côtés d'un bâtiment et un bloc dangereux. */
    private static final int DANGER_RADIUS = 5;

    /** Variance de hauteur maximale (blocs) pour qu'un terrain soit considéré plat. */
    private static final int MAX_TERRAIN_VARIANCE = 4;

    /** Blocs considérés comme dangereux (interdisent le placement à proximité). */
    private static final Set<net.minecraft.world.level.block.Block> DANGER_BLOCKS =
        Set.of(Blocks.LAVA);

    /**
     * Vérifie que {@code goldPos} est assez loin de tous les villages existants.
     *
     * @return message d'erreur localisé ou {@code null} si OK
     */
    public static Component checkSpacing(ServerLevel level, BlockPos goldPos) {
        int minDist = VillageConfig.villageSpacing;
        for (Village v : VillageManager.getAllVillages(level)) {
            double dx = v.getCenter().getX() - goldPos.getX();
            double dz = v.getCenter().getZ() - goldPos.getZ();
            int dist = (int) Math.sqrt(dx * dx + dz * dz);
            if (dist < minDist) {
                return Component.translatable("chat.millenaire-new-age.village.spacing_error",
                    v.getName(), dist, minDist);
            }
        }
        return null;
    }

    /**
     * Vérifie que {@code goldPos} n'est pas trop proche d'un bloc dangereux
     * (en tenant compte de l'empreinte réelle du bâtiment principal CENTER).
     *
     * @return message d'erreur localisé ou {@code null} si OK
     */
    public static Component checkDanger(MinecraftServer server, ServerLevel level,
                                         String cultureId, String villageTypeId,
                                         BlockPos goldPos) {
        Optional<Culture> cultureOpt = CultureRegistry.get(cultureId);
        if (cultureOpt.isEmpty()) return null;
        Culture culture = cultureOpt.get();

        Optional<VillageType> vtOpt = culture.getVillageType(villageTypeId);
        if (vtOpt.isEmpty()) return null;
        VillageType vt = vtOpt.get();

        // Trouver le townhall (bâtiment CENTER)
        Optional<BuildingType> centerOpt = vt.townhallId().isEmpty()
            ? Optional.empty()
            : culture.getBuildingType(vt.townhallId());

        if (centerOpt.isEmpty()) return null;
        Vec3i size = getTemplateSize(server, centerOpt.get().structureId());
        if (size == null) return null;

        // Empreinte du bâtiment central, centré sur goldPos
        int x0 = goldPos.getX() - size.getX() / 2;
        int z0 = goldPos.getZ() - size.getZ() / 2;
        int x1 = x0 + size.getX();
        int z1 = z0 + size.getZ();

        if (hasDangerousBlock(level, x0 - DANGER_RADIUS, z0 - DANGER_RADIUS,
                                      x1 + DANGER_RADIUS, z1 + DANGER_RADIUS)) {
            return Component.translatable("chat.millenaire-new-age.village.danger_lava");
        }
        return null;
    }

    /**
     * Place un village via la baguette d'invocation (supprime le bloc d'or marqueur).
     *
     * @return le village créé, ou empty si la culture/type est introuvable
     */
    public static Optional<Village> placeVillage(MinecraftServer server, ServerLevel level,
                                                  String cultureId, String villageTypeId,
                                                  BlockPos goldPos) {
        return doPlaceVillage(server, level, cultureId, villageTypeId, goldPos, true);
    }

    /**
     * Place un village lors de la génération naturelle du monde (sans marqueur physique).
     */
    public static void placeVillageAt(MinecraftServer server, ServerLevel level,
                                      String cultureId, String villageTypeId,
                                      BlockPos center) {
        doPlaceVillage(server, level, cultureId, villageTypeId, center, false);
    }

    private static Optional<Village> doPlaceVillage(MinecraftServer server, ServerLevel level,
                                                     String cultureId, String villageTypeId,
                                                     BlockPos center, boolean removeMarker) {
        Optional<Culture> cultureOpt = CultureRegistry.get(cultureId);
        if (cultureOpt.isEmpty()) {
            MillenaireNewAge.LOGGER.error("[MNA] Culture '{}' introuvable.", cultureId);
            return Optional.empty();
        }
        Culture culture = cultureOpt.get();

        Optional<VillageType> vtOpt = culture.getVillageType(villageTypeId);
        if (vtOpt.isEmpty()) {
            MillenaireNewAge.LOGGER.error("[MNA] Type '{}' introuvable dans '{}'.", villageTypeId, cultureId);
            return Optional.empty();
        }
        VillageType vt = vtOpt.get();

        // 1. Initialiser le générateur aléatoire (partagé entre sélection et placement)
        Random rng = new Random();

        // 2. Sélectionner & trier les bâtiments (CENTER → REQUIRED → CORE → SECONDARY → EXTRA)
        List<BuildingType> selected = selectBuildings(culture, vt, rng);
        selected.sort(Comparator.comparingInt(a -> a.role().ordinal()));

        // 3. Nommer le village (nom unique dans ce monde pour cette culture)
        String villageName = generateVillageName(culture, level);

        // 4. Créer l'instance Village
        Village village = new Village(UUID.randomUUID(), villageName, cultureId, villageTypeId, center);

        // 5. Placer le périmètre d'abord + récupérer ses empreintes pour la détection de collision
        List<PlacedBuilding> placed = new ArrayList<>(
            PerimeterElementPlacer.place(server, level, village, culture, center));

        // 6. Placer chaque bâtiment (qui évite maintenant le périmètre)
        for (BuildingType bt : selected) {
            Vec3i size = getTemplateSize(server, bt.structureId());
            if (size == null) {
                MillenaireNewAge.LOGGER.warn("[MNA] Template '{}' introuvable, bâtiment ignoré.", bt.structureId());
                continue;
            }

            BlockPos xzPos = findPositionXZ(center, bt, size, placed, rng, level);
            if (xzPos == null) {
                MillenaireNewAge.LOGGER.warn("[MNA] Impossible de placer '{}', position non trouvée.", bt.id());
                continue;
            }

            // Terraformer en préservant les empreintes voisines déjà posées
            int targetY = TerrainAdapter.adapt(level, xzPos, size.getX(), size.getZ(), TERRAIN_PADDING, placed);

            // On ajuste le Y selon si la structure doit être "fondue" dans le sol
            if (StructureSaveManager.shouldEmbedInGround(server, bt.structureId())) {
                targetY--;
            }

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

        // 7. Supprimer le bloc d'or (marqueur) — uniquement pour le placement manuel
        if (removeMarker) {
            level.setBlock(center, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        // 8. Spawner l'indicateur de nom flottant
        spawnNameIndicator(level, center, villageName);

        // 9. Enregistrer dans VillageManager
        VillageManager.addVillage(level, village);
        return Optional.of(village);
    }

    // ── Sélection ─────────────────────────────────────────────────────────────

    /**
     * Sélectionne les bâtiments à construire pour la génération initiale :
     * <ol>
     *   <li>Tous les {@code required_building_ids} (CENTER inclus) — toujours présents.</li>
     *   <li>Entre {@code min_starter_buildings} et {@code max_starter_buildings} bâtiments
     *       tirés avec remise dans {@code optional_building_ids}.</li>
     * </ol>
     */
    private static List<BuildingType> selectBuildings(Culture culture, VillageType vt, Random rng) {
        List<BuildingType> result = new ArrayList<>();

        // 1. Townhall — bâtiment central, toujours placé en premier
        if (!vt.townhallId().isEmpty()) {
            culture.getBuildingType(vt.townhallId()).ifPresent(result::add);
        }

        // 2. Required — toujours tous présents
        for (String id : vt.requiredBuildingIds()) {
            culture.getBuildingType(id).ifPresent(result::add);
        }

        // 2. Optional starters — pioche avec remise entre min et max
        List<String> optPool = vt.optionalBuildingIds();
        if (!optPool.isEmpty() && vt.maxStarterBuildings() > 0) {
            int count = vt.minStarterBuildings();
            if (vt.maxStarterBuildings() > vt.minStarterBuildings()) {
                count += rng.nextInt(vt.maxStarterBuildings() - vt.minStarterBuildings() + 1);
            }
            for (int i = 0; i < count; i++) {
                String id = optPool.get(rng.nextInt(optPool.size()));
                culture.getBuildingType(id).ifPresent(result::add);
            }
        }

        return result;
    }

    // ── Recherche de position XZ ──────────────────────────────────────────────

    /**
     * Retourne une position XZ valide (Y=0) ou null si aucune n'est trouvée.
     *
     * <p>Pour CENTER : centré sur goldPos. Pour les autres rôles : spirale concentrique
     * dans la zone nominale [{@code minDistanceFactor × vs}, {@code maxDistanceFactor × vs}],
     * avec escalade progressive si cette zone est pleine.</p>
     *
     * <h3>Escalade à 3 niveaux</h3>
     * <ol>
     *   <li>Zone nominale : [minFactor × vs, maxFactor × vs]</li>
     *   <li>Expansion vers le bord : [maxFactor × vs, vs]</li>
     *   <li>Dernier recours : tout le village [0, vs]</li>
     * </ol>
     */
    private static BlockPos findPositionXZ(BlockPos origin,
                                            BuildingType bt,
                                            Vec3i size,
                                            List<PlacedBuilding> placed, Random rng,
                                            ServerLevel level) {
        if (bt.role() == BuildingType.BuildingRole.CENTER) {
            // Centrer la structure sur goldPos (pas le coin au bloc d'or)
            int cx = origin.getX() - size.getX() / 2;
            int cz = origin.getZ() - size.getZ() / 2;
            return new BlockPos(cx, 0, cz);
        }

        int vs      = VillageConfig.villageSize;
        int spacing = VillageConfig.buildingSpacing;

        int minRadius = Math.max(0, (int)(bt.minDistanceFactor() * vs));
        int maxRadius = Math.min(vs, (int)(bt.maxDistanceFactor() * vs));

        // Escalade niveau 1 : zone nominale du bâtiment
        BlockPos result = findPositionXZInZone(origin, minRadius, maxRadius, size, placed, spacing, rng, level);

        // Escalade niveau 2 : expansion vers le bord du village
        if (result == null && maxRadius < vs) {
            MillenaireNewAge.LOGGER.warn("[MNA] Zone nominale pleine pour '{}', escalade vers le bord.", bt.id());
            result = findPositionXZInZone(origin, maxRadius, vs, size, placed, spacing, rng, level);
        }

        // Escalade niveau 3 : tout le village en dernier recours
        if (result == null && minRadius > 0) {
            MillenaireNewAge.LOGGER.warn("[MNA] Dernier recours — zone complète pour '{}'.", bt.id());
            result = findPositionXZInZone(origin, 0, vs, size, placed, spacing, rng, level);
        }

        return result;
    }

    /**
     * Recherche en spirale concentrique une position XZ valide dans la zone [minRadius, maxRadius].
     *
     * <p>Parcourt les rayons de minRadius à maxRadius par paliers de {@code RADIUS_STEP},
     * et pour chaque rayon teste {@code ANGLE_STEPS} directions réparties uniformément.
     * Un décalage angulaire aléatoire garantit des layouts variés d'une génération à l'autre.</p>
     */
    private static BlockPos findPositionXZInZone(BlockPos origin,
                                                  int minRadius, int maxRadius,
                                                  Vec3i size, List<PlacedBuilding> placed,
                                                  int spacing, Random rng, ServerLevel level) {
        final int ANGLE_STEPS = 24;   // 15° par step = bonne couverture angulaire
        final int RADIUS_STEP = 3;    // avance de 3 blocs par anneau
        double angleOffset = rng.nextDouble() * 2 * Math.PI; // décalage aléatoire = layouts variés

        for (int radius = minRadius; radius <= maxRadius; radius += RADIUS_STEP) {
            for (int i = 0; i < ANGLE_STEPS; i++) {
                double angle = angleOffset + (2.0 * Math.PI * i / ANGLE_STEPS);
                int dx = (int) (Math.cos(angle) * radius);
                int dz = (int) (Math.sin(angle) * radius);
                BlockPos candidate = new BlockPos(origin.getX() + dx, 0, origin.getZ() + dz);

                if (!overlapsAny(candidate, size, placed, spacing)
                        && !hasDangerousBlock(level,
                            candidate.getX() - DANGER_RADIUS,
                            candidate.getZ() - DANGER_RADIUS,
                            candidate.getX() + size.getX() + DANGER_RADIUS,
                            candidate.getZ() + size.getZ() + DANGER_RADIUS)
                        && isTerrainFlat(level, candidate, size)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Échantillonne 5 points (4 coins + centre) et rejette si la variance de hauteur
     * dépasse {@link #MAX_TERRAIN_VARIANCE}.
     */
    private static boolean isTerrainFlat(ServerLevel level, BlockPos candidate, Vec3i size) {
        int[] samples = {
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX(), candidate.getZ()),
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX() + size.getX() - 1, candidate.getZ()),
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX(), candidate.getZ() + size.getZ() - 1),
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX() + size.getX() - 1, candidate.getZ() + size.getZ() - 1),
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX() + size.getX() / 2, candidate.getZ() + size.getZ() / 2),
        };
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int h : samples) { min = Math.min(min, h); max = Math.max(max, h); }
        return (max - min) <= MAX_TERRAIN_VARIANCE;
    }

    private static boolean overlapsAny(BlockPos pos, Vec3i size,
                                        List<PlacedBuilding> placed, int minSpacing) {
        int x1 = pos.getX();
        int z1 = pos.getZ();
        int w1 = size.getX();
        int d1 = size.getZ();

        for (PlacedBuilding pb : placed) {
            // Deux rectangles (A et B) se chevauchent si :
            // A.minX < B.maxX AND A.maxX > B.minX
            // On ajoute minSpacing à la "boîte" existante pour garantir l'écart.
            boolean xOverlap = x1 < (pb.x() + pb.sizeX() + minSpacing) 
                            && (x1 + w1 + minSpacing) > pb.x();
            boolean zOverlap = z1 < (pb.z() + pb.sizeZ() + minSpacing) 
                            && (z1 + d1 + minSpacing) > pb.z();
            
            if (xOverlap && zOverlap) return true;
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

    /**
     * Génère un nom de village unique pour cette culture dans ce monde.
     *
     * <p>Tire un nom au hasard depuis la langue de la culture. Si tous les noms
     * du pool sont déjà pris, recommence une passe en ajoutant le suffixe " 2",
     * puis " 3", etc.</p>
     */
    private static String generateVillageName(Culture culture, ServerLevel level) {
        List<String> pool = culture.language().villageNames();
        if (pool.isEmpty()) {
            return Component.translatable("culture.millenaire-new-age." + culture.id())
                .getString() + " Village";
        }

        // Noms déjà utilisés par cette culture dans ce monde
        Set<String> used = new HashSet<>();
        for (Village v : VillageManager.getAllVillages(level)) {
            if (v.getCultureId().equals(culture.id())) {
                used.add(v.getName());
            }
        }

        // Essai par passes : passe 1 = noms bruts, passe 2 = "Nom 2", passe 3 = "Nom 3"…
        List<String> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);

        for (int pass = 1; pass <= 100; pass++) {
            for (String base : shuffled) {
                String candidate = (pass == 1) ? base : base + " " + pass;
                if (!used.contains(candidate)) {
                    return candidate;
                }
            }
        }

        // Fallback impossible en pratique (100 passes × taille du pool)
        return pool.getFirst() + " " + (used.size() + 1);
    }

    // ── Vérification de danger ────────────────────────────────────────────────

    /** Fenêtre verticale (blocs) au-dessus et en dessous de la surface pour la détection de danger. */
    private static final int DANGER_Y_RANGE = 4;

    /**
     * Retourne {@code true} si un bloc dangereux est présent dans la zone XZ
     * {@code [x0..x1] × [z0..z1]}, dans une fenêtre de ±{@link #DANGER_Y_RANGE} blocs
     * autour de la surface du terrain (lave en surface uniquement, pas en cave).
     */
    private static boolean hasDangerousBlock(ServerLevel level,
                                              int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                int yMin = Math.max(level.getMinY(), surface - DANGER_Y_RANGE);
                int yMax = surface + DANGER_Y_RANGE;
                for (int y = yMin; y <= yMax; y++) {
                    if (DANGER_BLOCKS.contains(
                            level.getBlockState(new BlockPos(x, y, z)).getBlock())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ── Téléportation ───────────────────────────────────────────────────

    /**
     * Calcule une position sûre au nord du bâtiment central pour téléporter le joueur.
     *
     * @param server    serveur pour charger le template
     * @param level     monde du village
     * @param culture   culture pour trouver le bâtiment CENTER
     * @param vt        type de village
     * @param goldPos   position du bloc d'or (centre du village)
     * @return position sûre au sol, ou goldPos si aucun bâtiment CENTER trouvé
     */
    public static BlockPos findSafeTeleportPos(MinecraftServer server, ServerLevel level,
                                                Culture culture, VillageType vt,
                                                BlockPos goldPos) {
        Optional<BuildingType> centerOpt = vt.townhallId().isEmpty()
            ? Optional.empty()
            : culture.getBuildingType(vt.townhallId());

        if (centerOpt.isEmpty()) return goldPos;

        Vec3i size = getTemplateSize(server, centerOpt.get().structureId());
        if (size == null) return goldPos;

        // Position au nord du bâtiment : centre X, décalé en Z négatif
        int tpX = goldPos.getX();
        int tpZ = goldPos.getZ() - size.getZ() / 2 - TERRAIN_PADDING - 1;
        int tpY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tpX, tpZ);

        return new BlockPos(tpX, tpY, tpZ);
    }

}
