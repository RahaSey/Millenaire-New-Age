package com.mat37dev.creator;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mat37dev.MillenaireNewAge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Sauvegarde et chargement des structures créateur.
 *
 * <p>Chemin de sortie : {@code mods/MillenaireNewAge/creator_structures/<id>.nbt}
 * et {@code <id>_blocks.json} (liste des positions relatives non-air pour le preview).</p>
 *
 * <p>Génère aussi {@code mods/MillenaireNewAge/lang_additions.json} avec les clés de traduction.</p>
 */
public class StructureSaveManager {

    private static final com.google.gson.Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    // ── Sauvegarde ───────────────────────────────────────────────────────────

    /**
     * Sauvegarde la sélection active comme structure NBT.
     *
     * @param server      serveur courant
     * @param level       dimension courante
     * @param session     session du joueur (contient pos1/pos2)
     * @param structureId ex: "normans/house_t1"
     * @return chemin absolu du fichier .nbt créé
     * @throws IOException en cas d'erreur d'écriture
     */
    public static Path saveStructure(MinecraftServer server, ServerLevel level,
                                     CreatorSession session, String structureId) throws IOException {
        if (!session.hasSelection()) {
            throw new IllegalStateException("Aucune sélection active.");
        }

        BlockPos min  = session.getMinPos();
        Vec3i    size = session.getSize();

        // 1. Capturer les blocs dans un StructureTemplate
        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, min, size, false, List.of());

        // 2. Sérialiser en NBT
        CompoundTag nbt = template.save(new CompoundTag());

        // 3. Préparer les chemins
        String sanitized = structureId.replace(':', '/');
        Path outputDir   = creatorOutputDir().resolve("creator_structures");
        Path nbtPath     = outputDir.resolve(sanitized + ".nbt");
        Path blocksPath  = outputDir.resolve(sanitized + "_blocks.json");

        Files.createDirectories(nbtPath.getParent());

        // 4. Écrire le .nbt
        NbtIo.writeCompressed(nbt, nbtPath);

        // 5. Écrire la liste de positions non-air pour le preview client
        List<BlockPos> blockPositions = captureBlockPositions(level, min, size);
        saveBlockPositions(blocksPath, blockPositions, size);

        // 6. Mettre à jour lang_additions.json
        updateLangAdditions(server, structureId);

        MillenaireNewAge.LOGGER.info("Structure '{}' sauvegardée → {}", structureId, nbtPath);
        return nbtPath;
    }

    // ── Chargement ───────────────────────────────────────────────────────────

    /**
     * Charge le StructureTemplate depuis le dossier creator ou les ressources du mod.
     */
    public static StructureTemplate loadTemplate(MinecraftServer server, String structureId) {
        String sanitized = structureId.replace(':', '/');
        Path nbtPath = creatorOutputDir().resolve("creator_structures/" + sanitized + ".nbt");

        // 1. Priorité au fichier local dans mods/MillenaireNewAge (travail en cours)
        try {
            if (Files.exists(nbtPath)) {
                CompoundTag nbt = NbtIo.readCompressed(nbtPath, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                StructureTemplate template = new StructureTemplate();
                template.load(server.registryAccess().lookupOrThrow(Registries.BLOCK), nbt);
                return template;
            }
        } catch (IOException e) {
            MillenaireNewAge.LOGGER.error("Impossible de charger la structure locale '{}' : {}", structureId, e.getMessage());
        }

        // 2. Essayer de charger directement depuis src/main/resources (dev mode helper)
        String relPath = "data/" + MillenaireNewAge.MOD_ID + "/structure/" + sanitized + ".nbt";
        Path resourcesPath = Path.of("src/main/resources").resolve(relPath);
        
        // Si non trouvé, on tente ../src (cas où on est dans le dossier /run)
        if (!Files.exists(resourcesPath)) {
            resourcesPath = Path.of("../src/main/resources").resolve(relPath);
        }

        if (Files.exists(resourcesPath)) {
            try {
                CompoundTag nbt = NbtIo.readCompressed(resourcesPath, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                StructureTemplate template = new StructureTemplate();
                template.load(server.registryAccess().lookupOrThrow(Registries.BLOCK), nbt);
                return template;
            } catch (IOException e) {
                MillenaireNewAge.LOGGER.error("Erreur chargement direct resources '{}' : {}", structureId, e.getMessage());
            }
        }

        // 3. Fallback aux ressources du mod via le StructureManager (prod mode)
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
            MillenaireNewAge.MOD_ID, sanitized);

        return server.getStructureManager().get(rl).orElse(null);
    }

    /**
     * Charge la liste des positions relatives pour le preview client.
     */
    public static List<BlockPos> loadBlockPositions(MinecraftServer server, String structureId) {
        String sanitized = structureId.replace(':', '/');
        Path blocksPath = creatorOutputDir().resolve("creator_structures/" + sanitized + "_blocks.json");

        // 1. Si le JSON existe, on l'utilise (plus rapide)
        if (Files.exists(blocksPath)) {
            try {
                String json = Files.readString(blocksPath);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                JsonArray arr = root.getAsJsonArray("blocks");
                List<BlockPos> result = new ArrayList<>(arr.size());
                for (var elem : arr) {
                    JsonObject o = elem.getAsJsonObject();
                    result.add(new BlockPos(
                        o.get("x").getAsInt(),
                        o.get("y").getAsInt(),
                        o.get("z").getAsInt()
                    ));
                }
                return result;
            } catch (Exception e) {
                MillenaireNewAge.LOGGER.error("Erreur lecture JSON preview pour '{}' : {}", structureId, e.getMessage());
            }
        }

        // 2. Sinon, on extrait les positions depuis le NBT directement
        StructureTemplate template = loadTemplate(server, structureId);
        if (template != null) {
            List<BlockPos> result = new ArrayList<>();
            // On utilise l'accesseur pour accéder à la première palette
            var palettes = ((com.mat37dev.mixin.StructureTemplateAccessor) template).getPalettes();
            if (!palettes.isEmpty()) {
                for (StructureTemplate.StructureBlockInfo info : palettes.getFirst().blocks()) {
                    if (!info.state().isAir()) {
                        result.add(info.pos());
                    }
                }
            }
            return result;
        }

        return List.of();
    }

    /**
     * Liste toutes les structures disponibles dans le dossier creator ET dans les ressources du mod.
     */
    public static List<String> listStructures(MinecraftServer server) {
        java.util.Set<String> ids = new java.util.TreeSet<>();

        // 1. Scanner le dossier MillenaireNewAge
        Path structuresDir = creatorOutputDir().resolve("creator_structures");
        if (Files.exists(structuresDir)) {
            try (var stream = Files.walk(structuresDir)) {
                stream.filter(p -> p.toString().endsWith(".nbt"))
                    .forEach(p -> {
                        String relative = structuresDir.relativize(p).toString()
                            .replace('\\', '/')
                            .replace(".nbt", "");
                        ids.add(relative);
                    });
            } catch (IOException e) {
                MillenaireNewAge.LOGGER.error("Erreur listing structures locales : {}", e.getMessage());
            }
        }

        // 2. Scanner les ressources du mod (datapack interne)
        server.getResourceManager().listResources("structure", rl ->
            rl.getNamespace().equals(MillenaireNewAge.MOD_ID) && rl.getPath().endsWith(".nbt")
        ).forEach((rl, resource) -> {
            String path = rl.getPath();
            String id = path.substring("structure/".length(), path.length() - ".nbt".length());
            ids.add(id);
        });

        return new ArrayList<>(ids);
    }

    // ── Helpers internes ─────────────────────────────────────────────────────

    private static List<BlockPos> captureBlockPositions(ServerLevel level, BlockPos min, Vec3i size) {
        List<BlockPos> result = new ArrayList<>();
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos world = min.offset(x, y, z);
                    if (!level.getBlockState(world).isAir()) {
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return result;
    }

    private static void saveBlockPositions(Path path, List<BlockPos> positions, Vec3i size) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("size_x", size.getX());
        root.addProperty("size_y", size.getY());
        root.addProperty("size_z", size.getZ());

        JsonArray arr = new JsonArray();
        for (BlockPos pos : positions) {
            JsonObject o = new JsonObject();
            o.addProperty("x", pos.getX());
            o.addProperty("y", pos.getY());
            o.addProperty("z", pos.getZ());
            arr.add(o);
        }
        root.add("blocks", arr);
        Files.writeString(path, GSON.toJson(root));
    }

    private static void updateLangAdditions(MinecraftServer server, String structureId)
            throws IOException {
        Path langPath = creatorOutputDir().resolve("lang_additions.json");

        JsonObject root;
        if (Files.exists(langPath)) {
            root = JsonParser.parseString(Files.readString(langPath)).getAsJsonObject();
        } else {
            root = new JsonObject();
        }

        if (!root.has("en_us")) root.add("en_us", new JsonObject());
        if (!root.has("fr_fr")) root.add("fr_fr", new JsonObject());

        String key     = "structure.millenaire-new-age." + structureId.replace('/', '.').replace(':', '.');
        String enName  = autoName(structureId, false);
        String frName  = autoName(structureId, true);

        JsonObject en = root.getAsJsonObject("en_us");
        JsonObject fr = root.getAsJsonObject("fr_fr");

        if (!en.has(key)) en.addProperty(key, enName);
        if (!fr.has(key)) fr.addProperty(key, frName);

        Files.writeString(langPath, GSON.toJson(root));
    }

    /**
     * Génère un nom lisible depuis l'ID de structure.
     * ex: "normans/house_t1" → "House (Tier 1)" / "Maison (Niveau 1)"
     */
    public static String autoName(String structureId, boolean french) {
        String basePart = structureId.contains("/")
            ? structureId.substring(structureId.lastIndexOf('/') + 1)
            : structureId;

        String[] parts = basePart.split("_");
        StringBuilder name = new StringBuilder();
        int tier = -1;

        for (String part : parts) {
            if (part.matches("t\\d+")) {
                tier = Integer.parseInt(part.substring(1));
                continue;
            }
            if (!name.isEmpty()) name.append(' ');
            name.append(Character.toUpperCase(part.charAt(0)));
            name.append(part.substring(1));
        }

        if (tier > 0) {
            name.append(french ? " (Niveau " : " (Tier ").append(tier).append(')');
        }

        // Préfixe culture si présent
        if (structureId.contains("/")) {
            String culture = structureId.substring(0, structureId.lastIndexOf('/'));
            String cultureName = Character.toUpperCase(culture.charAt(0)) + culture.substring(1);
            name.insert(0, cultureName + " ");
        }

        return name.toString();
    }

    public static Path creatorOutputDir() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir()
            .resolve("mods/MillenaireNewAge");
    }

    // Placement des structures ─────────────────────────────────────────────

    public static boolean placeStructure(MinecraftServer server, ServerLevel level,
                                         String structureId, BlockPos origin,
                                         net.minecraft.world.level.block.Mirror mirror,
                                         net.minecraft.world.level.block.Rotation rotation) {
        StructureTemplate template = loadTemplate(server, structureId);
        if (template == null) return false;

        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setMirror(mirror)
            .setRotation(rotation);

        template.placeInWorld(level, origin, origin, settings, level.random, 2);
        return true;
    }
}
