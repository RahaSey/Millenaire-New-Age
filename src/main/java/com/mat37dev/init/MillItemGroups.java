package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Creative mode tabs for Millenaire: New Age.
 *
 * A single unified tab groups all mod content, organized by civilization then
 * material family. When a new civilization is added, append its blocks at the
 * end of the relevant section (or add a new civilization block at the bottom).
 *
 * Organisation within the Norman section:
 *   1. Stone (pierre) — cobblestone → bricks → mud → cooked, with stairs/slabs/walls
 *   2. Timber frame (charpente) — colombage, barreaux, variantes
 *   3. Thatch (chaume) — bloc, escalier, dalle
 *   4. Decorative (décoratif) — rosette
 *   5. Paths (chemins) — terre, gravier, dalles
 *   6. Functional (fonctionnels) — foyer, coffre, lit
 *   7. Vegetation (végétation) — pommier, vigne
 */
public class MillItemGroups {

    public static final CreativeModeTab MILLENAIRE_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "millenaire"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(MillItems.TIMBERFRAME))
                    .title(Component.translatable("itemGroup.millenaire-new-age.millenaire"))
                    .displayItems((context, entries) -> {

                        // ── Normands — Pierre (pierre normande) ──────────────
                        entries.accept(MillItems.NORMAN_COBBLESTONE);
                        entries.accept(MillItems.NORMAN_COBBLESTONE_STAIRS);
                        entries.accept(MillItems.NORMAN_COBBLESTONE_SLAB);
                        entries.accept(MillItems.NORMAN_COBBLESTONE_WALL);

                        entries.accept(MillItems.NORMAN_BRICKS);
                        entries.accept(MillItems.NORMAN_BRICKS_STAIRS);
                        entries.accept(MillItems.NORMAN_BRICKS_SLAB);
                        entries.accept(MillItems.NORMAN_BRICKS_WALL);

                        entries.accept(MillItems.WET_BRICK);
                        entries.accept(MillItems.MUD_BRICK);
                        entries.accept(MillItems.MUD_BRICK_STAIRS);
                        entries.accept(MillItems.MUD_BRICK_SLAB);
                        entries.accept(MillItems.MUD_BRICK_WALL);

                        entries.accept(MillItems.COOKED_BRICK);
                        entries.accept(MillItems.COOKED_BRICK_STAIRS);
                        entries.accept(MillItems.COOKED_BRICK_SLAB);
                        entries.accept(MillItems.COOKED_BRICK_WALL);

                        // ── Normands — Charpente (timber frame) ──────────────
                        entries.accept(MillItems.TIMBERFRAME);
                        entries.accept(MillItems.TIMBERFRAME_CROSS);
                        entries.accept(MillItems.TIMBERFRAME_STAIRS);
                        entries.accept(MillItems.TIMBERFRAME_SLAB);
                        entries.accept(MillItems.WOODEN_BARS);
                        entries.accept(MillItems.WOODEN_BARS_DARK);

                        // ── Normands — Chaume (thatch) ────────────────────────
                        entries.accept(MillItems.THATCH);
                        entries.accept(MillItems.THATCH_STAIRS);
                        entries.accept(MillItems.THATCH_SLAB);

                        // ── Normands — Décoratif ──────────────────────────────
                        entries.accept(MillItems.ROSETTE);

                        // ── Chemins ───────────────────────────────────────────
                        entries.accept(MillItems.PATH_DIRT);
                        entries.accept(MillItems.PATH_DIRT_SLAB);
                        entries.accept(MillItems.PATH_GRAVEL);
                        entries.accept(MillItems.PATH_GRAVEL_SLAB);

                        // ── Fonctionnels ──────────────────────────────────────
                        entries.accept(MillItems.FIRE_PIT);
                        entries.accept(MillItems.LOCKED_CHEST);
                        entries.accept(MillItems.STRAW_BED);

                        // ── Végétation ────────────────────────────────────────
                        entries.accept(MillItems.SAPLING_APPLETREE);
                        entries.accept(MillItems.LEAVES_APPLETREE);
                        entries.accept(MillItems.GRAPE_VINE);
                    })
                    .build()
    );

    public static void initialize() {}
}
