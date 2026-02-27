package com.mat37dev;

import com.mat37dev.command.MillCommands;
import com.mat37dev.config.VillageConfig;
import com.mat37dev.creator.StructureScannerItem;
import com.mat37dev.data.CultureLoader;
import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.MillMemories;
import com.mat37dev.entity.ai.MillSensors;
import com.mat37dev.init.MillBlocks;
import com.mat37dev.init.MillEntities;
import com.mat37dev.init.MillItemGroups;
import com.mat37dev.init.MillItems;
import com.mat37dev.network.MillNetwork;
import com.mat37dev.world.VillageGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MillenaireNewAge implements ModInitializer {

    public static final String MOD_ID = "millenaire-new-age";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // IA Brain — mémoires et capteurs enregistrés avant les entités
        MillMemories.init();
        MillSensors.init();

        // Registres (l'ordre compte : blocs → items → groupes → entités)
        MillBlocks.initialize();
        MillItems.initialize();
        MillItemGroups.initialize();
        MillEntities.initialize();
        FabricDefaultAttributeRegistry.register(MillEntities.VILLAGER, MillVillagerEntity.createAttributes());

        // Réseau
        MillNetwork.registerServerPayloads();
        MillNetwork.registerServerHandlers();

        // Chargeurs de données (datapacks)
        CultureLoader.register();

        // Config village (chargée au démarrage du serveur)
        ServerLifecycleEvents.SERVER_STARTED.register(VillageConfig::load);

        // Génération naturelle — phase 1 : détection au chargement (ultra-léger)
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) ->
            VillageGenerator.onChunkLoad(level, chunk.getPos()));

        // Génération naturelle — phase 2 : placement différé (1 par tick)
        ServerTickEvents.END_SERVER_TICK.register(VillageGenerator::processTick);

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
