package com.mat37dev.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Building {

    private final UUID id;
    private final UUID villageId;
    private final String typeId;
    private BlockPos origin;
    private Direction facing;
    private BuildingState state;
    private int currentHealth;
    private int maxHealth;
    private final List<UUID> residentIds = new ArrayList<>();

    public Building(UUID id, UUID villageId, String typeId,
                    BlockPos origin, Direction facing, int maxHealth) {
        this.id = id;
        this.villageId = villageId;
        this.typeId = typeId;
        this.origin = origin;
        this.facing = facing;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.state = BuildingState.INTACT;
    }

    public void applyDamage(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        state = BuildingState.fromHealth(currentHealth, maxHealth);
    }

    public void repair(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
        state = BuildingState.fromHealth(currentHealth, maxHealth);
    }

    // ── Sérialisation (MC 1.21.10 ValueInput/ValueOutput) ────────────────────

    public void writeTo(ValueOutput output) {
        output.putLong("id_msb", id.getMostSignificantBits());
        output.putLong("id_lsb", id.getLeastSignificantBits());
        output.putLong("village_msb", villageId.getMostSignificantBits());
        output.putLong("village_lsb", villageId.getLeastSignificantBits());
        output.putString("type_id", typeId);
        output.putLong("origin", origin.asLong());
        output.putString("facing", facing.getSerializedName());
        output.putString("state", state.name());
        output.putInt("health", currentHealth);
        output.putInt("max_health", maxHealth);

        ValueOutput.ValueOutputList residentList = output.childrenList("residents");
        for (UUID uuid : residentIds) {
            ValueOutput entry = residentList.addChild();
            entry.putLong("msb", uuid.getMostSignificantBits());
            entry.putLong("lsb", uuid.getLeastSignificantBits());
        }
    }

    public static Building readFrom(ValueInput input) {
        UUID id = new UUID(input.getLongOr("id_msb", 0L), input.getLongOr("id_lsb", 0L));
        UUID villageId = new UUID(input.getLongOr("village_msb", 0L), input.getLongOr("village_lsb", 0L));
        String typeId = input.getStringOr("type_id", "");
        BlockPos origin = BlockPos.of(input.getLongOr("origin", 0L));
        Direction facing = Direction.byName(input.getStringOr("facing", "north"));
        if (facing == null) facing = Direction.NORTH;
        int maxHealth = input.getIntOr("max_health", 100);

        Building building = new Building(id, villageId, typeId, origin, facing, maxHealth);
        building.currentHealth = input.getIntOr("health", maxHealth);
        building.state = BuildingState.valueOf(input.getStringOr("state", "INTACT"));

        for (ValueInput entry : input.childrenListOrEmpty("residents")) {
            building.residentIds.add(new UUID(entry.getLongOr("msb", 0L), entry.getLongOr("lsb", 0L)));
        }
        return building;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public UUID getVillageId() { return villageId; }
    public String getTypeId() { return typeId; }
    public BlockPos getOrigin() { return origin; }
    public Direction getFacing() { return facing; }
    public BuildingState getState() { return state; }
    public void setState(BuildingState state) { this.state = state; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public List<UUID> getResidentIds() { return Collections.unmodifiableList(residentIds); }
    public void addResident(UUID uuid) { residentIds.add(uuid); }
    public void removeResident(UUID uuid) { residentIds.remove(uuid); }
}
