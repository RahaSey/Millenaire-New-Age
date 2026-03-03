package com.mat37dev.init;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.block.MillChestBlockEntity;
import com.mat37dev.block.MillChestScreenHandler;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class MillBlockEntities {

    public static final BlockEntityType<MillChestBlockEntity> MILL_CHEST_ENTITY =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "mill_chest"),
                    FabricBlockEntityTypeBuilder.create(MillChestBlockEntity::new, MillBlocks.MILL_CHEST).build()
            );

    public static final MenuType<MillChestScreenHandler> MILL_CHEST_MENU =
            Registry.register(
                    BuiltInRegistries.MENU,
                    ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "mill_chest"),
                    new ExtendedScreenHandlerType<>(MillChestScreenHandler::new, MillChestScreenHandler.CODEC)
            );

    public static void initialize() {
        MillenaireNewAge.LOGGER.info("Block entities registered.");
    }
}
