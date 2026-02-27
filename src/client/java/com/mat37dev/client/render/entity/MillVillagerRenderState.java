package com.mat37dev.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

@Environment(EnvType.CLIENT)
public class MillVillagerRenderState extends HumanoidRenderState {

    /** Chemin ResourceLocation relatif de la texture de corps (sans le namespace). */
    public String bodyTexturePath = "";

    /** Chemin relatif de la texture de vêtements (peut être vide = pas de layer). */
    public String clothingTexturePath = "";
}
