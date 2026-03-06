package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.creator.StructurePlacerItem;
import com.mat37dev.creator.StructureScannerItem;
import com.mat37dev.creator.WandOfSummoningItem;
import com.mat37dev.init.custom_classes.MillCustomMaterials;
import com.mat37dev.init.custom_classes.MillFoodItemBuilder;
import com.mat37dev.init.custom_classes.MillFoodItemBuilder.MillFoodType;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class MillItems {

    // =========================================================================
    // SHARED — Paths (BlockItems)
    // =========================================================================

    public static final Item PATH_GRAVEL      = registerBlockItem("path_gravel",      MillBlocks.PATH_GRAVEL);
    public static final Item PATH_GRAVEL_SLAB = registerBlockItem("path_gravel_slab", MillBlocks.PATH_GRAVEL_SLAB);
    public static final Item PATH_DIRT        = registerBlockItem("path_dirt",        MillBlocks.PATH_DIRT);
    public static final Item PATH_DIRT_SLAB   = registerBlockItem("path_dirt_slab",   MillBlocks.PATH_DIRT_SLAB);
    public static final Item DIRT_WALL        = registerBlockItem("dirt_wall",        MillBlocks.DIRT_WALL);
    public static final Item PATH_SLABS        = registerBlockItem("path_slabs",        MillBlocks.PATH_SLABS);
    public static final Item PATH_SLABS_SLAB   = registerBlockItem("path_slabs_slab",   MillBlocks.PATH_SLABS_SLAB);
    public static final Item BED_STRAW        = registerItem("bed_straw", props -> new BedItem(MillBlocks.BED_STRAW, props));
    public static final Item MILL_CHEST       = registerBlockItem("mill_chest",       MillBlocks.MILL_CHEST);
    public static final Item TIMBER_FRAME_PLAIN   = registerBlockItem("timber_frame_plain",   MillBlocks.TIMBER_FRAME_PLAIN);
    public static final Item TIMBER_FRAME_CROSS   = registerBlockItem("timber_frame_cross",   MillBlocks.TIMBER_FRAME_CROSS);
    public static final Item STAINED_GLASS_WHITE   = registerBlockItem("stained_glass_white",   MillBlocks.STAINED_GLASS_WHITE);
    public static final Item STAINED_GLASS_YELLOW   = registerBlockItem("stained_glass_yellow",   MillBlocks.STAINED_GLASS_YELLOW);
    public static final Item STAINED_GLASS_YELLOW_RED   = registerBlockItem("stained_glass_yellow_red",   MillBlocks.STAINED_GLASS_YELLOW_RED);
    public static final Item STAINED_GLASS_RED_BLUE   = registerBlockItem("stained_glass_red_blue",   MillBlocks.STAINED_GLASS_RED_BLUE);
    public static final Item STAINED_GLASS_GREEN_BLUE   = registerBlockItem("stained_glass_green_blue",   MillBlocks.STAINED_GLASS_GREEN_BLUE);

    // =========================================================================
    // TOOLS — Wands (pure Items)
    // =========================================================================

    public static final Item WAND_OF_SUMMONING  = registerItem("wand_of_summoning",  WandOfSummoningItem::new);
    public static final Item WAND_OF_NEGATION   = registerItem("wand_of_negation",   Item::new);

    // =========================================================================
    // CREATOR TOOLS — Baguettes créateur
    // =========================================================================

    public static final Item STRUCTURE_SCANNER = registerItem("structure_scanner", StructureScannerItem::new);
    public static final Item STRUCTURE_PLACER  = registerItem("structure_placer",  StructurePlacerItem::new);
    public static final Item PRESERVE_GROUND   = registerBlockItem("preserve_ground", MillBlocks.PRESERVE_GROUND);
    public static final Item SLEEPING_POS      = registerBlockItem("sleeping_pos",    MillBlocks.SLEEPING_POS);
    public static final Item SELLING_POS       = registerBlockItem("selling_pos",     MillBlocks.SELLING_POS);
    public static final Item CRAFTING_POS      = registerBlockItem("crafting_pos",    MillBlocks.CRAFTING_POS);
    public static final Item DEFENDING_POS     = registerBlockItem("defending_pos",   MillBlocks.DEFENDING_POS);
    public static final Item SHELTER_POS       = registerBlockItem("shelter_pos",     MillBlocks.SHELTER_POS);
    public static final Item LEISURE_POS       = registerBlockItem("leisure_pos",     MillBlocks.LEISURE_POS);
    public static final Item STALL_POS         = registerBlockItem("stall_pos",       MillBlocks.STALL_POS);
    public static final Item PATH_START_POS    = registerBlockItem("path_start_pos",  MillBlocks.PATH_START_POS);

    public static final Item TREE_OAK_SPAWN       = registerBlockItem("tree_oak_spawn",       MillBlocks.TREE_OAK_SPAWN);
    public static final Item TREE_DARK_OAK_SPAWN  = registerBlockItem("tree_dark_oak_spawn",  MillBlocks.TREE_DARK_OAK_SPAWN);
    public static final Item TREE_SPRUCE_SPAWN    = registerBlockItem("tree_spruce_spawn",    MillBlocks.TREE_SPRUCE_SPAWN);

    public static final Item SPAWN_COW      = registerBlockItem("spawn_cow",      MillBlocks.SPAWN_COW);
    public static final Item SPAWN_SHEEP    = registerBlockItem("spawn_sheep",    MillBlocks.SPAWN_SHEEP);
    public static final Item SPAWN_CHICKEN  = registerBlockItem("spawn_chicken",  MillBlocks.SPAWN_CHICKEN);
    public static final Item SPAWN_PIG      = registerBlockItem("spawn_pig",      MillBlocks.SPAWN_PIG);

    public static final Item SOURCE_ROCK = registerBlockItem("source_rock", MillBlocks.SOURCE_ROCK);
    public static final Item SOURCE_SAND = registerBlockItem("source_sand", MillBlocks.SOURCE_SAND);
    public static final Item SOIL_FLOWER = registerBlockItem("soil_flower", MillBlocks.SOIL_FLOWER);
    public static final Item SOIL_WHEAT  = registerBlockItem("soil_wheat",  MillBlocks.SOIL_WHEAT);
    public static final Item SOIL_CARROT = registerBlockItem("soil_carrot", MillBlocks.SOIL_CARROT);
    public static final Item SOIL_POTATO = registerBlockItem("soil_potato", MillBlocks.SOIL_POTATO);

    public static final Item IMPORT_TABLE      = registerBlockItem("import_table", MillBlocks.IMPORT_TABLE);

    // =========================================================================
    // NORMAN ITEMS
    // =========================================================================
        // Tools & Weapons
        public static final Item NORMAN_PICKAXE 
            = registerItem("norman_pickaxe", props -> new Item(props.pickaxe(MillCustomMaterials.NORMAN_TOOL_MATERIAL, 1, -2.8f)));
        public static final Item NORMAN_AXE 
            = registerItem("norman_axe", props -> new AxeItem(MillCustomMaterials.NORMAN_TOOL_MATERIAL, 4, -3, props));
        public static final Item NORMAN_SHOVEL 
            = registerItem("norman_shovel", props -> new ShovelItem(MillCustomMaterials.NORMAN_TOOL_MATERIAL, 1.5f, -3, props));
        public static final Item NORMAN_HOE 
            = registerItem("norman_hoe", props -> new HoeItem(MillCustomMaterials.NORMAN_TOOL_MATERIAL, -4, 1, props));
        public static final Item NORMAN_BROADSWORD 
            = registerItem("norman_broadsword", props -> new Item(props.sword(MillCustomMaterials.NORMAN_TOOL_MATERIAL, 3, -2.4f)));

        // Armor
        public static final Item NORMAN_HELMET 
            = registerItem("norman_helmet", props -> new Item(props.humanoidArmor(MillCustomMaterials.NORMAN_ARMOR_MATERIAL, ArmorType.HELMET)));
        public static final Item NORMAN_PLATE 
            = registerItem("norman_plate", props -> new Item(props.humanoidArmor(MillCustomMaterials.NORMAN_ARMOR_MATERIAL, ArmorType.CHESTPLATE)));
        public static final Item NORMAN_LEGS 
            = registerItem("norman_legs", props -> new Item(props.humanoidArmor(MillCustomMaterials.NORMAN_ARMOR_MATERIAL, ArmorType.LEGGINGS)));
        public static final Item NORMAN_BOOTS 
            = registerItem("norman_boots", props -> new Item(props.humanoidArmor(MillCustomMaterials.NORMAN_ARMOR_MATERIAL, ArmorType.BOOTS)));

        // Food
        public static final Item CIDER_APPLE 
            = registerItem("cider_apple", props -> MillFoodItemBuilder.CreateItem(props, MillFoodType.CIDER_APPLE));
        public static final Item CIDER 
            = registerItem("cider", props -> MillFoodItemBuilder.CreateItem(props, MillFoodType.CIDER));
        public static final Item BOUDIN 
            = registerItem("boudin", props -> MillFoodItemBuilder.CreateItem(props, MillFoodType.BOUDIN));
        public static final Item CALVA 
            = registerItem("calva", props -> MillFoodItemBuilder.CreateItem(props, MillFoodType.CALVA)); 
        public static final Item TRIPES 
            = registerItem("tripes", props -> MillFoodItemBuilder.CreateItem(props, MillFoodType.TRIPES));

    // =========================================================================
    // Helpers
    // =========================================================================

    private static Item registerBlockItem(String id, Block block) {
        return registerItem(id, props -> new BlockItem(block, props));
    }

    private static Item registerItem(String id, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, id));
        Item.Properties props = new Item.Properties().setId(key);
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props));
    }

    public static void initialize() {
        MillenaireNewAge.LOGGER.info("Items registered.");
    }
}
