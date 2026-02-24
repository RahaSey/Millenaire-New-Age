package com.mat37dev.api;

import com.mat37dev.civilization.Civilization;
import com.mat37dev.data.CivilizationLoader;

/**
 * API publique de Millenaire: New Age.
 * <p>
 * Destinée aux mods compagnons qui souhaitent enregistrer du contenu
 * programmatiquement (ex: une civilisation avec des comportements Java custom).
 *
 * <p>Usage dans le mod compagnon :</p>
 * <pre>{@code
 * // Dans ModInitializer.onInitialize() :
 * MilenaireApi.registerCivilization(new Civilization("vikings", ...));
 * }</pre>
 *
 * <p>Note : pour une civilisation sans code Java custom (blocs/items génériques),
 * préférez un datapack embarqué dans votre JAR — c'est plus simple.</p>
 */
public final class MilenaireApi {

    private MilenaireApi() {}

    /**
     * Enregistre une civilisation programmatiquement.
     *
     * <p>La civilisation persiste à travers les reloads de datapacks.
     * En cas de conflit d'ID avec un JSON, le JSON a la priorité.</p>
     *
     * @param civilization la civilisation à enregistrer
     */
    public static void registerCivilization(Civilization civilization) {
        CivilizationLoader.addProgrammatic(civilization);
    }
}
