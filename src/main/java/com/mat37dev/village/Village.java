package com.mat37dev.village;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Village {

    private final UUID id;
    private String name;
    private String civilizationId;
    private String villageTypeId;
    private BlockPos center;
    private VillageState state;
    private final List<Building> buildings = new ArrayList<>();
    private final List<UUID> villagerIds = new ArrayList<>();
    private final Map<UUID, Integer> reputation = new HashMap<>();
    private final ResourceStock stock = new ResourceStock();

    public Village(UUID id, String name, String civilizationId,
                   String villageTypeId, BlockPos center) {
        this.id = id;
        this.name = name;
        this.civilizationId = civilizationId;
        this.villageTypeId = villageTypeId;
        this.center = center;
        this.state = VillageState.GROWING;
    }

    // ── Reputation ────────────────────────────────────────────────────────────

    public int getReputation(UUID playerId) {
        return reputation.getOrDefault(playerId, 0);
    }

    public void addReputation(UUID playerId, int delta) {
        reputation.merge(playerId, delta, Integer::sum);
    }

    public void setReputation(UUID playerId, int value) {
        reputation.put(playerId, value);
    }

    // ── Buildings ─────────────────────────────────────────────────────────────

    public void addBuilding(Building building) {
        buildings.add(building);
    }

    public Optional<Building> getBuilding(UUID buildingId) {
        return buildings.stream().filter(b -> b.getId().equals(buildingId)).findFirst();
    }

    public void removeBuilding(UUID buildingId) {
        buildings.removeIf(b -> b.getId().equals(buildingId));
    }

    // ── Villagers ─────────────────────────────────────────────────────────────

    public void addVillager(UUID uuid) { villagerIds.add(uuid); }
    public void removeVillager(UUID uuid) { villagerIds.remove(uuid); }

    // ── Sérialisation (MC 1.21.10 ValueInput/ValueOutput) ────────────────────

    public void writeTo(ValueOutput output) {
        output.putLong("id_msb", id.getMostSignificantBits());
        output.putLong("id_lsb", id.getLeastSignificantBits());
        output.putString("name", name);
        output.putString("civilization_id", civilizationId);
        output.putString("village_type_id", villageTypeId);
        output.putLong("center", center.asLong());
        output.putString("state", state.name());

        // Bâtiments
        ValueOutput.ValueOutputList buildingList = output.childrenList("buildings");
        for (Building b : buildings) {
            b.writeTo(buildingList.addChild());
        }

        // Villageois
        ValueOutput.ValueOutputList villagerList = output.childrenList("villagers");
        for (UUID uuid : villagerIds) {
            ValueOutput entry = villagerList.addChild();
            entry.putLong("msb", uuid.getMostSignificantBits());
            entry.putLong("lsb", uuid.getLeastSignificantBits());
        }

        // Réputation
        ValueOutput.ValueOutputList reputationList = output.childrenList("reputation");
        reputation.forEach((uuid, rep) -> {
            ValueOutput entry = reputationList.addChild();
            entry.putLong("msb", uuid.getMostSignificantBits());
            entry.putLong("lsb", uuid.getLeastSignificantBits());
            entry.putInt("rep", rep);
        });

        // Stock
        stock.writeTo(output.child("stock"));
    }

    public static Village readFrom(ValueInput input) {
        UUID id = new UUID(input.getLongOr("id_msb", 0L), input.getLongOr("id_lsb", 0L));
        String name = input.getStringOr("name", "");
        String civilizationId = input.getStringOr("civilization_id", "");
        String villageTypeId = input.getStringOr("village_type_id", "");
        BlockPos center = BlockPos.of(input.getLongOr("center", 0L));

        Village village = new Village(id, name, civilizationId, villageTypeId, center);
        village.state = VillageState.valueOf(input.getStringOr("state", "GROWING"));

        for (ValueInput entry : input.childrenListOrEmpty("buildings")) {
            village.buildings.add(Building.readFrom(entry));
        }

        for (ValueInput entry : input.childrenListOrEmpty("villagers")) {
            village.villagerIds.add(new UUID(entry.getLongOr("msb", 0L), entry.getLongOr("lsb", 0L)));
        }

        for (ValueInput entry : input.childrenListOrEmpty("reputation")) {
            UUID uuid = new UUID(entry.getLongOr("msb", 0L), entry.getLongOr("lsb", 0L));
            village.reputation.put(uuid, entry.getIntOr("rep", 0));
        }

        village.stock.readFrom(input.childOrEmpty("stock"));
        return village;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCivilizationId() { return civilizationId; }
    public String getVillageTypeId() { return villageTypeId; }
    public BlockPos getCenter() { return center; }
    public VillageState getState() { return state; }
    public void setState(VillageState state) { this.state = state; }
    public List<Building> getBuildings() { return Collections.unmodifiableList(buildings); }
    public List<UUID> getVillagerIds() { return Collections.unmodifiableList(villagerIds); }
    public Map<UUID, Integer> getReputation() { return Collections.unmodifiableMap(reputation); }
    public ResourceStock getStock() { return stock; }
}
