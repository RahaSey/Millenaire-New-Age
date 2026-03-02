package com.mat37dev.command;

import com.mat37dev.village.Village;
import com.mat37dev.village.VillageManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Commandes de debug villages : /mna village ...
 *
 * <ul>
 *   <li>/mna village list              — liste tous les villages du monde courant</li>
 *   <li>/mna village info [nom]        — infos sur le village le plus proche (ou par nom)</li>
 *   <li>/mna village tp &lt;nom&gt;    — téléporter vers un village</li>
 *   <li>/mna village remove &lt;nom&gt;— supprimer un village (debug)</li>
 * </ul>
 *
 * <p>Les noms de village sont uniques par monde — ils servent d'identifiants dans les commandes.</p>
 */
public class VillageCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("village")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.literal("list")
                        .executes(ctx -> listVillages(ctx.getSource())))
                    .then(Commands.literal("info")
                        .executes(ctx -> infoNearestVillage(ctx.getSource()))
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                            .suggests(VillageCommands::suggestVillageNames)
                            .executes(ctx -> infoVillage(
                                ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                    .then(Commands.literal("tp")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                            .suggests(VillageCommands::suggestVillageNames)
                            .executes(ctx -> tpVillage(
                                ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                    .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                            .suggests(VillageCommands::suggestVillageNames)
                            .executes(ctx -> removeVillage(
                                ctx.getSource(), StringArgumentType.getString(ctx, "name")))));
    }

    // ── Autocomplétion ────────────────────────────────────────────────────────

    private static CompletableFuture<Suggestions> suggestVillageNames(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        ServerLevel level = ctx.getSource().getLevel();
        String prefix = builder.getRemaining().toLowerCase();
        VillageManager.getAllVillages(level).forEach(v -> {
            if (v.getName().toLowerCase().startsWith(prefix)) {
                builder.suggest(v.getName());
            }
        });
        return builder.buildFuture();
    }

    // ── /mna village list ─────────────────────────────────────────────────────

    private static int listVillages(CommandSourceStack source) {
        Collection<Village> villages = VillageManager.getAllVillages(source.getLevel());

        if (villages.isEmpty()) {
            source.sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.village.cmd.list_empty"),
                false);
            return 0;
        }

        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.list_header",
                Component.literal(String.valueOf(villages.size()))),
            false);

        for (Village v : villages) {
            BlockPos c = v.getCenter();
            source.sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.village.cmd.list_entry",
                    Component.literal(v.getName()),
                    Component.literal(v.getCultureId()),
                    Component.literal(v.getVillageTypeId()),
                    Component.literal(c.getX() + ", " + c.getY() + ", " + c.getZ())),
                false);
        }
        return villages.size();
    }

    // ── /mna village info [nom] ───────────────────────────────────────────────

    private static int infoNearestVillage(CommandSourceStack source) {
        Collection<Village> villages = VillageManager.getAllVillages(source.getLevel());

        if (villages.isEmpty()) {
            source.sendSuccess(
                () -> Component.translatable("chat.millenaire-new-age.village.cmd.list_empty"),
                false);
            return 0;
        }

        Vec3 pos = source.getPosition();
        Village nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Village v : villages) {
            BlockPos c = v.getCenter();
            double dist = pos.distanceToSqr(c.getX() + 0.5, c.getY(), c.getZ() + 0.5);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = v;
            }
        }

        if (nearest == null) return 0;
        return printVillageInfo(source, nearest);
    }

    private static int infoVillage(CommandSourceStack source, String name) {
        Optional<Village> village = resolveVillageByName(source.getLevel(), name);
        if (village.isEmpty()) {
            source.sendFailure(Component.translatable(
                "chat.millenaire-new-age.village.cmd.not_found",
                Component.literal(name)));
            return 0;
        }
        return printVillageInfo(source, village.get());
    }

    private static int printVillageInfo(CommandSourceStack source, Village v) {
        BlockPos c = v.getCenter();
        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.info_header",
                Component.literal(v.getName())),
            false);
        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.info_id",
                Component.literal(v.getId().toString())),
            false);
        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.info_culture",
                Component.literal(v.getCultureId())),
            false);
        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.info_type",
                Component.literal(v.getVillageTypeId())),
            false);
        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.info_pos",
                Component.literal(c.getX() + ", " + c.getY() + ", " + c.getZ())),
            false);
        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.info_state",
                Component.literal(v.getState().name())),
            false);
        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.info_buildings",
                Component.literal(String.valueOf(v.getBuildings().size()))),
            false);
        return 1;
    }

    // ── /mna village tp <nom> ─────────────────────────────────────────────────

    private static int tpVillage(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command requires a player."));
            return 0;
        }

        Optional<Village> village = resolveVillageByName(source.getLevel(), name);
        if (village.isEmpty()) {
            source.sendFailure(Component.translatable(
                "chat.millenaire-new-age.village.cmd.not_found",
                Component.literal(name)));
            return 0;
        }

        Village v = village.get();
        BlockPos c = v.getCenter();
        String villageName = v.getName();
        player.teleportTo(c.getX() + 0.5, c.getY(), c.getZ() + 0.5);
        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.tp_success",
                Component.literal(villageName)),
            false);
        return 1;
    }

    // ── /mna village remove <nom> ─────────────────────────────────────────────

    private static int removeVillage(CommandSourceStack source, String name) {
        ServerLevel level = source.getLevel();
        Optional<Village> village = resolveVillageByName(level, name);
        if (village.isEmpty()) {
            source.sendFailure(Component.translatable(
                "chat.millenaire-new-age.village.cmd.not_found",
                Component.literal(name)));
            return 0;
        }

        Village v = village.get();
        String villageName = v.getName();
        BlockPos c = v.getCenter();

        // Supprimer l'ArmorStand indicateur de nom.
        // Il est spawné 6 blocs au-dessus du centre (goldPos.getY() + 6.0),
        // donc l'AABB doit couvrir la plage Y+0 à Y+8.
        AABB searchBox = new AABB(
            c.getX() - 1, c.getY() - 1, c.getZ() - 1,
            c.getX() + 2, c.getY() + 8, c.getZ() + 2);
        level.getEntitiesOfClass(ArmorStand.class, searchBox,
                Entity::hasCustomName).forEach(Entity::discard);

        VillageManager.removeVillage(level, v.getId());
        source.sendSuccess(
            () -> Component.translatable("chat.millenaire-new-age.village.cmd.remove_success",
                Component.literal(villageName)),
            false);
        return 1;
    }

    // ── Résolution par nom ────────────────────────────────────────────────────

    /**
     * Résout un village par son nom exact (insensible à la casse),
     * ou par correspondance partielle de début de nom.
     */
    private static Optional<Village> resolveVillageByName(ServerLevel level, String nameStr) {
        Optional<Village> exact = VillageManager.getVillageByName(level, nameStr);
        if (exact.isPresent()) return exact;

        String lower = nameStr.toLowerCase();
        return VillageManager.getAllVillages(level).stream()
            .filter(v -> v.getName().toLowerCase().startsWith(lower))
            .findFirst();
    }
}
