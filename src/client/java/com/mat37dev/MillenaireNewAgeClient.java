package com.mat37dev;

import com.mat37dev.client.creator.CreatorClientState;
import com.mat37dev.client.creator.StructureListScreen;
import com.mat37dev.client.creator.StructurePreviewRenderer;
import com.mat37dev.client.gui.VillageCreationScreen;
import com.mat37dev.network.OpenStructureListPayload;
import com.mat37dev.network.OpenVillageCreationPayload;
import com.mat37dev.network.StructurePreviewPayload;
import com.mat37dev.network.StructureRotationPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class MillenaireNewAgeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
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
                    new VillageCreationScreen(payload.goldPos(), payload.civilizations())
                )
            )
        );
    }
}
