package com.mat37dev.block;

import com.mat37dev.init.MillBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Coffre millénaire : visuellement identique au coffre vanilla (BER hérité),
 * supporte le double-coffre (DoubleBlockCombiner hérité de ChestBlock).
 *
 * <p>Quand le coffre est lié à un bâtiment ({@code buildingId != null} dans la BlockEntity),
 * il est verrouillé pour les joueurs (lecture seule) sauf en mode créateur.
 * Sans lien, il se comporte comme un coffre normal.</p>
 */
public class MillChestBlock extends ChestBlock {

    public static final MapCodec<MillChestBlock> CODEC = simpleCodec(MillChestBlock::new);

    /**
     * Combiner personnalisé : titre "Grand Coffre Millénaire" pour les doubles coffres
     * (au lieu de "container.chestDouble" vanilla).
     */
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>>
        MILL_MENU_PROVIDER_COMBINER = new DoubleBlockCombiner.Combiner<>() {

            @Override
            public @NotNull Optional<MenuProvider> acceptDouble(ChestBlockEntity e1, ChestBlockEntity e2) {
                final var combined = new CompoundContainer(e1, e2);
                return Optional.of(new SimpleMenuProvider(
                    (syncId, inventory, player) -> {
                        e1.unpackLootTable(player);
                        e2.unpackLootTable(player);
                        return ChestMenu.sixRows(syncId, inventory, combined);
                    },
                    Component.translatable("container.millenaire-new-age.mill_chest_double")
                ));
            }

            @Override
            public @NotNull Optional<MenuProvider> acceptSingle(ChestBlockEntity entity) {
                return Optional.of(entity);
            }

            @Override
            public @NotNull Optional<MenuProvider> acceptNone() {
                return Optional.empty();
            }
        };

    public MillChestBlock(BlockBehaviour.Properties properties) {
        super(
            () -> MillBlockEntities.MILL_CHEST_ENTITY,
            SoundEvents.CHEST_OPEN,
            SoundEvents.CHEST_CLOSE,
            properties
        );
    }

    @Override
    public @NotNull MapCodec<? extends ChestBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MillChestBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return this.combine(state, level, pos, false)
                   .apply(MILL_MENU_PROVIDER_COMBINER)
                   .orElse(null);
    }
}
