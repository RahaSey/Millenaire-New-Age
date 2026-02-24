package com.mat37dev.command;

import com.mat37dev.civilization.Civilization;
import com.mat37dev.civilization.CivilizationRegistry;
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
 *   /mna civilization list           — liste toutes les civs chargées
 *   /mna civilization info <id>      — détails d'une civ
 * <p>
 * Futur (Phase 3+):
 *   /mna village list | spawn | tp | info
 *   /mna reputation set ...
 *   /mna creator ...
 */
public class MillCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Sous-commandes creator (arbre séparé mais même racine /mna)
        CreatorCommands.register(dispatcher);

        dispatcher.register(
            Commands.literal("mna")
                .then(Commands.literal("civilization")
                    .then(Commands.literal("list")
                        .requires(src -> src.hasPermission(2))
                        .executes(MillCommands::listCivilizations))
                    .then(Commands.literal("info")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx ->
                                civilizationInfo(ctx, StringArgumentType.getString(ctx, "id"))))))
        );
    }

    // ── /mna civilization list ────────────────────────────────────────────────

    private static int listCivilizations(CommandContext<CommandSourceStack> ctx) {
        Collection<Civilization> all = CivilizationRegistry.getAll();

        if (all.isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("chat.millenaire-new-age.command.no_civ_loaded"), false);
            return 0;
        }

        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.civ_list_title", all.size()), false);

        for (Civilization civ : all) {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("chat.millenaire-new-age.command.civ_list_item",
                            civ.id(), Component.translatable("civilization.millenaire-new-age." + civ.id())), false);
        }
        return all.size();
    }

    // ── /mna civilization info <id> ───────────────────────────────────────────

    private static int civilizationInfo(CommandContext<CommandSourceStack> ctx, String id) {
        Optional<Civilization> found = CivilizationRegistry.get(id);

        if (found.isEmpty()) {
            ctx.getSource().sendFailure(
                    Component.translatable("chat.millenaire-new-age.command.civ_unknown", id));
            return 0;
        }

        Civilization civ = found.get();
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.civ_info_title",
                        Component.translatable("civilization.millenaire-new-age." + civ.id()), civ.id()), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.civ_info_biomes",
                        String.join(", ", civ.compatibleBiomes())), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.civ_info_village_types",
                        civ.villageTypes().stream()
                                .map(vt -> Component.translatable("village_type.millenaire-new-age." + civ.id() + "." + vt.id().split(":")[1]).getString())
                                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b)), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.civ_info_buildings", civ.buildingTypes().size()), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.civ_info_villagers", civ.villagerTypes().size()), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.civ_info_trade", civ.tradeGoods().size()), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.civ_info_crops", String.join(", ", civ.knownCrops())), false);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.command.civ_info_lang", civ.language().id(),
                        civ.language().maleFirstNames().size(),
                        civ.language().femaleFirstNames().size(),
                        civ.language().villageNames().size()), false);

        return 1;
    }
}
