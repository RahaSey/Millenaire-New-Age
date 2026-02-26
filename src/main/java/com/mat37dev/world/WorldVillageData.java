package com.mat37dev.world;

import com.mat37dev.village.Village;
import org.ladysnake.cca.api.v3.component.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Composant Cardinal Components attaché au Level (monde).
 * Contient la liste de tous les villages persistés dans ce monde.
 *
 * <p>La clé d'accès est dans {@link VillageComponents#KEY}.</p>
 */
public interface WorldVillageData extends Component {

    void addVillage(Village village);

    Optional<Village> getVillage(UUID id);

    Collection<Village> getAllVillages();

    void removeVillage(UUID id);

    /**
     * Indique si ce chunk a déjà fait l'objet d'une tentative de génération de village.
     * Utiliser {@link net.minecraft.world.level.ChunkPos#toLong()} comme clé.
     */
    boolean hasTriedChunk(long chunkKey);

    /** Marque le chunk comme tenté (appeler avant la tentative de placement). */
    void markChunkTried(long chunkKey);
}
