package com.mat37dev.world;

import com.mat37dev.village.Village;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WorldVillageDataImpl implements WorldVillageData {

    private final Map<UUID, Village> villages = new LinkedHashMap<>();

    public WorldVillageDataImpl(Level level) {
        // Le niveau est disponible pour usage futur (ex: tick, accès au monde)
    }

    @Override
    public void addVillage(Village village) {
        villages.put(village.getId(), village);
    }

    @Override
    public Optional<Village> getVillage(UUID id) {
        return Optional.ofNullable(villages.get(id));
    }

    @Override
    public Collection<Village> getAllVillages() {
        return Collections.unmodifiableCollection(villages.values());
    }

    @Override
    public void removeVillage(UUID id) {
        villages.remove(id);
    }

    // ── CCA Component — sérialisation (MC 1.21.10) ───────────────────────────

    @Override
    public void readData(ValueInput input) {
        villages.clear();
        for (ValueInput entry : input.childrenListOrEmpty("villages")) {
            Village v = Village.readFrom(entry);
            villages.put(v.getId(), v);
        }
    }

    @Override
    public void writeData(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("villages");
        for (Village v : villages.values()) {
            v.writeTo(list.addChild());
        }
    }
}
