package com.mat37dev.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mat37dev.MillenaireNewAge;
import com.mat37dev.creator.StructureSaveManager;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration du système de villages.
 * Chargée au démarrage du serveur depuis {@code mods/MillenaireNewAge/config/village_config.json}.
 * Créée avec les valeurs par défaut si absente.
 */
public class VillageConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static int villageSize             = 90;
    public static int villageSpacing          = 100;
    public static int maxVillagersPerVillage  = 20;
    public static int buildingSpacing         = 2;

    public static void load(MinecraftServer server) {
        Path configPath = StructureSaveManager.creatorOutputDir().resolve("config/village_config.json");
        try {
            Files.createDirectories(configPath.getParent());
            if (Files.exists(configPath)) {
                ConfigData data = GSON.fromJson(Files.readString(configPath), ConfigData.class);
                villageSize            = data.villageSize;
                villageSpacing         = data.villageSpacing;
                maxVillagersPerVillage = data.maxVillagersPerVillage;
                buildingSpacing        = data.buildingSpacing;
                MillenaireNewAge.LOGGER.info("[MNA] VillageConfig chargé depuis {}", configPath);
            } else {
                save(configPath);
                MillenaireNewAge.LOGGER.info("[MNA] VillageConfig créé avec les valeurs par défaut : {}", configPath);
            }
        } catch (IOException e) {
            MillenaireNewAge.LOGGER.error("[MNA] Erreur chargement VillageConfig : {}", e.getMessage());
        }
    }

    private static void save(Path path) throws IOException {
        ConfigData data = new ConfigData();
        data.villageSize            = villageSize;
        data.villageSpacing         = villageSpacing;
        data.maxVillagersPerVillage = maxVillagersPerVillage;
        data.buildingSpacing        = buildingSpacing;
        Files.writeString(path, GSON.toJson(data));
    }

    private static class ConfigData {
        int villageSize            = 90;
        int villageSpacing         = 100;
        int maxVillagersPerVillage = 20;
        int buildingSpacing        = 2;
    }
}
