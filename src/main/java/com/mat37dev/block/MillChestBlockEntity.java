package com.mat37dev.block;

import com.mat37dev.init.MillBlockEntities;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * BlockEntity du coffre millénaire.
 *
 * <p>Étend {@link ChestBlockEntity} pour hériter :
 * <ul>
 *   <li>Les 27 slots d'inventaire</li>
 *   <li>{@link net.minecraft.world.level.block.entity.LidBlockEntity} (animation couvercle)</li>
 *   <li>La sérialisation des items</li>
 * </ul>
 * Ajoute uniquement le champ {@code buildingId} pour le verrouillage village.</p>
 *
 * <p>Si {@link #buildingId} est non-null, le coffre est verrouillé :
 * les joueurs peuvent l'ouvrir mais ne peuvent pas prendre ni déposer d'objets,
 * sauf s'ils sont en mode créateur.</p>
 */
public class MillChestBlockEntity extends ChestBlockEntity
        implements ExtendedScreenHandlerFactory<Boolean> {

    /** UUID du bâtiment auquel ce coffre appartient. Null si coffre libre. */
    @Nullable
    private UUID buildingId = null;

    public MillChestBlockEntity(BlockPos pos, BlockState state) {
        super(MillBlockEntities.MILL_CHEST_ENTITY, pos, state);
    }

    // ── ExtendedScreenHandlerFactory ─────────────────────────────────────────

    @Override
    public Boolean getScreenOpeningData(ServerPlayer player) {
        return isLockedFor(player);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return getName();
    }

    // ── Container ─────────────────────────────────────────────────────────────

    @Override
    protected @NotNull Component getDefaultName() {
        return buildingId != null
                ? Component.translatable("container.millenaire-new-age.mill_chest_locked")
                : Component.translatable("container.millenaire-new-age.mill_chest");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
        boolean locked = isLockedFor(inventory.player);
        return new MillChestScreenHandler(syncId, inventory, this, locked);
    }

    // ── Locking ───────────────────────────────────────────────────────────────

    /**
     * Retourne true si le coffre est verrouillé pour ce joueur.
     * Un coffre lié à un bâtiment est verrouillé pour les joueurs normaux.
     * Les joueurs en mode créateur ont toujours accès complet.
     */
    public boolean isLockedFor(Player player) {
        if (buildingId == null) return false;
        return !player.isCreative();
    }

    // ── Sérialisation ─────────────────────────────────────────────────────────
    // super.saveAdditional() / super.loadAdditional() gère déjà les items,
    // le custom name et la loot table (via RandomizableContainerBlockEntity).

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (buildingId != null) {
            output.putLong("building_msb", buildingId.getMostSignificantBits());
            output.putLong("building_lsb", buildingId.getLeastSignificantBits());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        long msb = input.getLongOr("building_msb", 0L);
        long lsb = input.getLongOr("building_lsb", 0L);
        this.buildingId = (msb != 0L || lsb != 0L) ? new UUID(msb, lsb) : null;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    @Nullable
    public UUID getBuildingId() { return buildingId; }

    public void setBuildingId(@Nullable UUID id) {
        this.buildingId = id;
        setChanged();
    }
}
