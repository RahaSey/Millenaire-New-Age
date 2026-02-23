package com.mat37dev.client.creator;

import com.mat37dev.creator.StructureSaveManager;
import com.mat37dev.network.SelectStructurePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * GUI de liste des structures créateur.
 *
 * <p>Rendu entièrement custom (sans ObjectSelectionList) pour éviter les
 * problèmes de texte invisible liés aux changements d'API MC 1.21.10.</p>
 */
@Environment(EnvType.CLIENT)
public class StructureListScreen extends Screen {

    private final List<String> structureIds;
    private String selectedId = null;
    private Button placeButton;
    private Button deleteButton;
    private Button closeButton;

    // Layout
    private int listX, listY, listWidth, listHeight;
    private static final int ITEM_H = 26;
    private static final int SCROLLBAR_W = 6;

    // Scroll
    private int scrollOffset = 0;

    public StructureListScreen(List<String> structureIds) {
        super(Component.translatable("gui.millenaire-new-age.structure_list.title"));
        this.structureIds = structureIds;
    }

    @Override
    protected void init() {
        listX      = 12;
        listY      = 44;
        listWidth  = this.width - 24 - SCROLLBAR_W;
        listHeight = this.height - 44 - 38;
        scrollOffset = 0;

        int btnW = 80;
        int spacing = 10;
        int startX = this.width / 2 - (btnW * 3 + spacing * 2) / 2;

        this.placeButton = Button.builder(
            Component.translatable("gui.millenaire-new-age.structure_list.place"),
            btn -> onPlaceClicked()
        ).bounds(startX, this.height - 28, btnW, 20).build();
        this.placeButton.active = false;
        this.addRenderableWidget(this.placeButton);

        this.deleteButton = Button.builder(
            Component.literal("§cSupprimer"),
            btn -> onDeleteClicked()
        ).bounds(startX + btnW + spacing, this.height - 28, btnW, 20).build();
        this.deleteButton.active = false;
        this.addRenderableWidget(this.deleteButton);

        this.closeButton = Button.builder(
            Component.translatable("gui.millenaire-new-age.structure_list.close"),
            btn -> this.onClose()
        ).bounds(startX + (btnW + spacing) * 2, this.height - 28, btnW, 20).build();
        this.addRenderableWidget(this.closeButton);
    }

    // ── Rendu ────────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        super.renderBackground(g, mx, my, pt);
        g.fill(0, 0, this.width, this.height, 0xAA000000);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // Le framework appelle renderBackground() automatiquement avant render() — ne pas le rappeler ici

        // Titre
        g.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);
        g.drawString(this.font,
            Component.literal(structureIds.size() + " structure(s)"),
            listX, listY - 12, 0xFF888888);

        // Fond de la liste
        g.fill(listX - 2, listY - 2, listX + listWidth + SCROLLBAR_W + 4, listY + listHeight + 2, 0xFF2A2A2A);
        g.fill(listX, listY, listX + listWidth, listY + listHeight, 0xFF1A1A1A);

        // Entrées avec scissor
        g.enableScissor(listX, listY, listX + listWidth, listY + listHeight);
        renderEntries(g, mx, my);
        g.disableScissor();

        // Scrollbar
        if (maxScroll() > 0) renderScrollbar(g);

        // Boutons (appel manuel, pas de super.render() pour éviter double fond)
        this.placeButton.render(g, mx, my, pt);
        this.deleteButton.render(g, mx, my, pt);
        this.closeButton.render(g, mx, my, pt);
    }

    private void renderEntries(GuiGraphics g, int mx, int my) {
        for (int i = 0; i < structureIds.size(); i++) {
            String id       = structureIds.get(i);
            int entryY      = listY + i * ITEM_H - scrollOffset;
            int entryBottom = entryY + ITEM_H;

            if (entryBottom <= listY || entryY >= listY + listHeight) continue;

            boolean sel = id.equals(selectedId);
            boolean hov = mx >= listX && mx < listX + listWidth
                       && my >= entryY && my < entryBottom;

            // Fond d'entrée
            if (sel)       g.fill(listX, entryY, listX + listWidth, entryBottom, 0xFF2255AA);
            else if (hov)  g.fill(listX, entryY, listX + listWidth, entryBottom, 0x55FFFFFF);
            else if (i % 2 == 0) g.fill(listX, entryY, listX + listWidth, entryBottom, 0x15FFFFFF);

            // Séparateur
            g.fill(listX, entryY, listX + listWidth, entryY + 1, 0x33FFFFFF);

            // Icône Placeholder (en attendant les screens auto)
            g.renderFakeItem(net.minecraft.world.item.Items.BRICKS.getDefaultInstance(), listX + 4, entryY + 5);

            // Texte — nom principal
            String displayName = StructureSaveManager.autoName(id, false);
            g.drawString(this.font, displayName,
                listX + 26, entryY + 4,
                sel ? 0xFFFFFFFF : 0xFFDDDDDD);

            // Texte — ID en gris
            g.drawString(this.font, id,
                listX + 26, entryY + 15, 0xFF888888);
        }
    }

    private void renderScrollbar(GuiGraphics g) {
        int sbX   = listX + listWidth + 2;
        int total = structureIds.size() * ITEM_H;
        g.fill(sbX, listY, sbX + SCROLLBAR_W, listY + listHeight, 0xFF333333);
        int thumbH = Math.max(20, listHeight * listHeight / total);
        int thumbY = listY + (int)((long) scrollOffset * (listHeight - thumbH) / maxScroll());
        g.fill(sbX, thumbY, sbX + SCROLLBAR_W, thumbY + thumbH, 0xFFAAAAAA);
    }

    // ── Saisie ───────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mx = event.x(), my = event.y();
        if (mx >= listX && mx < listX + listWidth
                && my >= listY && my < listY + listHeight) {
            int idx = ((int) my - listY + scrollOffset) / ITEM_H;
            if (idx >= 0 && idx < structureIds.size()) {
                selectedId = structureIds.get(idx);
                placeButton.active = true;
                deleteButton.active = true;
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (mx >= listX && mx < listX + listWidth + SCROLLBAR_W
                && my >= listY && my < listY + listHeight) {
            scrollOffset = Mth.clamp(
                scrollOffset - (int)(dy * ITEM_H),
                0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    private int maxScroll() {
        return Math.max(0, structureIds.size() * ITEM_H - listHeight);
    }

    private void onPlaceClicked() {
        if (selectedId == null) return;
        ClientPlayNetworking.send(new SelectStructurePayload(selectedId));
        this.onClose();
    }

    private void onDeleteClicked() {
        if (selectedId == null) return;
        ClientPlayNetworking.send(new com.mat37dev.network.DeleteStructurePayload(selectedId));
        // La GUI se fermera ou se rafraîchira via le payload envoyé par le serveur en retour
    }
}
