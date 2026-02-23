package com.mat37dev;

import com.mat37dev.command.MillCommands;
import com.mat37dev.creator.StructureScannerItem;
import com.mat37dev.data.CivilizationLoader;
import com.mat37dev.init.MillBlocks;
import com.mat37dev.init.MillItemGroups;
import com.mat37dev.init.MillItems;
import com.mat37dev.network.MillNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MillenaireNewAge implements ModInitializer {

    public static final String MOD_ID = "millenaire-new-age";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Registres (l'ordre compte : blocs → items → groupes)
        MillBlocks.initialize();
        MillItems.initialize();
        MillItemGroups.initialize();

        // Réseau
        MillNetwork.registerServerPayloads();
        MillNetwork.registerServerHandlers();

        // Chargeurs de données (datapacks)
        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new CivilizationLoader());

        // Commandes debug
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> MillCommands.register(dispatcher));

        // Clic gauche avec la Baguette d'Arpentage → Pos1
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClientSide()
                    && player instanceof ServerPlayer serverPlayer
                    && player.getItemInHand(hand).getItem() instanceof StructureScannerItem) {
                StructureScannerItem.onLeftClick(serverPlayer, pos);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        LOGGER.info("Millenaire: New Age initialized.");
    }
}
