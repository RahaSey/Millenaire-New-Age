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
                        entries.accept(MillItems.TIMBER_FRAME_PLAIN);
                        entries.accept(MillItems.TIMBER_FRAME_CROSS);
                        entries.accept(MillItems.STAINED_GLASS_WHITE);
                        entries.accept(MillItems.STAINED_GLASS_YELLOW);
                        entries.accept(MillItems.BED_STRAW);

                        // ── Outils ────────────────────────────────────────────
                        entries.accept(MillItems.WAND_OF_SUMMONING);
                        entries.accept(MillItems.WAND_OF_NEGATION);

                        // ── Outils créateur ───────────────────────────────────
                        entries.accept(MillItems.STRUCTURE_SCANNER);
                        entries.accept(MillItems.STRUCTURE_PLACER);
                    })
                    .build()
    );

    public static void initialize() {}
}
