package com.mat37dev.mixin;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Expose le champ privé {@code palettes} de {@link StructureTemplate}.
 * Déplacé dans le package commun pour être accessible au serveur (sauvegarde)
 * et au client (preview).
 */
@Mixin(StructureTemplate.class)
public interface StructureTemplateAccessor {

    @Accessor("palettes")
    List<StructureTemplate.Palette> getPalettes();
}
