package com.mat37dev.client.render.entity;

import com.mat37dev.MillenaireNewAge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class MillVillagerModel extends HumanoidModel<MillVillagerRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "villager"), "main");

    public MillVillagerModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        // Textures Millenaire sont au format 64×32 (layout pre-1.8 avec bras/jambes gauches mirrored)
        // HumanoidModel.createMesh utilise exactement ce layout — seule la hauteur change
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32);
    }
}
