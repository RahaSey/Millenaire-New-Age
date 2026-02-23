package com.mat37dev.village;

import net.minecraft.resources.ResourceLocation;

/**
 * Référence légère à un template de structure NBT.
 * La logique de pose est gérée par StructurePlacer (Phase 3).
 */
public class MillStructureTemplate {

    private final ResourceLocation id;

    public MillStructureTemplate(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public String toString() {
        return "MillStructureTemplate{" + id + "}";
    }
}
