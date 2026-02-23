package com.mat37dev.world;

import net.minecraft.resources.ResourceLocation;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

/**
 * Entrypoint Cardinal Components.
 * Enregistré dans fabric.mod.json sous la clé "cardinal-components".
 *
 * <p>La clé du composant DOIT être créée ici, à l'intérieur de
 * {@link #registerWorldComponentFactories}, car CCA 7.2.0 interdit
 * l'appel à {@code ComponentRegistryV3.INSTANCE.getOrCreate()} depuis
 * un initialiseur statique.</p>
 */
public class VillageComponents implements WorldComponentInitializer {

    /** Clé d'accès au composant village_data. Disponible après l'initialisation CCA. */
    public static ComponentKey<WorldVillageData> KEY;

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        KEY = ComponentRegistryV3.INSTANCE.getOrCreate(
                ResourceLocation.fromNamespaceAndPath("millenaire-new-age", "village_data"),
                WorldVillageData.class
        );
        registry.register(KEY, WorldVillageDataImpl::new);
    }
}
