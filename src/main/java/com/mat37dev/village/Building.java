package com.mat37dev.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Building {

    private final UUID id;
    private final UUID villageId;
    private final String typeId;
    private final BlockPos origin;
    private final Direction facing;
    private BuildingState state;
    private int currentHealth;
    private final int maxHealth;
    private final List<UUID> residentIds = new ArrayList<>();

    /** Positions des lits dans le bâtiment (scannés au placement). */
    private final List<BlockPos> bedPositions = new ArrayList<>();

    /** Position de l'entrée (porte) du bâtiment (scannée au placement). Peut être null. */
    @Nullable private BlockPos entrancePos;

    /** Positions des coffres millénaires dans le bâtiment (scannés au placement). */
    private final List<BlockPos> chestPositions = new ArrayList<>();

    /**
     * Position de dépôt par défaut : voisin non-solide du premier coffre,
     * côté le plus proche de l'entrée. Calculé au scan, peut être null.
     */
    @Nullable private BlockPos sellingPos;

    /** Cache agrégé du contenu de tous les coffres du bâtiment. Invalidé à chaque changement. */
    @Nullable private transient Map<Item, Integer> inventoryCache = null;

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

    // ── Lits & Entrée ─────────────────────────────────────────────────────────

    /** Ajoute une position de lit (appelé lors du scan après placement). */
    public void addBedPosition(BlockPos pos) { bedPositions.add(pos.immutable()); }

    /** Retourne la liste (immuable) des positions de lits. */
    public List<BlockPos> getBedPositions() { return Collections.unmodifiableList(bedPositions); }

    /**
     * Retourne le lit assigné au résident à l'index donné.
     * Les résidents sont assignés aux lits par ordre d'ajout.
     *
     * @param residentIndex index du résident dans {@link #getResidentIds()}
     * @return position du lit, ou null si pas assez de lits
     */
    @Nullable
    public BlockPos getBedForResident(int residentIndex) {
        if (residentIndex < 0 || residentIndex >= bedPositions.size()) return null;
        return bedPositions.get(residentIndex);
    }

    /** Définit la position de l'entrée (porte) du bâtiment. */
    public void setEntrancePos(@Nullable BlockPos pos) {
        this.entrancePos = pos != null ? pos.immutable() : null;
    }

    /** Retourne la position de l'entrée, ou null si aucune porte trouvée. */
    @Nullable
    public BlockPos getEntrancePos() { return entrancePos; }

    // ── Coffres & Stockage ────────────────────────────────────────────────────

    /** Ajoute une position de coffre millénaire (appelé lors du scan après placement). */
    public void addChestPosition(BlockPos pos) {
        chestPositions.add(pos.immutable());
        inventoryCache = null;
    }

    /** Retourne la liste (immuable) des positions de coffres. */
    public List<BlockPos> getChestPositions() { return Collections.unmodifiableList(chestPositions); }

    /** Définit la position de dépôt (sellingPos). */
    public void setSellingPos(@Nullable BlockPos pos) {
        this.sellingPos = pos != null ? pos.immutable() : null;
    }

    /** Retourne la position de dépôt, ou null si non calculée. */
    @Nullable
    public BlockPos getSellingPos() { return sellingPos; }

    /**
     * Construit ou retourne le cache agrégé du contenu de tous les coffres.
     * Le cache est invalidé automatiquement par {@link #invalidateInventoryCache()}.
     */
    public Map<Item, Integer> getInventoryCache(Level level) {
        if (inventoryCache != null) return inventoryCache;
        inventoryCache = new HashMap<>();
        for (BlockPos pos : chestPositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.mat37dev.block.MillChestBlockEntity chest)) continue;
            for (int i = 0; i < chest.getContainerSize(); i++) {
                ItemStack stack = chest.getItem(i);
                if (!stack.isEmpty()) {
                    inventoryCache.merge(stack.getItem(), stack.getCount(), Integer::sum);
                }
            }
        }
        return inventoryCache;
    }

    /** Invalide le cache inventaire (à appeler après toute modification d'un coffre du bâtiment). */
    public void invalidateInventoryCache() { inventoryCache = null; }

    /**
     * Compte combien d'exemplaires de l'item sont stockés dans les coffres du bâtiment.
     */
    public int countGoods(Level level, Item item) {
        return getInventoryCache(level).getOrDefault(item, 0);
    }

    /**
     * Retire {@code amount} exemplaires de l'item des coffres du bâtiment.
     *
     * @return le nombre réellement retiré (peut être < amount si stock insuffisant)
     */
    public int takeGoods(Level level, Item item, int amount) {
        int remaining = amount;
        for (BlockPos pos : chestPositions) {
            if (remaining <= 0) break;
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.mat37dev.block.MillChestBlockEntity chest)) continue;
            for (int i = 0; i < chest.getContainerSize() && remaining > 0; i++) {
                ItemStack stack = chest.getItem(i);
                if (!stack.isEmpty() && stack.is(item)) {
                    int toRemove = Math.min(remaining, stack.getCount());
                    stack.shrink(toRemove);
                    remaining -= toRemove;
                    chest.setItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                    chest.setChanged();
                }
            }
        }
        int taken = amount - remaining;
        if (taken > 0) inventoryCache = null;
        return taken;
    }

    /**
     * Stocke des items dans les coffres du bâtiment.
     * Essaie de remplir les stacks existants avant d'ouvrir de nouveaux slots.
     *
     * @param level  monde serveur
     * @param stack  stack à stocker (modifié in-place : count réduit)
     */
    public void storeGoods(Level level, ItemStack stack) {
        if (stack.isEmpty()) return;
        for (BlockPos pos : chestPositions) {
            if (stack.isEmpty()) break;
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.mat37dev.block.MillChestBlockEntity chest)) continue;
            // Remplir les stacks existants du même type
            for (int i = 0; i < chest.getContainerSize() && !stack.isEmpty(); i++) {
                ItemStack existing = chest.getItem(i);
                if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    int toAdd = Math.min(space, stack.getCount());
                    existing.grow(toAdd);
                    stack.shrink(toAdd);
                    chest.setItem(i, existing);
                    chest.setChanged();
                }
            }
            // Remplir les slots vides
            for (int i = 0; i < chest.getContainerSize() && !stack.isEmpty(); i++) {
                if (chest.getItem(i).isEmpty()) {
                    int toAdd = Math.min(stack.getMaxStackSize(), stack.getCount());
                    chest.setItem(i, stack.copyWithCount(toAdd));
                    stack.shrink(toAdd);
                    chest.setChanged();
                }
            }
        }
        inventoryCache = null;
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

        if (entrancePos != null) {
            output.putLong("entrance_pos", entrancePos.asLong());
        }

        ValueOutput.ValueOutputList residentList = output.childrenList("residents");
        for (UUID uuid : residentIds) {
            ValueOutput entry = residentList.addChild();
            entry.putLong("msb", uuid.getMostSignificantBits());
            entry.putLong("lsb", uuid.getLeastSignificantBits());
        }

        ValueOutput.ValueOutputList bedList = output.childrenList("beds");
        for (BlockPos pos : bedPositions) {
            ValueOutput entry = bedList.addChild();
            entry.putLong("pos", pos.asLong());
        }

        ValueOutput.ValueOutputList chestList = output.childrenList("chests");
        for (BlockPos pos : chestPositions) {
            ValueOutput entry = chestList.addChild();
            entry.putLong("pos", pos.asLong());
        }

        if (sellingPos != null) {
            output.putLong("selling_pos", sellingPos.asLong());
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

        long entranceLong = input.getLongOr("entrance_pos", 0L);
        if (entranceLong != 0L) {
            building.entrancePos = BlockPos.of(entranceLong);
        }

        for (ValueInput entry : input.childrenListOrEmpty("residents")) {
            building.residentIds.add(new UUID(entry.getLongOr("msb", 0L), entry.getLongOr("lsb", 0L)));
        }

        for (ValueInput entry : input.childrenListOrEmpty("beds")) {
            building.bedPositions.add(BlockPos.of(entry.getLongOr("pos", 0L)));
        }

        for (ValueInput entry : input.childrenListOrEmpty("chests")) {
            building.chestPositions.add(BlockPos.of(entry.getLongOr("pos", 0L)));
        }

        long sellingLong = input.getLongOr("selling_pos", 0L);
        if (sellingLong != 0L) {
            building.sellingPos = BlockPos.of(sellingLong);
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
