package com.mat37dev.world;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.config.VillageConfig;
import com.mat37dev.culture.Culture;
import com.mat37dev.culture.CultureRegistry;
import com.mat37dev.culture.VillageType;
import com.mat37dev.village.VillagePlacer;
import com.mat37dev.village.VillageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Génération naturelle des villages lors du chargement de chunks.
 *
 * <h3>Architecture deux phases</h3>
 * <ol>
 *   <li>{@link #onChunkLoad} — ultra-léger : filtre probabiliste + ajout en file.</li>
 *   <li>{@link #processTick} — 1/tick : sélection biome/culture/type, vérification terrain
 *       et distance, puis placement.</li>
 * </ol>
 *
 * <h3>Note sur les chunks voisins</h3>
 * <p>OldSource (Forge) déclenchait la génération PENDANT la création du chunk, Forge garantissant
 * que les chunks voisins étaient disponibles. Fabric/CHUNK_LOAD ne le garantit pas.
 * En pratique, {@code ServerLevel.getHeight()} sur le serveur accède aux heightmaps disponibles
 * même pour des chunks non encore au niveau FULL — les structures se placent correctement
 * dans la zone explorée par le joueur.</p>
 */
public class VillageGenerator {

    /** Variance de hauteur maximale (blocs) pour le pré-filtre terrain. */
    private static final int MAX_HEIGHT_VARIANCE = 8;

    /**
     * Nombre maximum de fois qu'on remet en file un chunk en attendant qu'un joueur soit proche.
     * À 1 tick par tentative, MAX_RETRIES=400 = ~20 secondes d'attente max.
     */
    private static final int MAX_RETRIES = 400;

    private static final Queue<PendingChunk> QUEUE = new ConcurrentLinkedQueue<>();

    private record PendingChunk(ServerLevel level, ChunkPos pos, int retries) {}

    private VillageGenerator() {}

    // ── Phase 1 : hook léger ──────────────────────────────────────────────────

    /**
     * Appelé à chaque chargement de chunk ({@code ServerChunkEvents.CHUNK_LOAD}).
     * <b>Doit rester ultra-léger</b> — aucune opération bloc ici.
     */
    public static void onChunkLoad(ServerLevel level, ChunkPos chunkPos) {
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        WorldVillageData data = VillageManager.getData(level);
        long key = chunkPos.toLong();

        if (data.hasTriedChunk(key)) return;

        if (new Random().nextInt(VillageConfig.generationChance) != 0) return;

        // Marquer avant de mettre en file (évite doublons si le chunk recharge vite)
        data.markChunkTried(key);

        QUEUE.add(new PendingChunk(level, chunkPos, 0));
        MillenaireNewAge.LOGGER.debug("[MNA] Chunk {}/{} en file ({} en attente).",
            chunkPos.x, chunkPos.z, QUEUE.size());
    }

    // ── Phase 2 : traitement différé, un par tick ─────────────────────────────

    /**
     * Appelé une fois par tick serveur ({@code ServerTickEvents.END_SERVER_TICK}).
     * Traite UN chunk de la file — garantit 0 impact perceptible sur le TPS.
     */
    public static void processTick(MinecraftServer server) {
        PendingChunk task = QUEUE.poll();
        if (task == null) return;

        ServerLevel level = task.level();
        if (!server.levelKeys().contains(level.dimension())) return;

        // Les chunks du périmètre (villageSize blocs du centre) doivent être chargés pour que
        // TerrainAdapter calcule des hauteurs correctes. On vérifie directement leur état.
        int cx = task.pos().x * 16 + 8;
        int cz = task.pos().z * 16 + 8;

        if (!perimeterChunksLoaded(level, cx, cz)) {
            if (task.retries() < MAX_RETRIES) {
                QUEUE.add(new PendingChunk(level, task.pos(), task.retries() + 1));
            }
            return;
        }

        attemptGeneration(level, task.pos());
    }

    // ── Logique de génération ─────────────────────────────────────────────────

    private static void attemptGeneration(ServerLevel level, ChunkPos chunkPos) {
        int centerX = chunkPos.x * 16 + 8;
        int centerZ = chunkPos.z * 16 + 8;
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ);
        BlockPos center = new BlockPos(centerX, surfaceY, centerZ);

        String biomeKey = getBiomeKey(level, center);

        List<Culture> compatible = getCompatibleCultures(biomeKey);
        if (compatible.isEmpty()) return;

        Random rng = new Random();
        Culture culture = compatible.get(rng.nextInt(compatible.size()));

        Optional<VillageType> vtOpt = culture.selectRandomVillageType(rng);
        if (vtOpt.isEmpty()) return;

        if (!isTerrainSuitable(level, center)) {
            MillenaireNewAge.LOGGER.debug("[MNA] Chunk {}/{} : terrain trop accidenté, skip.",
                chunkPos.x, chunkPos.z);
            return;
        }

        if (VillagePlacer.checkSpacing(level, center) != null) {
            MillenaireNewAge.LOGGER.debug("[MNA] Chunk {}/{} : trop proche d'un village existant, skip.",
                chunkPos.x, chunkPos.z);
            return;
        }

        MillenaireNewAge.LOGGER.info("[MNA] Génération naturelle : {} ({}) biome='{}' pos={}",
            vtOpt.get().id(), culture.id(), biomeKey, center.toShortString());

        VillagePlacer.placeVillageAt(level.getServer(), level,
            culture.id(), vtOpt.get().id(), center);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String getBiomeKey(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        return biome.unwrapKey()
            .map(k -> k.location().toString())
            .orElse("");
    }

    private static List<Culture> getCompatibleCultures(String biomeKey) {
        List<Culture> result = new ArrayList<>();
        for (Culture culture : CultureRegistry.getAll()) {
            if (culture.compatibleBiomes().contains(biomeKey)) {
                result.add(culture);
            }
        }
        return result;
    }

    /**
     * Vérifie que les 4 chunks aux coins du périmètre du village sont chargés.
     *
     * <p>Utilise {@code getChunkNow} qui retourne {@code null} sans forcer de chargement.
     * Avec une view distance élevée, ces chunks sont chargés même si le joueur est loin
     * du centre — ce qui permet une génération plus naturelle.</p>
     */
    private static boolean perimeterChunksLoaded(ServerLevel level, int cx, int cz) {
        int r = VillageConfig.villageSize;
        int[][] corners = {
            {cx - r, cz - r}, {cx + r, cz - r},
            {cx - r, cz + r}, {cx + r, cz + r}
        };
        for (int[] corner : corners) {
            int chunkX = corner[0] >> 4;
            int chunkZ = corner[1] >> 4;
            if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTerrainSuitable(ServerLevel level, BlockPos center) {
        int x = center.getX();
        int z = center.getZ();
        int[] heights = {
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x - 8, z - 8),
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + 8, z - 8),
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x - 8, z + 8),
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + 8, z + 8),
            center.getY(),
        };
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int h : heights) { min = Math.min(min, h); max = Math.max(max, h); }
        return (max - min) <= MAX_HEIGHT_VARIANCE;
    }
}
