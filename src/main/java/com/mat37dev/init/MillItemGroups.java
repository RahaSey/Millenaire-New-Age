package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class MillItemGroups {

    public static final CreativeModeTab MILLENAIRE_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "millenaire"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(MillItems.PATH_GRAVEL))
                    .title(Component.translatable("itemGroup.millenaire-new-age.millenaire"))
                    .displayItems((context, entries) -> {
                        // ── Chemins ───────────────────────────────────────────
                        entries.accept(MillItems.PATH_GRAVEL);
                        entries.accept(MillItems.PATH_GRAVEL_SLAB);
                        entries.accept(MillItems.PATH_DIRT);
                        entries.accept(MillItems.PATH_DIRT_SLAB);
                        entries.accept(MillItems.PATH_SLABS);
                        entries.accept(MillItems.PATH_SLABS_SLAB);
                        entries.accept(MillItems.DIRT_WALL);
                        entries.accept(MillItems.BED_STRAW);

                        // ── Stockage ──────────────────────────────────────────
                        entries.accept(MillItems.MILL_CHEST);

                        // ── Outils ────────────────────────────────────────────
                        entries.accept(MillItems.WAND_OF_SUMMONING);
                        entries.accept(MillItems.WAND_OF_NEGATION);
                    })
                    .build()
    );

    public static final CreativeModeTab MILLENAIRE_NORMAN = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "norman"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(MillItems.NORMAN_BROADSWORD))
                    .title(Component.literal("Norman"))
                    .displayItems((context, entries) -> {

                        // Norman Tools
                        entries.accept(MillItems.NORMAN_PICKAXE);
                        entries.accept(MillItems.NORMAN_AXE);
                        entries.accept(MillItems.NORMAN_SHOVEL);
                        entries.accept(MillItems.NORMAN_HOE);
                        entries.accept(MillItems.NORMAN_BROADSWORD);

                        // Norman Armor
                        entries.accept(MillItems.NORMAN_HELMET);
                        entries.accept(MillItems.NORMAN_PLATE);
                        entries.accept(MillItems.NORMAN_LEGS);
                        entries.accept(MillItems.NORMAN_BOOTS);

                        // Norman Foods
                        entries.accept(MillItems.CIDER_APPLE);
                        entries.accept(MillItems.CIDER);
                        entries.accept(MillItems.BOUDIN);
                        entries.accept(MillItems.CALVA);
                        entries.accept(MillItems.TRIPES);

                        // Blocks
                        entries.accept(MillItems.TIMBER_FRAME_PLAIN);
                        entries.accept(MillItems.TIMBER_FRAME_CROSS);
                        entries.accept(MillItems.STAINED_GLASS_WHITE);
                        entries.accept(MillItems.STAINED_GLASS_YELLOW);
                        entries.accept(MillItems.STAINED_GLASS_YELLOW_RED);
                        entries.accept(MillItems.STAINED_GLASS_RED_BLUE);
                        entries.accept(MillItems.STAINED_GLASS_GREEN_BLUE);
                    }).build()
    );

    public static final CreativeModeTab CONTENT_CREATOR_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "content_creator"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(MillItems.IMPORT_TABLE))
                    .title(Component.translatable("itemGroup.millenaire-new-age.content_creator"))
                    .displayItems((context, entries) -> {
                        // ── Table d'importation ────────────────────────────────
                        entries.accept(MillItems.IMPORT_TABLE);
                        entries.accept(MillItems.PRESERVE_GROUND);

                        // ── Positions ─────────────────────────────────────────
                        entries.accept(MillItems.SLEEPING_POS);
                        entries.accept(MillItems.SELLING_POS);
                        entries.accept(MillItems.CRAFTING_POS);
                        entries.accept(MillItems.DEFENDING_POS);
                        entries.accept(MillItems.SHELTER_POS);
                        entries.accept(MillItems.LEISURE_POS);
                        entries.accept(MillItems.STALL_POS);
                        entries.accept(MillItems.PATH_START_POS);

                        // ── Plantations ───────────────────────────────────────
                        entries.accept(MillItems.TREE_OAK_SPAWN);
                        entries.accept(MillItems.TREE_DARK_OAK_SPAWN);
                        entries.accept(MillItems.TREE_SPRUCE_SPAWN);

                        // ── Spawns Animaux ────────────────────────────────────
                        entries.accept(MillItems.SPAWN_COW);
                        entries.accept(MillItems.SPAWN_SHEEP);
                        entries.accept(MillItems.SPAWN_CHICKEN);
                        entries.accept(MillItems.SPAWN_PIG);

                        // ── Sources et Sols ───────────────────────────────────
                        entries.accept(MillItems.SOURCE_ROCK);
                        entries.accept(MillItems.SOURCE_SAND);
                        entries.accept(MillItems.SOIL_FLOWER);
                        entries.accept(MillItems.SOIL_WHEAT);
                        entries.accept(MillItems.SOIL_CARROT);
                        entries.accept(MillItems.SOIL_POTATO);

                        // ── Baguettes créateur ────────────────────────────────
                        entries.accept(MillItems.STRUCTURE_SCANNER);
                        entries.accept(MillItems.STRUCTURE_PLACER);
                    })
                    .build()
    );

    public static void initialize() {}
}
