package com.mat37dev.client.gui;

import com.mat37dev.block.MillChestScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Écran GUI du coffre millénaire.
 *
 * <p>Utilise la texture du coffre générique vanilla (3 rangées).
 * Les dimensions et positions de slots sont identiques à {@code ContainerScreen} vanilla.
 * Si le coffre est verrouillé, une teinte rouge indique visuellement l'inaccessibilité.</p>
 */
public class MillChestScreen extends AbstractContainerScreen<MillChestScreenHandler> {

    private static final ResourceLocation CHEST_GUI_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    /** Hauteur de la zone coffre dans la texture : titre (17px) + 3 rangées × 18px = 71px. */
    private static final int CHEST_AREA_HEIGHT = 3 * 18 + 17; // = 71

    /** Hauteur de la zone inventaire joueur dans la texture (depuis UV 126). */
    private static final int INVENTORY_AREA_HEIGHT = 96;

    public MillChestScreen(MillChestScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth  = 176;
        this.imageHeight = 114 + 3 * 18; // = 168 (identique au ContainerScreen vanilla)
    }

    @Override
    protected void init() {
        super.init();
        // Label "Inventaire" juste sous la zone coffre + 3px de marge
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Zone titre + slots coffre (71px du haut de la texture)
        graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST_GUI_TEXTURE,
                leftPos, topPos,
                0f, 0f, imageWidth, CHEST_AREA_HEIGHT, 256, 256);

        // Zone inventaire joueur (UV y=126, hauteur 96px)
        graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST_GUI_TEXTURE,
                leftPos, topPos + CHEST_AREA_HEIGHT,
                0f, 126f, imageWidth, INVENTORY_AREA_HEIGHT, 256, 256);

        // Teinte rouge légère si verrouillé
        if (menu.isLocked()) {
            graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0x30FF0000);
        }
    }
}
