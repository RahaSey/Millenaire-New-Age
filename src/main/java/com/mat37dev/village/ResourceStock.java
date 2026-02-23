package com.mat37dev.village;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stock de ressources agrégé par catégorie (nourriture, bois, pierre…).
 * Pas de tracking item par item — on travaille en quantités abstraites.
 */
public class ResourceStock {

    private final Map<String, Integer> stock = new HashMap<>();

    public int get(String category) {
        return stock.getOrDefault(category, 0);
    }

    public void set(String category, int amount) {
        stock.put(category, Math.max(0, amount));
    }

    public void add(String category, int amount) {
        set(category, get(category) + amount);
    }

    /** @return true si la consommation a pu être effectuée */
    public boolean consume(String category, int amount) {
        int current = get(category);
        if (current < amount) return false;
        set(category, current - amount);
        return true;
    }

    public Set<String> categories() {
        return stock.keySet();
    }

    // ── Sérialisation (MC 1.21.10 ValueInput/ValueOutput) ────────────────────

    public void writeTo(ValueOutput output) {
        stock.forEach(output::putInt);
    }

    public void readFrom(ValueInput input) {
        stock.clear();
        // keys() est fourni par FabricReadView, injecté sur ValueInput via Mixin
        input.keys().forEach(key -> input.getInt(key).ifPresent(v -> stock.put(key, v)));
    }
}
