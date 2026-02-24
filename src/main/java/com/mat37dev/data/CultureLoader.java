package com.mat37dev.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mat37dev.MillenaireNewAge;
import com.mat37dev.culture.Culture;
import com.mat37dev.culture.CultureRegistry;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Charge les cultures depuis les datapacks via Codec.
 *
 * <p>Emplacement des fichiers : {@code data/<namespace>/culture/<id>.json}</p>
 *
 * <p>Ordre de priorité :</p>
 * <ol>
 *   <li>Fichiers JSON (datapacks) — peuvent être overridés par d'autres datapacks</li>
 *   <li>Registrations programmatiques (MilenaireApi) — s'appliquent en dernier,
 *       sauf si un JSON existe déjà pour cet ID</li>
 * </ol>
 */
public class CultureLoader implements ResourceManagerReloadListener {

    private static final ResourceLocation LOADER_ID =
            ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "culture_loader");

    private static final String DATA_PATH = "culture";

    /**
     * Cultures enregistrées programmatiquement via {@link com.mat37dev.api.MilenaireApi}.
     * Survivent aux reloads de datapacks.
     */
    private static final Map<String, Culture> PROGRAMMATIC = new LinkedHashMap<>();

    // ── API interne (appelée par MilenaireApi) ───────────────────────────────

    public static void addProgrammatic(Culture culture) {
        PROGRAMMATIC.put(culture.id(), culture);
        MillenaireNewAge.LOGGER.debug("Culture programmatique enregistrée : {}", culture.id());
    }

    // ── Enregistrement ───────────────────────────────────────────────────────

    public static void register() {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloader(LOADER_ID, new CultureLoader());
    }

    // ── ResourceManagerReloadListener ────────────────────────────────────────

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        CultureRegistry.clear();

        // 1. Charger les fichiers JSON (datapacks — Niveaux 1 & 2)
        Map<ResourceLocation, Resource> resources = manager.listResources(
                DATA_PATH,
                id -> id.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                Culture.CODEC
                        .parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error ->
                                MillenaireNewAge.LOGGER.error("Erreur dans {} : {}", fileId, error))
                        .ifPresent(CultureRegistry::register);
            } catch (IOException e) {
                MillenaireNewAge.LOGGER.error("Impossible de lire {}", fileId, e);
            }
        }

        // 2. Appliquer les registrations programmatiques (Niveau 3)
        // Les JSON ont la priorité : on n'écrase que si l'ID n'existe pas déjà.
        PROGRAMMATIC.forEach((id, culture) -> {
            if (CultureRegistry.get(id).isEmpty()) {
                CultureRegistry.register(culture);
            }
        });

        MillenaireNewAge.LOGGER.info("{} culture(s) chargée(s).", CultureRegistry.size());
    }
}
