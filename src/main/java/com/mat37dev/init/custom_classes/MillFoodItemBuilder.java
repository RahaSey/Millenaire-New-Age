package com.mat37dev.init.custom_classes;

import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.sounds.SoundEvents;

public class MillFoodItemBuilder {

    //#region Inner Classes
    private static class MillCustomFoodItem extends Item {
        public MillCustomFoodItem(Properties settings, FoodProperties foodComponent, List<MobEffectInstance> statusEffects, Float statusEffectChance, Integer maxDamage, Boolean isDrink) {
            super(settings.food(foodComponent, 
                new Consumable(1.6f, 
                    isDrink ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT, 
                    isDrink ? SoundEvents.GENERIC_DRINK : SoundEvents.GENERIC_EAT, 
                    true, 
                    List.of(new ApplyStatusEffectsConsumeEffect(statusEffects, statusEffectChance)))).durability(maxDamage));
        }

        public MillCustomFoodItem(Properties settings, FoodProperties foodComponent, List<MobEffectInstance> statusEffects, Float statusEffectChance, Boolean isDrink) {
            super(settings.food(foodComponent, 
                new Consumable(1.6f, 
                    isDrink ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT, 
                    isDrink ? SoundEvents.GENERIC_DRINK : SoundEvents.GENERIC_EAT, 
                    true, 
                    List.of(new ApplyStatusEffectsConsumeEffect(statusEffects, statusEffectChance)))));
        }

        public MillCustomFoodItem(Properties settings, FoodProperties foodComponent, Integer maxDamage, Boolean isDrink) {
            super(settings.food(foodComponent, 
                new Consumable(1.6f, 
                    isDrink ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT, 
                    isDrink ? SoundEvents.GENERIC_DRINK : SoundEvents.GENERIC_EAT, 
                    true,
                List.of())).durability(maxDamage));
        }

        public MillCustomFoodItem(Properties settings, FoodProperties foodComponent, Boolean isDrink) {
            super(settings.food(foodComponent, 
                new Consumable(1.6f, 
                    isDrink ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT, 
                    isDrink ? SoundEvents.GENERIC_DRINK : SoundEvents.GENERIC_EAT, 
                    true, 
                    List.of())));
        }
        
        // For OLIVE_OIL
        public MillCustomFoodItem(Properties settings, Boolean isDrink) {
            super(settings.food(new FoodProperties(0, 0, true), 
                new Consumable(1.6f, 
                    isDrink ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT, 
                    isDrink ? SoundEvents.GENERIC_DRINK : SoundEvents.GENERIC_EAT, 
                    true, 
                    List.of(new ClearAllStatusEffectsConsumeEffect()))));
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
            if (!stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() <= 1){
                super.finishUsingItem(stack, level, user);
                return stack;
            }

            // I copy the stack to get the nutrition without loosing the item if it has durability left
            super.finishUsingItem(stack.copy(), level, user);
            stack.hurtAndBreak(1, user, EquipmentSlot.MAINHAND);
            return stack;
        }
    }

    private static class MillFoodItem {
        final Integer Nutrition;
        final Integer MaxDamage;
        final Float Saturation;
        final Float StatusEffectChance;
        final Boolean IsAlwaysEdible;
        final Boolean IsDrink;
        final List<MobEffectInstance> StatusEffects;

        public MillFoodItem(Integer nutrition, Float saturation, Integer maxDamage,
                            Boolean isAlwaysEdible, Boolean isDrink, List<MobEffectInstance> statusEffects, Float statusEffectChance) {
            Nutrition = nutrition;
            Saturation = saturation;
            MaxDamage = maxDamage;
            IsAlwaysEdible = isAlwaysEdible;
            IsDrink = isDrink;
            StatusEffects = statusEffects;
            StatusEffectChance = statusEffectChance;
        }
    }
    //#endregion Inner Classes

    public static Item CreateItem(Item.Properties settings, MillFoodType foodType)
    {
        MillFoodItem foodItem = AllFood.get(foodType);

        if (foodType == MillFoodType.OLIVE_OIL){
            return new MillCustomFoodItem(settings, true);
        }

        if (foodItem.StatusEffects == null || foodItem.StatusEffects.isEmpty()){
            if (foodItem.MaxDamage > 0)
                return new MillCustomFoodItem(settings, 
                    new FoodProperties(foodItem.Nutrition, foodItem.Saturation, foodItem.IsAlwaysEdible), foodItem.MaxDamage, foodItem.IsDrink);
            else
                return new MillCustomFoodItem(settings, 
                        new FoodProperties(foodItem.Nutrition, foodItem.Saturation, foodItem.IsAlwaysEdible), foodItem.IsDrink);
        }
        else{
            if (foodItem.MaxDamage > 0)
                return new MillCustomFoodItem(settings, 
                    new FoodProperties(foodItem.Nutrition, foodItem.Saturation, foodItem.IsAlwaysEdible), foodItem.StatusEffects, foodItem.StatusEffectChance, foodItem.MaxDamage, foodItem.IsDrink);
            else
                return new MillCustomFoodItem(settings, 
                    new FoodProperties(foodItem.Nutrition, foodItem.Saturation, foodItem.IsAlwaysEdible), foodItem.StatusEffects, foodItem.StatusEffectChance, foodItem.IsDrink);
        }
    }


    //#region All Food Types
        public static enum MillFoodType {
        VEG_CURRY,
        CHICKEN_CURRY,
        RASGULLA,
        YOGURT,
        AYRAN,
        PIDE,
        LOKUM,
        HELVA,
        PISTACHIOS,
        BEAR_MEAT_RAW,
        BEAR_MEAT_COOKED,
        WOLF_MEAT_RAW,
        WOLF_MEAT_COOKED,
        SEAFOOD_RAW,
        SEAFOOD_COOKED,
        INUIT_BEAR_STEW,
        INUIT_MEATY_STEW,
        INUIT_POTATO_STEW,
        SAKE,
        UDON,
        IKAYAKI,
        WINE_BASIC,
        WINE_FANCY,
        SOUVLAKI,
        FETA,
        CIDER_APPLE, // vv Not in game vv
        CIDER,
        BOUDIN,
        CALVA,
        TRIPES,
        OLIVES,
        OLIVE_OIL,
        CHERRIES,
        CHERRY_BLOSSOM,
        MASA,
        WAH,
        BALCHE,
        SIKIL_PAH,
        CACAUHAA
    }
    //#endregion All Food Types

    //#region All Food Items
    private static final Map<MillFoodType, MillFoodItem> AllFood =
        Map.ofEntries(
            //VEG_CURRY
            Map.entry(MillFoodType.VEG_CURRY, new MillFoodItem(6, 7.2f, 6, false, false, null, null)),

            //CHICKEN_CURRY
            Map.entry(MillFoodType.CHICKEN_CURRY, new MillFoodItem(8, 12.8f, 8, false, false, null, null)),

            //RASGULLA
            Map.entry(MillFoodType.RASGULLA, new MillFoodItem(0, 0f, 0, true, false,
             List.of(
                new MobEffectInstance(MobEffects.REGENERATION, 30 * 20, 0),
                new MobEffectInstance(MobEffects.SPEED, 480 * 20, 1)), 1f)),

            //YOGURT
            Map.entry(MillFoodType.YOGURT, new MillFoodItem(0, 0f, 0, true, false,
             List.of(
                new MobEffectInstance(MobEffects.REGENERATION, 15 * 20, 0)), 1f)),

            //AYRAN
            Map.entry(MillFoodType.AYRAN, new MillFoodItem(0, 0f, 0, true, true,
             List.of(
                new MobEffectInstance(MobEffects.REGENERATION, 15 * 20, 0),
                new MobEffectInstance(MobEffects.NAUSEA, 5 * 20, 0)), 1f)),

            //PIDE
            Map.entry(MillFoodType.PIDE, new MillFoodItem(8, 16f, 8, false, false, null, null)),
            
            //LOKUM
            Map.entry(MillFoodType.LOKUM, new MillFoodItem(3, 0.6f, 0, true, false,
             List.of(
                new MobEffectInstance(MobEffects.SPEED, 120 * 20, 0)), 0.2f)),

            //HELVA
            Map.entry(MillFoodType.HELVA, new MillFoodItem(5, 0.6f, 0, true, false,
             List.of(
                new MobEffectInstance(MobEffects.RESISTANCE, 120 * 20, 0)), 0.2f)),

            //PISTACHIOS
            Map.entry(MillFoodType.PISTACHIOS, new MillFoodItem(1, 0.6f, 0, false, false, null, null)),
            
            //BEAR_MEAT_RAW
            Map.entry(MillFoodType.BEAR_MEAT_RAW, new MillFoodItem(4, 4f, 0, false, false,
             List.of(
                new MobEffectInstance(MobEffects.STRENGTH, 240 * 20, 0)), 1f)),

            //BEAR_MEAT_COOKED
            Map.entry(MillFoodType.BEAR_MEAT_COOKED, new MillFoodItem(10, 20f, 0, false, false,
             List.of(
                new MobEffectInstance(MobEffects.STRENGTH, 480 * 20, 1)), 1f)),

            //WOLF_MEAT_RAW
            Map.entry(MillFoodType.WOLF_MEAT_RAW, new MillFoodItem(3, 1.8f, 0, false, false, null, null)),

            //BEAR_MEAT_COOKED
            Map.entry(MillFoodType.WOLF_MEAT_COOKED, new MillFoodItem(5, 6f, 0, false, false,
             List.of(
                new MobEffectInstance(MobEffects.STRENGTH, 60 * 20, 1)), 1f)),

            //SEAFOOD_RAW
            Map.entry(MillFoodType.SEAFOOD_RAW, new MillFoodItem(2, 0.8f, 0, false, false, null, null)),

            //SEAFOOD_COOKED
            Map.entry(MillFoodType.SEAFOOD_COOKED, new MillFoodItem(2, 1f, 0, false, false, null, null)),

            //INUIT_BEAR_STEW
            Map.entry(MillFoodType.INUIT_BEAR_STEW, new MillFoodItem(8, 16f, 8, false, false,
             List.of(
                new MobEffectInstance(MobEffects.STRENGTH, 480 * 20, 2)), 1f)),

            //INUIT_MEATY_STEW
            Map.entry(MillFoodType.INUIT_MEATY_STEW, new MillFoodItem(8, 12.8f, 8, false, false, null, null)),

            //INUIT_POTATO_STEW
            Map.entry(MillFoodType.INUIT_POTATO_STEW, new MillFoodItem(6, 7.2f, 6, false, false, null, null)),

            //SAKE
            Map.entry(MillFoodType.SAKE, new MillFoodItem(0, 0f, 8, true, true,
             List.of(
                new MobEffectInstance(MobEffects.JUMP_BOOST, 480 * 20, 0),
                new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 0)), 1f)),

            //UDON
            Map.entry(MillFoodType.UDON, new MillFoodItem(8, 12.8f, 6, false, false, null, null)),

            //IKAYAKI
            Map.entry(MillFoodType.IKAYAKI, new MillFoodItem(10, 20f, 8, false, false,
             List.of(
                new MobEffectInstance(MobEffects.WATER_BREATHING, 480 * 20, 1)), 1f)),

            //WINE_BASIC
            Map.entry(MillFoodType.WINE_BASIC, new MillFoodItem(0, 0f, 6, true, true,
             List.of(
                new MobEffectInstance(MobEffects.NAUSEA, 8 * 20, 0),
                new MobEffectInstance(MobEffects.REGENERATION, 5 * 20, 0)), 1f)),

            //WINE_FANCY
            Map.entry(MillFoodType.WINE_FANCY, new MillFoodItem(0, 0f, 16, true, true,
             List.of(
                new MobEffectInstance(MobEffects.NAUSEA, 15 * 20, 0),
                new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 0),
                new MobEffectInstance(MobEffects.RESISTANCE, 480 * 20, 1)), 1f)),
            
            //SOUVLAKI
            Map.entry(MillFoodType.SOUVLAKI, new MillFoodItem(10, 20f, 8, false, false, null, null)),

            //FETA
            Map.entry(MillFoodType.FETA, new MillFoodItem(1, 1f, 0, true, false,
             List.of(
                new MobEffectInstance(MobEffects.REGENERATION, 50, 0)), 1f)),

            //CIDER_APPLE
            Map.entry(MillFoodType.CIDER_APPLE, new MillFoodItem(1, 0.1f, 0, false, false, null, null)),

            //CIDER
            Map.entry(MillFoodType.CIDER, new MillFoodItem(0, 0f, 6, true, true,
             List.of(
                new MobEffectInstance(MobEffects.NAUSEA, 5 * 20, 0),
                new MobEffectInstance(MobEffects.REGENERATION, 5 * 20, 0)), 1f)),

            //BOUDIN
            Map.entry(MillFoodType.BOUDIN, new MillFoodItem(8, 16f, 6, false, false, null, null)),

            //CALVA
            Map.entry(MillFoodType.CALVA, new MillFoodItem(0, 0f, 16, true, true,
             List.of(
                new MobEffectInstance(MobEffects.NAUSEA, 10 * 20, 0),
                new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 0)), 1f)),

            //TRIPES
            Map.entry(MillFoodType.TRIPES, new MillFoodItem(10, 20f, 8, false, false, null, null)),

            //OLIVES
            Map.entry(MillFoodType.OLIVES, new MillFoodItem(1, 0.1f, 0, false, false, null, null)),

            //OLIVE_OIL
            Map.entry(MillFoodType.OLIVE_OIL, new MillFoodItem(0, 0f, 0, true, true, null, 1f)),

            //CHERRIES
            Map.entry(MillFoodType.CHERRIES, new MillFoodItem(1, 0.1f, 0, false, false, null, null)),

            //CHERRY_BLOSSOM
            Map.entry(MillFoodType.CHERRY_BLOSSOM, new MillFoodItem(1, 0.1f, 0, false, false, null, null)),

            //MASA
            Map.entry(MillFoodType.MASA, new MillFoodItem(6, 7.2f, 4, false, false, null, null)),

            //WAH
            Map.entry(MillFoodType.WAH, new MillFoodItem(10, 20f, 6, false, false, null, null)),

            //BALCHE
            Map.entry(MillFoodType.BALCHE, new MillFoodItem(0, 0f, 8, true, true,
             List.of(
                new MobEffectInstance(MobEffects.JUMP_BOOST, 480 * 20, 0),
                new MobEffectInstance(MobEffects.REGENERATION, 8 * 20, 0)), 1f)),

            //SIKIL_PAH
            Map.entry(MillFoodType.SIKIL_PAH, new MillFoodItem(7, 9.8f, 7, false, false, null, null)),

            //CACAUHAA
            Map.entry(MillFoodType.CACAUHAA, new MillFoodItem(0, 0f, 6, true, true,
             List.of(
                new MobEffectInstance(MobEffects.NIGHT_VISION, 480 * 20, 0),
                new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 0)), 1f))
        );
    //#endregion All Food Items
}