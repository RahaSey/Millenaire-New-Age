package com.mat37dev.entity;

import com.mat37dev.culture.CultureRegistry;
import com.mat37dev.culture.VillagerTypeDef;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MillVillagerEntity extends Mob {

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

    // ── Champs serveur (NBT uniquement) ──────────────────────────────────────

    @Nullable private UUID villageId;
    @Nullable private UUID homeId;
    @Nullable private UUID workplaceId;

    // ── Constructeur ─────────────────────────────────────────────────────────

    public MillVillagerEntity(EntityType<? extends MillVillagerEntity> type, Level level) {
        super(type, level);
    }

    // ── Attributs ────────────────────────────────────────────────────────────

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.55)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
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
    }

    // ── Spawn initial ─────────────────────────────────────────────────────────

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);

        // Si aucun type n'est déjà assigné (ex: spawn via /summon), assigner un type normand aléatoire
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
            // Type déjà assigné (via VillagePlacer) : juste randomiser le variant corps
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

    public boolean isMale() {
        return getEntityData().get(SEX);
    }

    public int getBodyVariant() {
        return getEntityData().get(BODY_VARIANT);
    }

    /** Retourne le chemin relatif de la texture de vêtement (peut être vide). */
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

    // ── Setters ───────────────────────────────────────────────────────────────

    public void assignType(String cultureId, String typeId, boolean male, int bodyVariant) {
        getEntityData().set(CULTURE_ID, cultureId);
        getEntityData().set(VILLAGER_TYPE_ID, typeId);
        getEntityData().set(SEX, male);
        getEntityData().set(BODY_VARIANT, bodyVariant);
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
