package com.mat37dev.culture;

import com.mat37dev.MillenaireNewAge;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CultureRegistry {

    private static final Map<String, Culture> REGISTRY = new LinkedHashMap<>();

    private CultureRegistry() {}

    public static void register(Culture culture) {
        if (REGISTRY.containsKey(culture.id())) {
            MillenaireNewAge.LOGGER.warn("Culture '{}' is being overwritten.", culture.id());
        }
        REGISTRY.put(culture.id(), culture);
        MillenaireNewAge.LOGGER.debug("Registered culture: {}", culture.id());
    }

    public static Optional<Culture> get(String id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static Collection<Culture> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static void clear() {
        REGISTRY.clear();
    }

    public static int size() {
        return REGISTRY.size();
    }
}
