package com.mat37dev.block;

import com.mat37dev.init.MillBlockEntities;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Screen handler du coffre millénaire.
 *
 * <p>Si le coffre est verrouillé ({@code locked == true}), tous les slots du coffre
 * sont remplacés par des {@link LockedSlot} qui refusent toute interaction.</p>
 */
public class MillChestScreenHandler extends AbstractContainerMenu {

    /** Codec pour l'extra data (boolean locked) passé à l'ouverture. */
    public static final StreamCodec<ByteBuf, Boolean> CODEC = ByteBufCodecs.BOOL;

    private static final int CHEST_ROWS = 3;
    private static final int CHEST_COLS = 9;
    private static final int CHEST_SLOTS = CHEST_ROWS * CHEST_COLS; // 27
    private static final int PLAYER_SLOTS = 36;

    private final boolean locked;

    /** Constructeur serveur (avec le vrai container). */
    public MillChestScreenHandler(int syncId, Inventory playerInventory,
                                   Container chestContainer, boolean locked) {
        super(MillBlockEntities.MILL_CHEST_MENU, syncId);
        this.locked = locked;
        checkContainerSize(chestContainer, CHEST_SLOTS);

        // Slots du coffre (3 rangées × 9 colonnes)
        for (int row = 0; row < CHEST_ROWS; row++) {
            for (int col = 0; col < CHEST_COLS; col++) {
                int slotIndex = col + row * CHEST_COLS;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                if (locked) {
                    addSlot(new LockedSlot(chestContainer, slotIndex, x, y));
                } else {
                    addSlot(new Slot(chestContainer, slotIndex, x, y));
                }
            }
        }

        // Inventaire joueur (3 rangées) — positions vanilla : i = (containerRows-4)*18 = -18
        // → y = 103 + row*18 - 18 = 85 + row*18
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 85 + row * 18));
            }
        }

        // Barre d'action (9 slots) — y = 161 + i = 161 - 18 = 143
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 143));
        }
    }

    /** Constructeur client (avec container vide). */
    public MillChestScreenHandler(int syncId, Inventory playerInventory, Boolean locked) {
        this(syncId, playerInventory, new SimpleContainer(CHEST_SLOTS), locked != null && locked);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        if (locked) return ItemStack.EMPTY;

        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < CHEST_SLOTS) {
                if (!moveItemStackTo(stack, CHEST_SLOTS, CHEST_SLOTS + PLAYER_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(stack, 0, CHEST_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public boolean isLocked() { return locked; }

    // ── LockedSlot ────────────────────────────────────────────────────────────

    /**
     * Slot verrouillé : visible mais aucune interaction possible.
     */
    private static class LockedSlot extends Slot {
        LockedSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPickup(Player player) { return false; }

        @Override
        public boolean mayPlace(ItemStack stack) { return false; }
    }
}
