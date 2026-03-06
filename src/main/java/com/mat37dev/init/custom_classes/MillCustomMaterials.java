package com.mat37dev.init.custom_classes;

import java.util.Map;

import com.mat37dev.MillenaireNewAge;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MillCustomMaterials {

    // Armor Asset Ids
    private static final ResourceKey<EquipmentAsset> NORMAN_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "norman"));
    private static final ResourceKey<EquipmentAsset> BYZANTINE_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "byzantine"));
    private static final ResourceKey<EquipmentAsset> JAPANESE_RED_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "japanese_red"));
    private static final ResourceKey<EquipmentAsset> JAPANESE_BLUE_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "japanese_blue"));
    private static final ResourceKey<EquipmentAsset> JAPANESE_GUARD_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "japanese_guard"));
    private static final ResourceKey<EquipmentAsset> SELJUK_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "seljuk"));
    private static final ResourceKey<EquipmentAsset> SELJUK_WOOL_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "seljuk_wool"));
    private static final ResourceKey<EquipmentAsset> FUR_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "furcoat"));
    private static final ResourceKey<EquipmentAsset> MAYAN_CROWN_ARMOR_MATERIAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "mayan_quest_crown"));

    // Armor Materials
    public static final ArmorMaterial NORMAN_ARMOR_MATERIAL = new ArmorMaterial(66, 
        Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3),
        10, SoundEvents.ARMOR_EQUIP_IRON, 2, 0, ItemTags.DIAMOND_TOOL_MATERIALS, NORMAN_ARMOR_MATERIAL_KEY);

    public static final ArmorMaterial BYZANTINE_ARMOR_MATERIAL = new ArmorMaterial(33, 
        Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3),
        20, SoundEvents.ARMOR_EQUIP_IRON, 1, 0, ItemTags.DIAMOND_TOOL_MATERIALS, BYZANTINE_ARMOR_MATERIAL_KEY);

    public static final ArmorMaterial JAPANESE_RED_ARMOR_MATERIAL = new ArmorMaterial(33, 
        Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 6, ArmorType.LEGGINGS, 5, ArmorType.BOOTS, 2),
        25, SoundEvents.ARMOR_EQUIP_IRON, 0, 0, ItemTags.DIAMOND_TOOL_MATERIALS, JAPANESE_RED_ARMOR_MATERIAL_KEY);

    public static final ArmorMaterial JAPANESE_BLUE_ARMOR_MATERIAL = new ArmorMaterial(33, 
        Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 6, ArmorType.LEGGINGS, 5, ArmorType.BOOTS, 2),
        25, SoundEvents.ARMOR_EQUIP_IRON, 0, 0, ItemTags.DIAMOND_TOOL_MATERIALS, JAPANESE_BLUE_ARMOR_MATERIAL_KEY);

    public static final ArmorMaterial JAPANESE_GUARD_ARMOR_MATERIAL = new ArmorMaterial(25, 
        Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 5, ArmorType.LEGGINGS, 4, ArmorType.BOOTS, 1),
        25, SoundEvents.ARMOR_EQUIP_IRON, 0, 0, ItemTags.DIAMOND_TOOL_MATERIALS, JAPANESE_GUARD_ARMOR_MATERIAL_KEY);

    public static final ArmorMaterial SELJUK_ARMOR_MATERIAL = new ArmorMaterial(66, 
        Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3),
        10, SoundEvents.ARMOR_EQUIP_IRON, 2, 0, ItemTags.DIAMOND_TOOL_MATERIALS, SELJUK_ARMOR_MATERIAL_KEY);

    public static final ArmorMaterial SELJUK_WOOL_ARMOR_MATERIAL = new ArmorMaterial(7, 
        Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 5, ArmorType.LEGGINGS, 3, ArmorType.BOOTS, 1),
        10, SoundEvents.ARMOR_EQUIP_LEATHER, 1, 0, ItemTags.WOOL, SELJUK_WOOL_ARMOR_MATERIAL_KEY);

    public static final ArmorMaterial FUR_ARMOR_MATERIAL = new ArmorMaterial(7, 
        Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 5, ArmorType.LEGGINGS, 3, ArmorType.BOOTS, 1),
        25, SoundEvents.ARMOR_EQUIP_LEATHER, 2, 0, ItemTags.DIAMOND_TOOL_MATERIALS, FUR_ARMOR_MATERIAL_KEY);

    public static final ArmorMaterial MAYAN_CROWN_ARMOR_MATERIAL = new ArmorMaterial(33, 
        Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 6, ArmorType.LEGGINGS, 8, ArmorType.BOOTS, 3),
        10, SoundEvents.ARMOR_EQUIP_GOLD, 3, 0, ItemTags.GOLD_TOOL_MATERIALS, MAYAN_CROWN_ARMOR_MATERIAL_KEY);

    // Tool Materials
    public static final ToolMaterial NORMAN_TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 10.0F, 4.0F, 10, ItemTags.DIAMOND_TOOL_MATERIALS);
    public static final ToolMaterial BYZANTINE_TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 12.0F, 3.0F, 15, ItemTags.DIAMOND_TOOL_MATERIALS);
    public static final ToolMaterial OBSIDIAN_TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 6.0F, 2.0F, 25, ItemTags.DIAMOND_TOOL_MATERIALS);
    public static final ToolMaterial BETTER_STEEL_TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 5.0F, 3.0F, 10, ItemTags.DIAMOND_TOOL_MATERIALS);
}
