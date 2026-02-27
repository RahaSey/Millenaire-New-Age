package com.mat37dev.entity.ai;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/**
 * Registre extensible des blocs considérés comme des lits par les villageois.
 *
 * <p>Par défaut, tous les lits vanilla (via le tag {@code #minecraft:beds}) sont inclus.
 * Les cultures peuvent enregistrer leurs propres blocs lit custom via {@link #register(Block)}.</p>
 *
 * <p>Exemple d'utilisation dans un mod compagnon :</p>
 * <pre>
 *   MillBedBlocks.register(MyMod.STRAW_BED);
 * </pre>
 */
public class MillBedBlocks {

    /** Blocs custom enregistrés explicitement (s'ajoute au tag vanilla). */
    private static final Set<Block> CUSTOM_BEDS = new HashSet<>();

    /**
     * Retourne {@code true} si le {@link BlockState} donné est considéré comme un lit.
     * Couvre les lits vanilla (tag {@code #minecraft:beds}) et les blocs enregistrés via {@link #register}.
     */
    public static boolean isBed(BlockState state) {
        if (state.is(BlockTags.BEDS)) return true;
        return CUSTOM_BEDS.contains(state.getBlock());
    }

    /**
     * Enregistre un bloc custom comme lit utilisable par les villageois.
     * À appeler pendant l'initialisation du mod (ex: dans {@code onInitialize()}).
     */
    public static void register(Block block) {
        CUSTOM_BEDS.add(block);
    }
}
