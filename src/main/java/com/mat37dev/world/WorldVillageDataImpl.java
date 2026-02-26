package com.mat37dev.world;

import com.mat37dev.village.Village;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class WorldVillageDataImpl implements WorldVillageData {

    private final Map<UUID, Village> villages = new LinkedHashMap<>();

    /** Chunks où une tentative de génération naturelle a déjà eu lieu. */
    private final Set<Long> triedChunks = new HashSet<>();

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

    @Override
    public boolean hasTriedChunk(long chunkKey) {
        return triedChunks.contains(chunkKey);
    }

    @Override
    public void markChunkTried(long chunkKey) {
        triedChunks.add(chunkKey);
    }

    // ── CCA Component — sérialisation (MC 1.21.10) ───────────────────────────

    @Override
    public void readData(ValueInput input) {
        villages.clear();
        for (ValueInput entry : input.childrenListOrEmpty("villages")) {
            Village v = Village.readFrom(entry);
            villages.put(v.getId(), v);
        }
        triedChunks.clear();
        for (ValueInput entry : input.childrenListOrEmpty("tried_chunks")) {
            entry.getLong("k").ifPresent(triedChunks::add);
        }
    }

    @Override
    public void writeData(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("villages");
        for (Village v : villages.values()) {
            v.writeTo(list.addChild());
        }
        ValueOutput.ValueOutputList triedList = output.childrenList("tried_chunks");
        for (long key : triedChunks) {
            triedList.addChild().putLong("k", key);
        }
    }
}
