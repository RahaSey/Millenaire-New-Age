package com.mat37dev.client.render.entity;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.entity.MillVillagerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class MillVillagerEntityRenderer extends HumanoidMobRenderer<MillVillagerEntity, MillVillagerRenderState, MillVillagerModel> {

    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            MillenaireNewAge.MOD_ID, "textures/entity/villager/normans/male/nor_peasant_0.png");

    public MillVillagerEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MillVillagerModel(ctx.bakeLayer(MillVillagerModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new MillVillagerClothingLayer(this, ctx.getModelSet()));
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
    }
}
