package com.mat37dev.entity;

import com.mat37dev.culture.CultureRegistry;
import com.mat37dev.culture.VillagerTypeDef;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.entity.ai.MillVillagerAi;
import com.mat37dev.village.Village;
import com.mat37dev.village.VillageManager;
import com.mojang.serialization.Dynamic;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class MillVillagerEntity extends PathfinderMob {

    // ── SynchedEntityData — visibles côté client pour le renderer ────────────

    private static final EntityDataAccessor<String> CULTURE_ID =
            SynchedEntityData.defineId(MillVillagerEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> VILLAGER_TYPE_ID =
            SynchedEntityData.defineId(MillVillagerEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> SEX =
            SynchedEntityData.defineId(MillVillagerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BODY_VARIANT =
            SynchedEntityData.defineId(MillVillagerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> FIRST_NAME =
            SynchedEntityData.defineId(MillVillagerEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FAMILY_NAME =
            SynchedEntityData.defineId(MillVillagerEntity.class, EntityDataSerializers.STRING);
    /** Vrai quand le villageois est en train de dormir (pose couchée côté renderer). */
    private static final EntityDataAccessor<Boolean> IS_SLEEPING =
            SynchedEntityData.defineId(MillVillagerEntity.class, EntityDataSerializers.BOOLEAN);

    // ── Champs serveur (NBT uniquement) ──────────────────────────────────────

    @Nullable private UUID villageId;
    @Nullable private UUID homeId;
    @Nullable private UUID workplaceId;

    /**
     * Prochain game-tick où tenter d'injecter HOME_POS/WORK_POS depuis VillageManager.
     * Vaut -1 une fois que l'injection a réussi (plus besoin de réessayer).
     * Permet de tolérer un VillageManager pas encore prêt au premier tick.
     */
    private long nextBrainInitAttempt = 0L;

    // ── Constructeur ─────────────────────────────────────────────────────────

    public MillVillagerEntity(EntityType<? extends MillVillagerEntity> type, Level level) {
        super(type, level);
        this.getNavigation().setCanOpenDoors(true);
    }

    // ── Attributs ────────────────────────────────────────────────────────────

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 20.0)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.5)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE, 48.0)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 1.0);
    }

    // ── Brain API ─────────────────────────────────────────────────────────────

    @Override
    protected Brain.@NotNull Provider<MillVillagerEntity> brainProvider() {
        return Brain.provider(MillVillagerAi.MEMORY_TYPES, MillVillagerAi.SENSOR_TYPES);
    }

    @Override
    protected @NotNull Brain<?> makeBrain(Dynamic<?> dynamic) {
        return MillVillagerAi.makeBrain(brainProvider(), dynamic);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Brain<MillVillagerEntity> getBrain() {
        return (Brain<MillVillagerEntity>) super.getBrain();
    }

    /**
     * Tick IA principal : gestion du schedule et tick du Brain.
     *
     * <h3>Schedule (ticks MC, 0 = lever du soleil = 6h)</h3>
     * <ul>
     *   <li>0–11000   : WORK (journée)</li>
     *   <li>11000–14000 : IDLE (loisir)</li>
     *   <li>14000–24000 : REST (nuit)</li>
     *   <li>PANIC : priorité absolue si ATTACK_TARGET présent</li>
     * </ul>
     */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        // Injection différée des mémoires HOME_POS/WORK_POS.
        if (nextBrainInitAttempt >= 0 && level.getGameTime() >= nextBrainInitAttempt) {
            if (initBrainMemoriesIfNeeded(level)) {
                nextBrainInitAttempt = -1L;
            } else {
                nextBrainInitAttempt = level.getGameTime() + 20L;
            }
        }

        Brain<MillVillagerEntity> brain = getBrain();
        long dayTime = level.getDayTime() % 24000L;
        Activity previousActivity = brain.getActiveNonCoreActivity().orElse(null);
        Activity newActivity;

        // Détermination de la nouvelle activité
        if (brain.getMemory(MillMemories.ATTACK_TARGET).isPresent()) {
            newActivity = Activity.PANIC;
        } else if (dayTime < MillVillagerAi.IDLE_START) {
            newActivity = Activity.WORK;
        } else if (dayTime < MillVillagerAi.REST_START) {
            newActivity = Activity.IDLE;
        } else {
            newActivity = Activity.REST;
        }

        // Si l'activité change, on nettoie les intentions de mouvement précédentes
        if (newActivity != previousActivity) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.eraseMemory(MemoryModuleType.PATH);
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
            this.getNavigation().stop();
            brain.setActiveActivityIfPossible(newActivity);
        }

        brain.tick(level, this);
        super.customServerAiStep(level);
    }

    /**
     * Tente d'injecter HOME_POS et WORK_POS dans le Brain si absentes.
     * @return {@code true} si l'initialisation a réussi (ou si rien à faire),
     *         {@code false} si VillageManager n'a pas encore le village — réessayer plus tard.
     */
    private boolean initBrainMemoriesIfNeeded(ServerLevel level) {
        Brain<MillVillagerEntity> brain = getBrain();
        if (villageId == null) return true; // pas de village assigné, rien à faire

        Optional<Village> villageOpt = VillageManager.getVillage(level, villageId);
        if (villageOpt.isEmpty()) return false; // village pas encore chargé — réessayer

        Village village = villageOpt.get();
        if (homeId != null && brain.getMemory(MillMemories.HOME_POS).isEmpty()) {
            village.getBuilding(homeId).ifPresent(b ->
                    brain.setMemory(MillMemories.HOME_POS, b.getOrigin()));
        }
        if (workplaceId != null && brain.getMemory(MillMemories.WORK_POS).isEmpty()) {
            village.getBuilding(workplaceId).ifPresent(b ->
                    brain.setMemory(MillMemories.WORK_POS, b.getOrigin()));
        }
        return true;
    }

    // ── SynchedEntityData ─────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CULTURE_ID, "");
        builder.define(VILLAGER_TYPE_ID, "");
        builder.define(SEX, true);
        builder.define(BODY_VARIANT, 0);
        builder.define(FIRST_NAME, "");
        builder.define(FAMILY_NAME, "");
        builder.define(IS_SLEEPING, false);
    }

    // ── Sérialisation ─────────────────────────────────────────────────────────

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("CultureId", getEntityData().get(CULTURE_ID));
        output.putString("VillagerTypeId", getEntityData().get(VILLAGER_TYPE_ID));
        output.putBoolean("Sex", getEntityData().get(SEX));
        output.putInt("BodyVariant", getEntityData().get(BODY_VARIANT));
        output.putString("FirstName", getEntityData().get(FIRST_NAME));
        output.putString("FamilyName", getEntityData().get(FAMILY_NAME));
        if (villageId != null) {
            output.putLong("VillageIdMsb", villageId.getMostSignificantBits());
            output.putLong("VillageIdLsb", villageId.getLeastSignificantBits());
        }
        if (homeId != null) {
            output.putLong("HomeIdMsb", homeId.getMostSignificantBits());
            output.putLong("HomeIdLsb", homeId.getLeastSignificantBits());
        }
        if (workplaceId != null) {
            output.putLong("WorkplaceIdMsb", workplaceId.getMostSignificantBits());
            output.putLong("WorkplaceIdLsb", workplaceId.getLeastSignificantBits());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        getEntityData().set(CULTURE_ID, input.getStringOr("CultureId", ""));
        getEntityData().set(VILLAGER_TYPE_ID, input.getStringOr("VillagerTypeId", ""));
        getEntityData().set(SEX, input.getBooleanOr("Sex", true));
        getEntityData().set(BODY_VARIANT, input.getIntOr("BodyVariant", 0));
        getEntityData().set(FIRST_NAME, input.getStringOr("FirstName", ""));
        getEntityData().set(FAMILY_NAME, input.getStringOr("FamilyName", ""));
        long villMsb = input.getLongOr("VillageIdMsb", 0L);
        long villLsb = input.getLongOr("VillageIdLsb", 0L);
        villageId = (villMsb != 0 || villLsb != 0) ? new UUID(villMsb, villLsb) : null;
        long homeMsb = input.getLongOr("HomeIdMsb", 0L);
        long homeLsb = input.getLongOr("HomeIdLsb", 0L);
        homeId = (homeMsb != 0 || homeLsb != 0) ? new UUID(homeMsb, homeLsb) : null;
        long workMsb = input.getLongOr("WorkplaceIdMsb", 0L);
        long workLsb = input.getLongOr("WorkplaceIdLsb", 0L);
        workplaceId = (workMsb != 0 || workLsb != 0) ? new UUID(workMsb, workLsb) : null;

        // Réinitialiser pour forcer un rechargement des mémoires Brain au prochain tick
        nextBrainInitAttempt = 0L;
    }

    // ── Spawn initial ─────────────────────────────────────────────────────────

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);

        if (getEntityData().get(CULTURE_ID).isEmpty()) {
            CultureRegistry.get("normans").ifPresent(culture -> {
                if (!culture.villagerTypes().isEmpty()) {
                    VillagerTypeDef typeDef = culture.villagerTypes().get(
                            level.getRandom().nextInt(culture.villagerTypes().size()));
                    boolean male = typeDef.male();
                    int variant = level.getRandom().nextInt(male ? 11 : 4);
                    assignType(culture.id(), typeDef.id(), male, variant);
                }
            });
        } else {
            boolean male = getEntityData().get(SEX);
            getEntityData().set(BODY_VARIANT, level.getRandom().nextInt(male ? 11 : 4));
        }

        return data;
    }

    // ── Nametag ───────────────────────────────────────────────────────────────

    @Override
    public @NotNull Component getDisplayName() {
        String typeId = getEntityData().get(VILLAGER_TYPE_ID);
        String cultureId = getEntityData().get(CULTURE_ID);
        String typeName = "";
        if (!typeId.isEmpty() && !cultureId.isEmpty()) {
            typeName = CultureRegistry.get(cultureId)
                    .flatMap(c -> c.villagerTypes().stream()
                            .filter(t -> t.id().equals(typeId))
                            .findFirst())
                    .map(VillagerTypeDef::displayName)
                    .orElse(typeId);
        }

        String fName = getEntityData().get(FIRST_NAME);
        String lName = getEntityData().get(FAMILY_NAME);
        String fullName = (fName.isEmpty() && lName.isEmpty())
                ? "Villageois" : (fName + " " + lName).trim();
        Component name = Component.literal(fullName);
        if (!typeName.isEmpty()) {
            name = name.copy().append(
                    Component.translatable("entity.millenaire-new-age.villager.role", typeName));
        }
        return name;
    }

    // ── Accesseurs renderer ───────────────────────────────────────────────────

    public boolean isMale() { return getEntityData().get(SEX); }
    public int getBodyVariant() { return getEntityData().get(BODY_VARIANT); }

    /** Retourne {@code true} si le villageois est en train de dormir (pose couchée). */
    public boolean isVillagerSleeping() { return getEntityData().get(IS_SLEEPING); }

    /** Chemin relatif de la texture de vêtement (peut être vide si non assigné). */
    public String getClothingTexture() {
        String typeId = getEntityData().get(VILLAGER_TYPE_ID);
        String cultureId = getEntityData().get(CULTURE_ID);
        if (typeId.isEmpty() || cultureId.isEmpty()) return "";
        return CultureRegistry.get(cultureId)
                .flatMap(c -> c.villagerTypes().stream()
                        .filter(t -> t.id().equals(typeId))
                        .findFirst())
                .map(VillagerTypeDef::clothingTexture)
                .orElse("");
    }

    /** Chemin relatif de la texture des cheveux (peut être vide si non assigné). */
    public String getHairTexture() {
        String typeId = getEntityData().get(VILLAGER_TYPE_ID);
        String cultureId = getEntityData().get(CULTURE_ID);
        if (typeId.isEmpty() || cultureId.isEmpty()) return "";
        return CultureRegistry.get(cultureId)
                .flatMap(c -> c.villagerTypes().stream()
                        .filter(t -> t.id().equals(typeId))
                        .findFirst())
                .map(VillagerTypeDef::hairTexture)
                .orElse("");
    }
    
    // ── Setters ───────────────────────────────────────────────────────────────

    public void assignType(String cultureId, String typeId, boolean male, int bodyVariant) {
        getEntityData().set(CULTURE_ID, cultureId);
        getEntityData().set(VILLAGER_TYPE_ID, typeId);
        getEntityData().set(SEX, male);
        getEntityData().set(BODY_VARIANT, bodyVariant);
    }

    public void setSleeping(boolean sleeping) {
        getEntityData().set(IS_SLEEPING, sleeping);
    }

    public void setVillageId(@Nullable UUID id) { this.villageId = id; }
    public void setHomeId(@Nullable UUID id) { this.homeId = id; }
    public void setWorkplaceId(@Nullable UUID id) { this.workplaceId = id; }
    public void setFirstName(String firstName) { getEntityData().set(FIRST_NAME, firstName); }
    public void setFamilyName(String familyName) { getEntityData().set(FAMILY_NAME, familyName); }

    @Nullable public UUID getVillageId() { return villageId; }
    public String getFirstName() { return getEntityData().get(FIRST_NAME); }
    public String getFamilyName() { return getEntityData().get(FAMILY_NAME); }
}
