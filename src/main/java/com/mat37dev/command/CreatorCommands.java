package com.mat37dev.command;

import com.mat37dev.block.MillChestBlockEntity;
import com.mat37dev.config.VillageConfig;
import com.mat37dev.creator.CreatorSession;
import com.mat37dev.creator.StructurePlacerItem;
import com.mat37dev.creator.StructureSaveManager;
import com.mat37dev.init.MillItems;
import com.mat37dev.network.MillNetwork;
import com.mat37dev.network.OpenStructureListPayload;
import com.mat37dev.network.StructurePreviewPayload;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Commandes {@code /mna creator}.
 *
 * <pre>
 * /mna creator tool scanner          → donne la Baguette d'Arpentage
 * /mna creator tool placer           → donne la Baguette de Placement (vide)
 *
 * /mna creator structure save <id>   → sauvegarde la sélection actuelle
 * /mna creator structure list        → ouvre le GUI de liste
 * /mna creator structure place <id>  → configure la baguette + envoie le preview
 * /mna creator structure delete <id> → supprime le .nbt et le _blocks.json
 * /mna creator structure info <id>   → infos sur une structure
 *
 * /mna creator selection clear       → réinitialise la sélection
 * /mna creator selection info        → affiche pos1/pos2/taille
 *
 * /mna creator chest unlock          → déverrouille le coffre visé
 * /mna creator chest lock            → verrouille le coffre visé
 * </pre>
 *
 * <p>Toutes ces commandes nécessitent le niveau de permission 2 (op).</p>
 */
public class CreatorCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("creator").requires(src -> src.hasPermission(2))

                    // ── /mna creator tool ──────────────────────────────────
                    .then(Commands.literal("tool")
                        .then(Commands.literal("scanner")
                            .executes(CreatorCommands::giveScannerWand))
                        .then(Commands.literal("placer")
                            .executes(CreatorCommands::givePlacerWand))
                    )

                    // ── /mna creator structure ─────────────────────────────
                    .then(Commands.literal("structure")
                        .then(Commands.literal("save")
                            .then(Commands.argument("id", StringArgumentType.greedyString())
                                .executes(ctx -> saveStructure(ctx,
                                    StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("list")
                            .executes(CreatorCommands::listStructures))
                        .then(Commands.literal("place")
                            .then(Commands.argument("id", StringArgumentType.greedyString())
                                .executes(ctx -> placeStructure(ctx,
                                    StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("delete")
                            .then(Commands.argument("id", StringArgumentType.greedyString())
                                .executes(ctx -> deleteStructure(ctx,
                                    StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("info")
                            .then(Commands.argument("id", StringArgumentType.greedyString())
                                .executes(ctx -> structureInfo(ctx,
                                    StringArgumentType.getString(ctx, "id")))))
                    )

                    // ── /mna creator selection ─────────────────────────────
                    .then(Commands.literal("selection")
                        .then(Commands.literal("clear")
                            .executes(CreatorCommands::clearSelection))
                        .then(Commands.literal("info")
                            .executes(CreatorCommands::selectionInfo))
                    )

                    // ── /mna creator chest ─────────────────────────────────
                    .then(Commands.literal("chest")
                        .then(Commands.literal("unlock")
                            .executes(CreatorCommands::unlockChest))
                        .then(Commands.literal("lock")
                            .executes(CreatorCommands::lockChest))
                    )

                    // ── /mna creator generation ────────────────────────────
                    .then(Commands.literal("generation")
                        .then(Commands.literal("on")
                            .executes(ctx -> setGeneration(ctx, true)))
                        .then(Commands.literal("off")
                            .executes(ctx -> setGeneration(ctx, false)))
                        .then(Commands.literal("status")
                            .executes(CreatorCommands::generationStatus))
                    );
    }

    // ── Tool ─────────────────────────────────────────────────────────────────

    private static int giveScannerWand(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MillItems.STRUCTURE_SCANNER));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.success_prefix")
            .append(Component.translatable("chat.millenaire-new-age.creator.scanner_tool")));
        return 1;
    }

    private static int givePlacerWand(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MillItems.STRUCTURE_PLACER));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.success_prefix")
            .append(Component.translatable("chat.millenaire-new-age.creator.placer_tool")));
        return 1;
    }

    // ── Structure save ────────────────────────────────────────────────────────

    private static int saveStructure(CommandContext<CommandSourceStack> ctx, String structureId) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        CreatorSession session = CreatorSession.get(player);
        if (!session.hasSelection()) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.error_prefix")
                .append(Component.translatable("chat.millenaire-new-age.creator.no_selection")));
            return 0;
        }

        try {
            java.nio.file.Path saved = StructureSaveManager.saveStructure(
                player.level().getServer(), player.level(), session, structureId);

            Vec3i size = session.getSize();
            String langKey = "structure.millenaire-new-age." + structureId.replace('/', '.');

            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.success_prefix")
                .append(Component.translatable("chat.millenaire-new-age.creator.save_success")));
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.save_file", Component.literal(saved.toString())));
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.save_size",
                size.getX(), size.getY(), size.getZ()));
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.save_lang", langKey));
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.save_instruction_lang"));
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.save_instruction_data", structureId));
            return 1;

        } catch (Exception e) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.error_prefix")
                .append(Component.translatable("chat.millenaire-new-age.creator.save_error", e.getMessage())));
            return 0;
        }
    }

    // ── Structure list ────────────────────────────────────────────────────────

    private static int listStructures(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        List<String> ids = StructureSaveManager.listStructures(player.level().getServer());

        if (ids.isEmpty()) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.prefix")
                .append(Component.translatable("chat.millenaire-new-age.creator.list_empty")));
            return 0;
        }

        // Envoyer les IDs au client pour ouvrir la GUI
        ServerPlayNetworking.send(player, new OpenStructureListPayload(ids));
        return ids.size();
    }

    // ── Structure place ───────────────────────────────────────────────────────

    private static int placeStructure(CommandContext<CommandSourceStack> ctx, String structureId) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        List<BlockPos> blocks = StructureSaveManager.loadBlockPositions(player.level().getServer(), structureId);
        if (blocks.isEmpty()) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.error_prefix")
                .append(Component.translatable("chat.millenaire-new-age.creator.not_found", structureId)));
            return 0;
        }

        Vec3i size = MillNetwork.computeSizePublic(blocks);

        // Configurer la baguette
        ItemStack stack = new ItemStack(MillItems.STRUCTURE_PLACER);
        StructurePlacerItem.setStructureId(stack, structureId);
        StructurePlacerItem.setRotation(stack, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        // Envoyer le preview au client
        ServerPlayNetworking.send(player, new StructurePreviewPayload(structureId, blocks, size));

        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.success_prefix")
            .append(Component.translatable("chat.millenaire-new-age.creator.placer_configured", structureId)));
        return 1;
    }

    // ── Structure delete ──────────────────────────────────────────────────────

    private static int deleteStructure(CommandContext<CommandSourceStack> ctx, String structureId) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        String sanitized = structureId.replace(':', '/');
        java.nio.file.Path base = StructureSaveManager.creatorOutputDir()
            .resolve("creator_structures/" + sanitized);

        try {
            boolean deleted = java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(base + ".nbt"));
            if (java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(base + "_blocks.json"))) deleted = true;

            if (deleted) {
                player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.success_prefix")
                    .append(Component.translatable("chat.millenaire-new-age.creator.deleted", structureId)));
            } else {
                player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.prefix")
                    .append(Component.translatable("chat.millenaire-new-age.creator.delete_not_found", structureId)));
            }
        } catch (Exception e) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.error_prefix")
                .append(Component.translatable("chat.millenaire-new-age.creator.error", e.getMessage())));
            return 0;
        }
        return 1;
    }

    // ── Structure info ────────────────────────────────────────────────────────

    private static int structureInfo(CommandContext<CommandSourceStack> ctx, String structureId) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        List<BlockPos> blocks = StructureSaveManager.loadBlockPositions(player.level().getServer(), structureId);
        if (blocks.isEmpty()) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.error_prefix")
                .append(Component.translatable("chat.millenaire-new-age.creator.not_found", structureId)));
            return 0;
        }

        Vec3i size = MillNetwork.computeSizePublic(blocks);
        String langKey  = "structure.millenaire-new-age." + structureId.replace('/', '.');
        String enName   = StructureSaveManager.autoName(structureId, false);
        String frName   = StructureSaveManager.autoName(structureId, true);

        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.info_title", structureId));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.info_size",
            size.getX(), size.getY(), size.getZ()));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.info_blocks", blocks.size()));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.info_lang", langKey));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.info_name_en", enName));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.info_name_fr", frName));
        return 1;
    }

    // ── Selection ─────────────────────────────────────────────────────────────

    private static int clearSelection(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;
        CreatorSession.get(player).clearSelection();
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.success_prefix")
            .append(Component.translatable("chat.millenaire-new-age.creator.selection_reset")));
        return 1;
    }

    private static int selectionInfo(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        CreatorSession session = CreatorSession.get(player);
        if (!session.hasSelection()) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.prefix")
                .append(Component.translatable("chat.millenaire-new-age.creator.no_active_selection")));
            return 0;
        }

        BlockPos min  = session.getMinPos();
        BlockPos max  = session.getMaxPos();
        Vec3i    size = session.getSize();

        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.selection_info_title"));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.selection_pos1", min.toShortString()));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.selection_pos2", max.toShortString()));
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.creator.selection_size",
            size.getX(), size.getY(), size.getZ(), (size.getX() * size.getY() * size.getZ())));
        return 1;
    }

    // ── Chest ─────────────────────────────────────────────────────────────────

    private static int unlockChest(CommandContext<CommandSourceStack> ctx) {
        return setChestLock(ctx, false);
    }

    private static int lockChest(CommandContext<CommandSourceStack> ctx) {
        return setChestLock(ctx, true);
    }

    private static int setChestLock(CommandContext<CommandSourceStack> ctx, boolean lock) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        // Trouver le coffre que le joueur regarde (rayon 5 blocs)
        var hitResult = player.pick(5.0, 1.0f, false);
        if (!(hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit)) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.error_prefix")
                .append(Component.literal("No block in sight.")));
            return 0;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof MillChestBlockEntity chest)) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.error_prefix")
                .append(Component.literal("Not a Millenaire chest.")));
            return 0;
        }

        if (lock) {
            player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.error_prefix")
                .append(Component.literal("Cannot lock chest from command (use building scan).")));
            return 0;
        }

        // Déverrouiller : retirer le lien au bâtiment
        chest.setBuildingId(null);
        player.sendSystemMessage(Component.translatable("chat.millenaire-new-age.success_prefix")
            .append(Component.literal("Chest unlocked.")));
        return 1;
    }

    // ── Generation ────────────────────────────────────────────────────────────

    private static int setGeneration(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        VillageConfig.naturalVillageGeneration = enabled;
        VillageConfig.save();
        String stateKey = enabled
            ? "chat.millenaire-new-age.creator.generation_on"
            : "chat.millenaire-new-age.creator.generation_off";
        ctx.getSource().sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.success_prefix")
                    .append(Component.translatable(stateKey)),
            true);
        return 1;
    }

    private static int generationStatus(CommandContext<CommandSourceStack> ctx) {
        boolean enabled = VillageConfig.naturalVillageGeneration;
        String stateKey = enabled
            ? "chat.millenaire-new-age.creator.generation_on"
            : "chat.millenaire-new-age.creator.generation_off";
        ctx.getSource().sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.prefix")
                    .append(Component.translatable("chat.millenaire-new-age.creator.generation_status",
                            Component.translatable(stateKey))),
            false);
        return enabled ? 1 : 0;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static ServerPlayer getPlayer(CommandContext<CommandSourceStack> ctx) {
        try {
            return ctx.getSource().getPlayerOrException();
        } catch (Exception e) {
            return null;
        }
    }
}
