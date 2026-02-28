package com.mat37dev.client.render.entity;

import com.mat37dev.MillenaireNewAge;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class MillVillagerHairLayer extends RenderLayer<MillVillagerRenderState, MillVillagerModel> {

    private final MillVillagerModel overlayModel;

    public MillVillagerHairLayer(RenderLayerParent<MillVillagerRenderState, MillVillagerModel> parent,
                                     EntityModelSet modelSet) {
        super(parent);
        this.overlayModel = new MillVillagerModel(modelSet.bakeLayer(MillVillagerModel.LAYER_LOCATION));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
                       MillVillagerRenderState renderState, float limbSwing, float limbSwingAmount) {
        String hairTex = renderState.hairTexturePath;
        if (hairTex == null || hairTex.isEmpty()) return;

        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                MillenaireNewAge.MOD_ID,
                "textures/entity/villager/normans/" + hairTex);

        coloredCutoutModelCopyLayerRender(overlayModel, loc, poseStack, submitNodeCollector,
                packedLight, renderState, -1, 2);
    }
}
