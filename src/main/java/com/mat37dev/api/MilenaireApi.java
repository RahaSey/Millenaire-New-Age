package com.mat37dev.api;

import com.mat37dev.culture.Culture;
import com.mat37dev.data.CultureLoader;

/**
 * API publique de Millenaire: New Age.
 * <p>
 * Destinée aux mods compagnons qui souhaitent enregistrer du contenu
 * programmatiquement (ex: une culture avec des comportements Java custom).
 *
 * <p>Usage dans le mod compagnon :</p>
 * <pre>{@code
 * // Dans ModInitializer.onInitialize() :
 * MilenaireApi.registerCulture(new Culture("vikings", ...));
 * }</pre>
 *
 * <p>Note : pour une culture sans code Java custom (blocs/items génériques),
 * préférez un datapack embarqué dans votre JAR — c'est plus simple.</p>
 */
public final class MilenaireApi {

    private MilenaireApi() {}

    /**
     * Enregistre une culture programmatiquement.
     *
     * <p>La culture persiste à travers les reloads de datapacks.
     * En cas de conflit d'ID avec un JSON, le JSON a la priorité.</p>
     *
     * @param culture la culture à enregistrer
     */
    public static void registerCulture(Culture culture) {
        CultureLoader.addProgrammatic(culture);
    }
}
