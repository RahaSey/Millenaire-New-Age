package com.mat37dev.client.render.entity;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.status.VillagerStatus;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class MillVillagerEntityRenderer extends HumanoidMobRenderer<MillVillagerEntity, MillVillagerRenderState, MillVillagerModel> {

    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            MillenaireNewAge.MOD_ID, "textures/entity/villager/normans/male/nor_peasant_0.png");

    public MillVillagerEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MillVillagerModel(ctx.bakeLayer(MillVillagerModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new MillVillagerClothingLayer(this, ctx.getModelSet()));
        this.addLayer(new MillVillagerHairLayer(this, ctx.getModelSet()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(MillVillagerRenderState renderState) {
        if (!renderState.bodyTexturePath.isEmpty()) {
            return ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, renderState.bodyTexturePath);
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    public @NotNull MillVillagerRenderState createRenderState() {
        return new MillVillagerRenderState();
    }

    @Override
    public void extractRenderState(MillVillagerEntity entity, MillVillagerRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);

        boolean male = entity.isMale();
        int variant = entity.getBodyVariant();

        if (male) {
            renderState.bodyTexturePath = "textures/entity/villager/normans/male/nor_peasant_" + variant + ".png";
        } else {
            renderState.bodyTexturePath = "textures/entity/villager/normans/female/nor_wife_" + (variant % 4) + ".png";
        }

        renderState.clothingTexturePath = entity.getClothingTexture();
        renderState.hairTexturePath = entity.getHairTexture();

        // Statut pour la 2ème ligne du nametag
        VillagerStatus status = entity.getStatus();
        renderState.statusLine = Component.translatable(status.getTranslationKey())
                .withStyle(status.getColor());
    }

    @Override
    protected void submitNameTag(
            MillVillagerRenderState renderState, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState cameraState) {
        // Ligne 1 : nom + rôle (au-dessus, Y offset = -10 pour laisser de la place)
        if (renderState.nameTag != null) {
            collector.submitNameTag(
                    poseStack,
                    renderState.nameTagAttachment,
                    -10,
                    renderState.nameTag,
                    !renderState.isDiscrete,
                    renderState.lightCoords,
                    renderState.distanceToCameraSq,
                    cameraState
            );
        }
        // Ligne 2 : statut (en dessous du nom, Y offset = 0)
        if (renderState.statusLine != null && renderState.nameTag != null) {
            collector.submitNameTag(
                    poseStack,
                    renderState.nameTagAttachment,
                    0,
                    renderState.statusLine,
                    !renderState.isDiscrete,
                    renderState.lightCoords,
                    renderState.distanceToCameraSq,
                    cameraState
            );
        }
    }
}
