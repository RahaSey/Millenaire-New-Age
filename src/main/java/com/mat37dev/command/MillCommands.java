package com.mat37dev.command;

import com.mat37dev.culture.Culture;
import com.mat37dev.culture.CultureRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.Optional;

/**
 * Commandes de debug préfixées /mna.
 * <p>
 * Phase 2 :
 *   /mna culture list — liste toutes les cultures chargées
 *   /mna culture info <id>      — détails d'une culture
 */
public class MillCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("mna")
                .then(Commands.literal("help")
                    .executes(MillCommands::sendHelp))
                .then(CreatorCommands.build())
                .then(VillageCommands.build())
                .then(Commands.literal("culture")
                    .then(Commands.literal("list")
                        .requires(src -> src.hasPermission(2))
                        .executes(MillCommands::listCultures))
                    .then(Commands.literal("info")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx ->
                                cultureInfo(ctx, StringArgumentType.getString(ctx, "id"))))))
        );
    }

    // ── /mna help ─────────────────────────────────────────────────────────────

    private static int sendHelp(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.title"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_help"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_culture_list"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_culture_info"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_village_list"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_village_info"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_village_tp"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_village_remove"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_tool_scanner"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_tool_placer"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_structure_save"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_structure_list"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_structure_place"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_structure_delete"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_structure_info"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_selection_clear"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_selection_info"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.millenaire-new-age.command.help.mna_creator_generation"), false);
        return 1;
    }

    // ── /mna culture list ─────────────────────────────────────────────────────

    private static int listCultures(CommandContext<CommandSourceStack> ctx) {
        Collection<Culture> all = CultureRegistry.getAll();

        if (all.isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("chat.millenaire-new-age.command.no_culture_loaded"), false);
            return 0;
        }

        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.culture_list_title", all.size()), false);

        for (Culture culture : all) {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("chat.millenaire-new-age.command.culture_list_item",
                            culture.id(), Component.translatable("culture.millenaire-new-age." + culture.id())), false);
        }
        return all.size();
    }

    // ── /mna culture info <id> ────────────────────────────────────────────────

    private static int cultureInfo(CommandContext<CommandSourceStack> ctx, String id) {
        Optional<Culture> found = CultureRegistry.get(id);

        if (found.isEmpty()) {
            ctx.getSource().sendFailure(
                    Component.translatable("chat.millenaire-new-age.command.culture_unknown", id));
            return 0;
        }

        Culture culture = found.get();
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.culture_info_title",
                        Component.translatable("culture.millenaire-new-age." + culture.id()), culture.id()), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.culture_info_biomes",
                        String.join(", ", culture.compatibleBiomes())), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.culture_info_village_types",
                        culture.villageTypes().stream()
                                .map(vt -> Component.translatable("village_type.millenaire-new-age." + culture.id() + "." + vt.id().split(":")[1]).getString())
                                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b)), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.culture_info_buildings", culture.buildingTypes().size()), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.culture_info_villagers", culture.villagerTypes().size()), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.culture_info_trade", culture.tradeGoods().size()), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.culture_info_crops", String.join(", ", culture.knownCrops())), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.culture_info_lang", culture.language().id(),
                        culture.language().maleFirstNames().size(),
                        culture.language().femaleFirstNames().size(),
                        culture.language().villageNames().size()), false);

        return 1;
    }
}
