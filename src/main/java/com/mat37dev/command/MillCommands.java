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
 *
 * Phase 2 :
 *   /mna civilization list           — liste toutes les civs chargées
 *   /mna civilization info <id>      — détails d'une civ
 *
 * Futur (Phase 3+) :
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
                    () -> Component.literal("§eAucune civilisation chargée."), false);
            return 0;
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("§6=== Civilisations chargées (" + all.size() + ") ==="), false);

        for (Civilization civ : all) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("§a• §f" + civ.id() + " §7— " + civ.displayName()), false);
        }
        return all.size();
    }

    // ── /mna civilization info <id> ───────────────────────────────────────────

    private static int civilizationInfo(CommandContext<CommandSourceStack> ctx, String id) {
        Optional<Civilization> found = CivilizationRegistry.get(id);

        if (found.isEmpty()) {
            ctx.getSource().sendFailure(
                    Component.literal("Civilisation inconnue : " + id));
            return 0;
        }

        Civilization civ = found.get();
        ctx.getSource().sendSuccess(
                () -> Component.literal("§6=== " + civ.displayName() + " [" + civ.id() + "] ==="), false);
        ctx.getSource().sendSuccess(
                () -> Component.literal("§7Biomes : §f" + String.join(", ", civ.compatibleBiomes())), false);
        ctx.getSource().sendSuccess(
                () -> Component.literal("§7Types de villages : §f" +
                        civ.villageTypes().stream().map(v -> v.id()).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b)), false);
        ctx.getSource().sendSuccess(
                () -> Component.literal("§7Bâtiments : §f" + civ.buildingTypes().size()), false);
        ctx.getSource().sendSuccess(
                () -> Component.literal("§7Villageois : §f" + civ.villagerTypes().size()), false);
        ctx.getSource().sendSuccess(
                () -> Component.literal("§7Commerce : §f" + civ.tradeGoods().size() + " bien(s)"), false);
        ctx.getSource().sendSuccess(
                () -> Component.literal("§7Cultures : §f" + String.join(", ", civ.knownCrops())), false);
        ctx.getSource().sendSuccess(
                () -> Component.literal("§7Langue : §f" + civ.language().id() +
                        " (" + civ.language().maleFirstNames().size() + " prénoms masc., " +
                        civ.language().femaleFirstNames().size() + " prénoms fém., " +
                        civ.language().villageNames().size() + " noms de village)"), false);

        return 1;
    }
}
