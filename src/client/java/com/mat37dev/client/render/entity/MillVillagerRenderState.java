package com.mat37dev.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MillVillagerRenderState extends HumanoidRenderState {

    /** Chemin ResourceLocation relatif de la texture de corps (sans le namespace). */
    public String bodyTexturePath = "";

    /** Chemin relatif de la texture de vêtements (peut être vide = pas de layer). */
    public String clothingTexturePath = "";

    /** Chemin relatif de la texture des cheveux (peut être vide = pas de layer). */
    public String hairTexturePath = "";

    /** Ligne de statut affichée sous le nom (null si pas de statut). */
    @Nullable
    public Component statusLine = null;
}
