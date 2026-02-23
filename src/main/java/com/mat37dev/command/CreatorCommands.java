package com.mat37dev.command;

import com.mat37dev.creator.CreatorSession;
import com.mat37dev.creator.StructurePlacerItem;
import com.mat37dev.creator.StructureSaveManager;
import com.mat37dev.init.MillItems;
import com.mat37dev.network.MillNetwork;
import com.mat37dev.network.OpenStructureListPayload;
import com.mat37dev.network.StructurePreviewPayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
 * </pre>
 *
 * <p>Toutes ces commandes nécessitent le niveau de permission 2 (op).</p>
 */
public class CreatorCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("mna")
                .then(Commands.literal("creator").requires(src -> src.hasPermission(2))

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
                )
        );
    }

    // ── Tool ─────────────────────────────────────────────────────────────────

    private static int giveScannerWand(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MillItems.STRUCTURE_SCANNER));
        player.sendSystemMessage(Component.literal("§a[MNA] Baguette d'Arpentage — Clic gauche = Pos1, Clic droit = Pos2"));
        return 1;
    }

    private static int givePlacerWand(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MillItems.STRUCTURE_PLACER));
        player.sendSystemMessage(Component.literal("§a[MNA] Baguette de Placement — Utilisez /mna creator structure place <id>"));
        return 1;
    }

    // ── Structure save ────────────────────────────────────────────────────────

    private static int saveStructure(CommandContext<CommandSourceStack> ctx, String structureId) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        CreatorSession session = CreatorSession.get(player);
        if (!session.hasSelection()) {
            player.sendSystemMessage(Component.literal(
                "§c[MNA] Aucune sélection active. Utilisez la Baguette d'Arpentage d'abord."));
            return 0;
        }

        try {
            java.nio.file.Path saved = StructureSaveManager.saveStructure(
                player.level().getServer(), player.level(), session, structureId);

            Vec3i size = session.getSize();
            String langKey = "structure.millenaire-new-age." + structureId.replace('/', '.');

            player.sendSystemMessage(Component.literal("§a[MNA] ✅ Structure sauvegardée !"));
            player.sendSystemMessage(Component.literal("§7Fichier : §f" + saved));
            player.sendSystemMessage(Component.literal("§7Taille  : §f"
                + size.getX() + " × " + size.getY() + " × " + size.getZ()));
            player.sendSystemMessage(Component.literal("§7Clé lang : §f" + langKey));
            player.sendSystemMessage(Component.literal(
                "§7→ Copiez les entrées de §fmods/MillenaireNewAge/lang_additions.json§7 dans vos fichiers lang."));
            player.sendSystemMessage(Component.literal(
                "§7→ Copiez le .nbt dans §fsrc/main/resources/data/millenaire-new-age/structure/" + structureId + ".nbt"));
            return 1;

        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§c[MNA] Erreur lors de la sauvegarde : " + e.getMessage()));
            return 0;
        }
    }

    // ── Structure list ────────────────────────────────────────────────────────

    private static int listStructures(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        List<String> ids = StructureSaveManager.listStructures(player.level().getServer());

        if (ids.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                "§e[MNA] Aucune structure dans creator_output. Sauvegardez-en une d'abord."));
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
            player.sendSystemMessage(Component.literal(
                "§c[MNA] Structure '§f" + structureId + "§c' introuvable dans creator_output."));
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

        player.sendSystemMessage(Component.literal(
            "§a[MNA] Baguette configurée pour '§f" + structureId
            + "§a'. Clic droit = placer, Shift+clic = tourner."));
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
                player.sendSystemMessage(Component.literal("§a[MNA] Structure '§f" + structureId + "§a' supprimée."));
            } else {
                player.sendSystemMessage(Component.literal("§e[MNA] Structure '§f" + structureId + "§e' non trouvée."));
            }
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§c[MNA] Erreur : " + e.getMessage()));
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
            player.sendSystemMessage(Component.literal(
                "§c[MNA] Structure '§f" + structureId + "§c' introuvable."));
            return 0;
        }

        Vec3i size = MillNetwork.computeSizePublic(blocks);
        String langKey  = "structure.millenaire-new-age." + structureId.replace('/', '.');
        String enName   = StructureSaveManager.autoName(structureId, false);
        String frName   = StructureSaveManager.autoName(structureId, true);

        player.sendSystemMessage(Component.literal("§6=== " + structureId + " ==="));
        player.sendSystemMessage(Component.literal("§7Taille    : §f" + size.getX() + " × " + size.getY() + " × " + size.getZ()));
        player.sendSystemMessage(Component.literal("§7Blocs     : §f" + blocks.size()));
        player.sendSystemMessage(Component.literal("§7Clé lang  : §f" + langKey));
        player.sendSystemMessage(Component.literal("§7Nom EN    : §f" + enName));
        player.sendSystemMessage(Component.literal("§7Nom FR    : §f" + frName));
        return 1;
    }

    // ── Selection ─────────────────────────────────────────────────────────────

    private static int clearSelection(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;
        CreatorSession.get(player).clearSelection();
        player.sendSystemMessage(Component.literal("§a[MNA] Sélection réinitialisée."));
        return 1;
    }

    private static int selectionInfo(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) return 0;

        CreatorSession session = CreatorSession.get(player);
        if (!session.hasSelection()) {
            player.sendSystemMessage(Component.literal("§e[MNA] Aucune sélection active."));
            return 0;
        }

        BlockPos min  = session.getMinPos();
        BlockPos max  = session.getMaxPos();
        Vec3i    size = session.getSize();

        player.sendSystemMessage(Component.literal("§6=== Sélection courante ==="));
        player.sendSystemMessage(Component.literal("§7Pos1 (min) : §f" + min.toShortString()));
        player.sendSystemMessage(Component.literal("§7Pos2 (max) : §f" + max.toShortString()));
        player.sendSystemMessage(Component.literal("§7Taille     : §f"
            + size.getX() + " × " + size.getY() + " × " + size.getZ()
            + " §7(§f" + (size.getX() * size.getY() * size.getZ()) + " blocs§7)"));
        return 1;
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
