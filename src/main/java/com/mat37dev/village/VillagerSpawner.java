package com.mat37dev.village;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.config.VillageConfig;
import com.mat37dev.culture.BuildingType;
import com.mat37dev.culture.Culture;
import com.mat37dev.culture.VillagerTypeDef;
import com.mat37dev.culture.VillageType;
import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.init.MillEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Optional;
import java.util.Random;

/**
 * Spawne les villageois initiaux lors de la création d'un village.
 *
 * <h3>Logique d'attribution du domicile</h3>
 * <ol>
 *   <li><b>Priorité 1 :</b> le lieu de travail lui-même si {@code sleeping_capacity > 0}
 *       (garde → caserne, chef → mairie).</li>
 *   <li><b>Priorité 2 :</b> premier autre bâtiment résidentiel avec une place libre
 *       (bûcheron → maison du bûcheron).</li>
 * </ol>
 *
 * <h3>Couples</h3>
 * Chaque occupant masculin d'un bâtiment avec {@code sleeping_capacity >= 2} reçoit
 * automatiquement une compagne ({@code normans:housewife} ou équivalent féminin de la culture).
 *
 * <h3>Positions de spawn</h3>
 * Tous les villageois spawn au sol près du centre du village (positions décalées par index)
 * pour éviter de se retrouver sur les toits des bâtiments.
 */
public class VillagerSpawner {

    private VillagerSpawner() {}

    // Aucune constante de position globale — voir spawnPos() pour la logique de placement.

    // ── Point d'entrée ────────────────────────────────────────────────────────

    /**
     * Spawne les villageois du village selon les {@code villager_type_ids} du type de village.
     *
     * @param level   monde serveur
     * @param village village cible (modifié : villagerIds + building.residentIds mis à jour)
     * @param vt      type de village (source des villager_type_ids)
     * @param culture culture (pour résoudre types, langue, bâtiments)
     */
    public static void spawnForVillage(ServerLevel level, Village village,
                                       VillageType vt, Culture culture) {
        Random random = new Random();
        int totalSpawned = 0;
        int maxVillagers = VillageConfig.maxVillagersPerVillage;

        // Type féminin compagnon — résolu par ID court "housewife"
        Optional<VillagerTypeDef> femaleTypeOpt = culture.getVillagerType("housewife");

        for (String typeId : vt.villagerTypeIds()) {
            if (totalSpawned >= maxVillagers) break;

            Optional<VillagerTypeDef> typeDefOpt = culture.getVillagerType(typeId);
            if (typeDefOpt.isEmpty()) {
                MillenaireNewAge.LOGGER.warn("[MNA] Type '{}' introuvable dans '{}'.", typeId, culture.id());
                continue;
            }
            VillagerTypeDef typeDef = typeDefOpt.get();
            if (typeDef.spawnCount() <= 0) continue;

            int toSpawn = Math.min(typeDef.spawnCount(), maxVillagers - totalSpawned);

            for (int i = 0; i < toSpawn; i++) {

                // 1. Lieu de travail
                Building workplace = findWorkplace(village, culture, typeDef.id());

                // 2. Domicile : priorité au lieu de travail si résidentiel
                Building home = findHome(village, culture, workplace);

                // 3. Position de spawn : juste au nord du domicile (jamais à l'intérieur)
                BlockPos malePos = spawnPos(level, home, village.getCenter(),
                        home != null ? home.getResidentIds().size() : totalSpawned);

                // 4. Spawn le villageois masculin
                spawnVillager(level, village, culture, typeDef, home, workplace, malePos, random);
                totalSpawned++;

                // 5. Compagnon féminin si le domicile est familial (sleeping_capacity >= 2)
                //    et qu'il reste de la place ET un type féminin est défini
                if (femaleTypeOpt.isPresent() && home != null && totalSpawned < maxVillagers) {
                    Optional<BuildingType> btOpt = culture.getBuildingType(home.getTypeId());
                    if (btOpt.isPresent()
                            && btOpt.get().sleepingCapacity() >= 2
                            && home.getResidentIds().size() < btOpt.get().sleepingCapacity()) {
                        BlockPos femalePos = spawnPos(level, home, village.getCenter(),
                                home.getResidentIds().size());
                        spawnVillager(level, village, culture, femaleTypeOpt.get(),
                                home, null, femalePos, random);
                        totalSpawned++;
                    }
                }
            }
        }

        MillenaireNewAge.LOGGER.info("[MNA] {} villageois spawnés pour '{}'.",
                totalSpawned, village.getName());
    }

    // ── Spawn d'un villageois ─────────────────────────────────────────────────

    private static void spawnVillager(ServerLevel level, Village village, Culture culture,
                                      VillagerTypeDef typeDef, Building home, Building workplace,
                                      BlockPos pos, Random random) {
        MillVillagerEntity villager = new MillVillagerEntity(MillEntities.VILLAGER, level);
        villager.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        int bodyVariant = random.nextInt(typeDef.male() ? 11 : 4);
        villager.assignType(culture.id(), typeDef.id(), typeDef.male(), bodyVariant);

        String firstName = typeDef.male()
                ? culture.language().randomMaleName(random)
                : culture.language().randomFemaleName(random);
        String lastName = culture.language().randomLastName(random);
        villager.setFirstName(firstName);
        villager.setFamilyName(lastName);
        villager.setVillageId(village.getId());

        if (home != null) {
            villager.setHomeId(home.getId());
            home.addResident(villager.getUUID());
            // Injecter HOME_POS directement dans le Brain (disponible dès le 1er tick)
            villager.getBrain().setMemory(MillMemories.HOME_POS, home.getOrigin());
            // Injecter l'entrée depuis les données du Building (scannées au placement)
            if (home.getEntrancePos() != null) {
                villager.getBrain().setMemory(MillMemories.HOME_ENTRANCE_POS, home.getEntrancePos());
            }
            // Injecter le lit assigné par index de résident
            int residentIndex = home.getResidentIds().indexOf(villager.getUUID());
            if (residentIndex >= 0) {
                net.minecraft.core.BlockPos bed = home.getBedForResident(residentIndex);
                if (bed != null) {
                    villager.getBrain().setMemory(MillMemories.HOME_BED_POS, bed);
                }
            }
        }
        if (workplace != null) {
            villager.setWorkplaceId(workplace.getId());
            // Injecter WORK_POS directement dans le Brain
            villager.getBrain().setMemory(MillMemories.WORK_POS, workplace.getOrigin());
        }

        villager.setCustomNameVisible(true);
        villager.setPersistenceRequired();

        level.addFreshEntity(villager);
        village.addVillager(villager.getUUID());

        MillenaireNewAge.LOGGER.debug("[MNA] Spawn {} '{}' en {} (home={}, work={}).",
                typeDef.displayName(), firstName + " " + lastName,
                pos.toShortString(),
                home      != null ? home.getTypeId()      : "—",
                workplace != null ? workplace.getTypeId() : "—");
    }

    // ── Assignation domicile / lieu de travail ────────────────────────────────

    /**
     * Cherche le domicile d'un villageois.
     *
     * <p><b>P1 :</b> le lieu de travail lui-même, s'il a de la capacité disponible
     * (garde → caserne, chef → mairie).</p>
     * <p><b>P2 :</b> tout autre bâtiment résidentiel avec une place libre, en sautant
     * le lieu de travail déjà testé (bûcheron → maison du bûcheron).</p>
     */
    private static Building findHome(Village village, Culture culture, Building workplace) {
        // Priorité 1 — workplace résidentiel avec place
        if (workplace != null) {
            Optional<BuildingType> btOpt = culture.getBuildingType(workplace.getTypeId());
            if (btOpt.isPresent()
                    && btOpt.get().sleepingCapacity() > 0
                    && workplace.getResidentIds().size() < btOpt.get().sleepingCapacity()) {
                return workplace;
            }
        }

        // Priorité 2 — tout autre bâtiment résidentiel disponible
        for (Building b : village.getBuildings()) {
            if (workplace != null && b.getId().equals(workplace.getId())) continue;
            Optional<BuildingType> btOpt = culture.getBuildingType(b.getTypeId());
            if (btOpt.isEmpty()) continue;
            int cap = btOpt.get().sleepingCapacity();
            if (cap > 0 && b.getResidentIds().size() < cap) {
                return b;
            }
        }
        return null;
    }

    /**
     * Premier bâtiment dont {@code workplace_for} contient le type du villageois.
     * Supporte les IDs courts ({@code "guard"}) et complets ({@code "normans:guard"}).
     */
    private static Building findWorkplace(Village village, Culture culture, String resolvedTypeId) {
        for (Building b : village.getBuildings()) {
            Optional<BuildingType> btOpt = culture.getBuildingType(b.getTypeId());
            if (btOpt.isEmpty()) continue;
            for (String wf : btOpt.get().workplaceFor()) {
                String resolvedWf = wf.contains(":") ? wf : culture.id() + ":" + wf;
                if (resolvedWf.equals(resolvedTypeId)) return b;
            }
        }
        return null;
    }

    // ── Position de spawn ─────────────────────────────────────────────────────

    /** Positions de secours autour du centre (villageois sans domicile assigné). */
    private static final int[][] FALLBACK_OFFSETS = {
        { 8,  0}, {-8,  0}, { 0,  8}, { 0, -8},
        { 6,  6}, {-6,  6}, { 6, -6}, {-6, -6},
        {10,  0}, {-10, 0}, { 0, 10}, { 0,-10}
    };

    /**
     * Retourne une position au sol à l'extérieur du bâtiment domicile.
     *
     * <ul>
     *   <li>Si {@code home != null} : spawn 3 blocs au nord de l'angle NW du bâtiment
     *       ({@code origin.z - 3}), décalé en X par {@code offsetIndex} pour espacer
     *       les colocataires.</li>
     *   <li>Sinon : position de secours autour du centre à rayon ~8-10 blocs.</li>
     * </ul>
     */
    private static BlockPos spawnPos(ServerLevel level, Building home,
                                     BlockPos villageCenter, int offsetIndex) {
        int x, z;
        if (home != null) {
            x = home.getOrigin().getX() + (offsetIndex % 4);
            z = home.getOrigin().getZ() - 3;
        } else {
            int[] off = FALLBACK_OFFSETS[offsetIndex % FALLBACK_OFFSETS.length];
            x = villageCenter.getX() + off[0];
            z = villageCenter.getZ() + off[1];
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }
}
