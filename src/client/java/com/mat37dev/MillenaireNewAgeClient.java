package com.mat37dev;

import com.mat37dev.client.creator.CreatorClientState;
import com.mat37dev.client.creator.StructureListScreen;
import com.mat37dev.client.creator.StructurePreviewRenderer;
import com.mat37dev.client.gui.MillChestScreen;
import com.mat37dev.client.gui.VillageCreationScreen;
import com.mat37dev.client.render.entity.MillVillagerEntityRenderer;
import com.mat37dev.client.render.entity.MillVillagerModel;
import com.mat37dev.init.MillBlockEntities;
import com.mat37dev.init.MillEntities;
import com.mat37dev.network.OpenStructureListPayload;
import com.mat37dev.network.OpenVillageCreationPayload;
import com.mat37dev.network.StructurePreviewPayload;
import com.mat37dev.network.StructureRotationPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class MillenaireNewAgeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Entités — layer model + renderer
        EntityModelLayerRegistry.registerModelLayer(MillVillagerModel.LAYER_LOCATION,
                MillVillagerModel::createBodyLayer);
        EntityRendererRegistry.register(MillEntities.VILLAGER, MillVillagerEntityRenderer::new);

        // Block Entity Renderer — MillChestBlock utilise ChestRenderer (coffre vanilla animé)
        BlockEntityRendererRegistry.register(MillBlockEntities.MILL_CHEST_ENTITY, ChestRenderer::new);

        // Screen handlers
        MenuScreens.register(MillBlockEntities.MILL_CHEST_MENU, MillChestScreen::new);

        // Rendu ghost preview
        StructurePreviewRenderer.init();

        // S→C : données de preview structure
        ClientPlayNetworking.registerGlobalReceiver(StructurePreviewPayload.ID, (payload, ctx) ->
            ctx.client().execute(() ->
                CreatorClientState.setPreview(
                    payload.structureId(),
                    payload.relativeBlocks(),
                    payload.structureSize()
                )
            )
        );

        // Besoin de ça pour render les textures transparente
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.PATH_DIRT, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.PATH_DIRT_SLAB, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.PATH_GRAVEL, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.PATH_GRAVEL_SLAB, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.PATH_SLABS, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.PATH_SLABS_SLAB, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.STAINED_GLASS_WHITE, ChunkSectionLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.STAINED_GLASS_YELLOW, ChunkSectionLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.STAINED_GLASS_YELLOW_RED, ChunkSectionLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.STAINED_GLASS_RED_BLUE, ChunkSectionLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(com.mat37dev.init.MillBlocks.STAINED_GLASS_GREEN_BLUE, ChunkSectionLayer.TRANSLUCENT);
        
        // S→C : sync rotation
        ClientPlayNetworking.registerGlobalReceiver(StructureRotationPayload.ID, (payload, ctx) ->
            ctx.client().execute(() -> CreatorClientState.setRotation(payload.rotation()))
        );

        // S→C : ouvrir la GUI de liste des structures
        ClientPlayNetworking.registerGlobalReceiver(OpenStructureListPayload.ID, (payload, ctx) ->
            ctx.client().execute(() ->
                Minecraft.getInstance().setScreen(new StructureListScreen(payload.structureIds()))
            )
        );

        // S→C : ouvrir le GUI de création de village
        ClientPlayNetworking.registerGlobalReceiver(OpenVillageCreationPayload.ID, (payload, ctx) ->
            ctx.client().execute(() ->
                Minecraft.getInstance().setScreen(
                    new VillageCreationScreen(payload.goldPos(), payload.cultures())
                )
            )
        );
    }
}
