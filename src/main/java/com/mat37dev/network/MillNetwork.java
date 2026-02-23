package com.mat37dev.network;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.creator.StructurePlacerItem;
import com.mat37dev.creator.StructureSaveManager;
import com.mat37dev.init.MillItems;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Centralise l'enregistrement de tous les {@link net.minecraft.network.protocol.common.custom.CustomPayload}.
 *
 * <p>Appelé depuis {@link com.mat37dev.MillenaireNewAge#onInitialize()}.</p>
 */
public class MillNetwork {

    public static void registerServerPayloads() {
        // S→C
        PayloadTypeRegistry.playS2C().register(StructurePreviewPayload.ID,  StructurePreviewPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StructureRotationPayload.ID, StructureRotationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenStructureListPayload.ID, OpenStructureListPayload.CODEC);

        // C→S
        PayloadTypeRegistry.playC2S().register(SelectStructurePayload.ID,   SelectStructurePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DeleteStructurePayload.ID,   DeleteStructurePayload.CODEC);
    }

    public static void registerServerHandlers() {
        // C→S : le joueur a sélectionné une structure dans la GUI
        ServerPlayNetworking.registerGlobalReceiver(SelectStructurePayload.ID,
            (payload, ctx) -> {
                ServerPlayer player = ctx.player();
                String structureId  = payload.structureId();

                ctx.server().execute(() -> handleSelectStructure(player, structureId));
            }
        );

        // C→S : suppression d'une structure
        ServerPlayNetworking.registerGlobalReceiver(DeleteStructurePayload.ID,
            (payload, ctx) -> {
                ServerPlayer player = ctx.player();
                String structureId  = payload.structureId();

                ctx.server().execute(() -> {
                    handleDeleteStructure(player, structureId);
                    // Renvoyer la liste mise à jour pour rafraîchir la GUI
                    List<String> updatedList = StructureSaveManager.listStructures(ctx.server());
                    ServerPlayNetworking.send(player, new OpenStructureListPayload(updatedList));
                });
            }
        );
    }

    // ── Handlers serveur ─────────────────────────────────────────────────────

    private static void handleDeleteStructure(ServerPlayer player, String structureId) {
        String sanitized = structureId.replace(':', '/');
        java.nio.file.Path outputDir = StructureSaveManager.creatorOutputDir().resolve("creator_structures");
        java.nio.file.Path nbtPath    = outputDir.resolve(sanitized + ".nbt");
        java.nio.file.Path blocksPath = outputDir.resolve(sanitized + "_blocks.json");

        try {
            boolean deleted = java.nio.file.Files.deleteIfExists(nbtPath);
            java.nio.file.Files.deleteIfExists(blocksPath);

            if (deleted) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§e[MNA] Structure '§f" + structureId + "§e' supprimée."
                ));
            }
        } catch (java.io.IOException e) {
            MillenaireNewAge.LOGGER.error("Erreur suppression structure : {}", e.getMessage());
        }
    }

    private static void handleSelectStructure(ServerPlayer player, String structureId) {
        // Charger les positions de blocs pour le preview
        List<net.minecraft.core.BlockPos> blocks =
            StructureSaveManager.loadBlockPositions(player.level().getServer(), structureId);

        if (blocks.isEmpty()) {
            MillenaireNewAge.LOGGER.warn("Structure '{}' : aucun bloc trouvé pour le preview.", structureId);
        }

        // Calculer la taille depuis les positions max
        Vec3i size = computeSize(blocks);

        // Configurer la baguette de placement dans la main principale
        ItemStack stack = new ItemStack(MillItems.STRUCTURE_PLACER);
        StructurePlacerItem.setStructureId(stack, structureId);
        StructurePlacerItem.setRotation(stack, 0);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);

        // Envoyer les données de preview au client
        ServerPlayNetworking.send(player, new StructurePreviewPayload(structureId, blocks, size));

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§a[MNA] Structure '§f" + structureId + "§a' sélectionnée. Clic droit pour placer, Shift+clic pour tourner."
        ));
    }

    public static Vec3i computeSizePublic(List<net.minecraft.core.BlockPos> blocks) {
        return computeSize(blocks);
    }

    private static Vec3i computeSize(List<net.minecraft.core.BlockPos> blocks) {
        if (blocks.isEmpty()) return new Vec3i(1, 1, 1);
        int maxX = 1, maxY = 1, maxZ = 1;
        for (net.minecraft.core.BlockPos p : blocks) {
            if (p.getX() + 1 > maxX) maxX = p.getX() + 1;
            if (p.getY() + 1 > maxY) maxY = p.getY() + 1;
            if (p.getZ() + 1 > maxZ) maxZ = p.getZ() + 1;
        }
        return new Vec3i(maxX, maxY, maxZ);
    }
}
