package com.mat37dev.creator;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

/**
 * Baguette d'Arpentage.
 *
 * <ul>
 *   <li>Clic gauche sur un bloc = Pos1 (coin A de la sélection)</li>
 *   <li>Clic droit sur un bloc = Pos2 (coin B de la sélection)</li>
 * </ul>
 *
 * L'interception du clic gauche est enregistrée via {@code AttackBlockCallback.EVENT}
 * dans {@link com.mat37dev.MillenaireNewAge#onInitialize()}.
 */
public class StructureScannerItem extends Item {

    public StructureScannerItem(Properties props) {
        super(props);
    }

    // ── Clic droit sur bloc → Pos2 ───────────────────────────────────────────

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;

        BlockPos pos = ctx.getClickedPos();
        CreatorSession session = CreatorSession.get(player);
        session.setPos2(pos);

        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.info_prefix")
            .append(Component.translatable("chat.millenaire-new-age.creator.pos2",
                Component.literal(pos.toShortString()).withStyle(ChatFormatting.WHITE))));

        if (session.hasSelection()) {
            sendSelectionInfo(player, session);
        }

        session.spawnSelectionParticles(player);
        return InteractionResult.SUCCESS;
    }

    // ── Méthode statique appelée depuis AttackBlockCallback ──────────────────

    /**
     * À appeler depuis {@code AttackBlockCallback.EVENT} quand le joueur tient
     * cet item et clique gauche sur un bloc.
     */
    public static void onLeftClick(ServerPlayer player, BlockPos pos) {
        CreatorSession session = CreatorSession.get(player);
        session.setPos1(pos);

        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.info_prefix")
            .append(Component.translatable("chat.millenaire-new-age.creator.pos1",
                Component.literal(pos.toShortString()).withStyle(ChatFormatting.WHITE))));

        if (session.hasSelection()) {
            sendSelectionInfo(player, session);
        }

        session.spawnSelectionParticles(player);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static void sendSelectionInfo(ServerPlayer player, CreatorSession session) {
        net.minecraft.core.Vec3i size = session.getSize();
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.selection",
            size.getX(), size.getY(), size.getZ()));
    }
}
