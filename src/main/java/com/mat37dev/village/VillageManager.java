package com.mat37dev.village;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.world.VillageComponents;
import com.mat37dev.world.WorldVillageData;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Point d'accès statique aux villages d'un monde.
 * Délègue à WorldVillageData (composant Cardinal Components attaché au ServerLevel).
 */
public class VillageManager {

    private VillageManager() {}

    public static WorldVillageData getData(ServerLevel level) {
        return VillageComponents.KEY.get(level);
    }

    public static void addVillage(ServerLevel level, Village village) {
        getData(level).addVillage(village);
        MillenaireNewAge.LOGGER.info("Village '{}' ({}) added to world.", village.getName(), village.getId());
    }

    public static Optional<Village> getVillage(ServerLevel level, UUID id) {
        return getData(level).getVillage(id);
    }

    /** Recherche un village par son nom (insensible à la casse). */
    public static Optional<Village> getVillageByName(ServerLevel level, String name) {
        return getData(level).getAllVillages().stream()
            .filter(v -> v.getName().equalsIgnoreCase(name))
            .findFirst();
    }

    public static Collection<Village> getAllVillages(ServerLevel level) {
        return getData(level).getAllVillages();
    }

    public static void removeVillage(ServerLevel level, UUID id) {
        getData(level).removeVillage(id);
    }

    /** Appelé au tick du ServerLevel pour mettre à jour tous les villages actifs. */
    public static void tick(ServerLevel level) {
        // Placeholder Phase 3 — hibernation + IA tick
    }
}
