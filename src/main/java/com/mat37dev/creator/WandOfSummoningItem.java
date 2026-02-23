package com.mat37dev.creator;

import com.mat37dev.civilization.Civilization;
import com.mat37dev.civilization.CivilizationRegistry;
import com.mat37dev.network.OpenVillageCreationPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Baguette d'Invocation.
 *
 * <p>Clic droit sur un bloc d'or → ouvre le GUI de création de village.</p>
 */
public class WandOfSummoningItem extends Item {

    public WandOfSummoningItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;

        BlockPos clickedPos = ctx.getClickedPos();
        if (ctx.getLevel().getBlockState(clickedPos).getBlock() != Blocks.GOLD_BLOCK) {
            return InteractionResult.PASS;
        }

        Collection<Civilization> civs = CivilizationRegistry.getAll();
        if (civs.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c[MNA] Aucune civilisation chargée."));
            return InteractionResult.FAIL;
        }

        List<Civilization> civList = new ArrayList<>(civs);
        ServerPlayNetworking.send(player, OpenVillageCreationPayload.from(clickedPos, civList));
        return InteractionResult.SUCCESS;
    }
}
