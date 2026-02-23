package com.mat37dev.civilization;

import com.mat37dev.MillenaireNewAge;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CivilizationRegistry {

    private static final Map<String, Civilization> REGISTRY = new LinkedHashMap<>();

    private CivilizationRegistry() {}

    public static void register(Civilization civilization) {
        if (REGISTRY.containsKey(civilization.id())) {
            MillenaireNewAge.LOGGER.warn("Civilization '{}' is being overwritten.", civilization.id());
        }
        REGISTRY.put(civilization.id(), civilization);
        MillenaireNewAge.LOGGER.debug("Registered civilization: {}", civilization.id());
    }

    public static Optional<Civilization> get(String id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static Collection<Civilization> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static void clear() {
        REGISTRY.clear();
    }

    public static int size() {
        return REGISTRY.size();
    }
}
