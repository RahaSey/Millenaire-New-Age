package com.mat37dev.client.gui;

import com.mat37dev.network.CreateVillagePayload;
import com.mat37dev.network.OpenVillageCreationPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * GUI de création de village via la Baguette d'Invocation.
 *
 * <p>Layout :
 * <ul>
 *   <li>Titre en haut</li>
 *   <li>Rangée d'onglets (un par culture)</li>
 *   <li>Liste des types de village de la culture active</li>
 *   <li>Boutons "Créer" / "Annuler"</li>
 * </ul>
 */
@Environment(EnvType.CLIENT)
public class VillageCreationScreen extends Screen {

    private final List<OpenVillageCreationPayload.CultureInfo> cultures;
    private final BlockPos goldPos;

    private int selectedCultureIndex     = 0;
    private String selectedVillageTypeId = null;

    private Button createButton;
    private Button cancelButton;

    // Layout
    private static final int TAB_H    = 20;
    private static final int ITEM_H   = 30;
    private static final int MARGIN   = 10;

    private int listX, listY, listWidth, listHeight;
    private int scrollOffset = 0;

    public VillageCreationScreen(BlockPos goldPos,
                                  List<OpenVillageCreationPayload.CultureInfo> cultures) {
        super(Component.translatable("gui.millenaire-new-age.village_creation.title"));
        this.goldPos  = goldPos;
        this.cultures = cultures;
    }

    @Override
    protected void init() {
        listX      = MARGIN;
        listY      = 14 + TAB_H + 10;
        listWidth  = this.width - MARGIN * 2;
        listHeight = this.height - listY - 38;

        int btnW    = 90;
        int spacing = 10;
        int startX  = this.width / 2 - (btnW + spacing / 2);

        this.createButton = Button.builder(
            Component.translatable("gui.millenaire-new-age.village_creation.create"),
            btn -> onCreateClicked()
        ).bounds(startX, this.height - 28, btnW, 20).build();
        this.createButton.active = false;
        addRenderableWidget(this.createButton);

        this.cancelButton = Button.builder(
            Component.translatable("gui.millenaire-new-age.village_creation.cancel"),
            btn -> this.onClose()
        ).bounds(startX + btnW + spacing, this.height - 28, btnW, 20).build();
        addRenderableWidget(this.cancelButton);
    }

    // ── Rendu ─────────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        super.renderBackground(g, mx, my, pt);
        g.fill(0, 0, this.width, this.height, 0xAA000000);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.drawCenteredString(this.font, this.title, this.width / 2, 4, 0xFFFFFFFF);

        renderTabs(g, mx, my);

        // Fond de la liste
        g.fill(listX - 2, listY - 2, listX + listWidth + 2, listY + listHeight + 2, 0xFF2A2A2A);
        g.fill(listX, listY, listX + listWidth, listY + listHeight, 0xFF1A1A1A);

        g.enableScissor(listX, listY, listX + listWidth, listY + listHeight);
        renderVillageTypes(g, mx, my);
        g.disableScissor();

        this.createButton.render(g, mx, my, pt);
        this.cancelButton.render(g, mx, my, pt);
    }

    private void renderTabs(GuiGraphics g, int mx, int my) {
        if (cultures.isEmpty()) return;
        int tabY     = 14;
        int tabWidth = tabWidthFor(cultures.size());

        for (int i = 0; i < cultures.size(); i++) {
            OpenVillageCreationPayload.CultureInfo culture = cultures.get(i);
            int tabX = MARGIN + i * tabWidth;
            boolean sel     = (i == selectedCultureIndex);
            boolean hovered = mx >= tabX && mx < tabX + tabWidth
                           && my >= tabY  && my < tabY + TAB_H;

            g.fill(tabX, tabY, tabX + tabWidth, tabY + TAB_H,
                sel ? 0xFF4477CC : (hovered ? 0xFF3A3A3A : 0xFF2A2A2A));
            g.fill(tabX, tabY, tabX + tabWidth, tabY + 1, 0xFF888888);
            g.fill(tabX, tabY, tabX + 1, tabY + TAB_H, 0xFF888888);

            Component cultureName = Component.translatable("culture.millenaire-new-age." + culture.id());
            g.drawCenteredString(this.font, cultureName,
                tabX + tabWidth / 2, tabY + (TAB_H - 8) / 2,
                sel ? 0xFFFFFFFF : 0xFFAAAAAA);
        }
    }

    private void renderVillageTypes(GuiGraphics g, int mx, int my) {
        if (cultures.isEmpty()) return;
        OpenVillageCreationPayload.CultureInfo currentCulture = cultures.get(selectedCultureIndex);
        List<OpenVillageCreationPayload.VillageTypeInfo> vtList = currentCulture.villageTypes();

        for (int i = 0; i < vtList.size(); i++) {
            OpenVillageCreationPayload.VillageTypeInfo vt = vtList.get(i);
            int entryY      = listY + i * ITEM_H - scrollOffset;
            int entryBottom = entryY + ITEM_H;
            if (entryBottom <= listY || entryY >= listY + listHeight) continue;

            boolean sel = vt.id().equals(selectedVillageTypeId);
            boolean hov = mx >= listX && mx < listX + listWidth
                       && my >= entryY && my < entryBottom;

            if (sel)            g.fill(listX, entryY, listX + listWidth, entryBottom, 0xFF2255AA);
            else if (hov)       g.fill(listX, entryY, listX + listWidth, entryBottom, 0x55FFFFFF);
            else if (i % 2 == 0) g.fill(listX, entryY, listX + listWidth, entryBottom, 0x15FFFFFF);

            g.fill(listX, entryY, listX + listWidth, entryY + 1, 0x33FFFFFF);

            // Nom
            String vtSubId = vt.id().contains(":") ? vt.id().split(":")[1] : vt.id();
            Component vtName = Component.translatable("village_type.millenaire-new-age." + currentCulture.id() + "." + vtSubId);
            g.drawString(this.font, vtName,
                listX + 6, entryY + 5, sel ? 0xFFFFFFFF : 0xFFDDDDDD);

            // Infos
            Component info = Component.translatable("gui.millenaire-new-age.village_creation.buildings", vt.minBuildings(), vt.maxBuildings());
            if (vt.hasWalls()) {
                info = info.copy().append("  |  ").append(Component.translatable("gui.millenaire-new-age.village_creation.walls"));
            }
            g.drawString(this.font, info, listX + 6, entryY + 17, 0xFF888888);
        }
    }

    // ── Saisie ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mx = event.x(), my = event.y();

        // Clic sur onglet
        int tabY     = 14;
        int tabWidth = tabWidthFor(cultures.size());
        if (my >= tabY && my < tabY + TAB_H) {
            for (int i = 0; i < cultures.size(); i++) {
                int tabX = MARGIN + i * tabWidth;
                if (mx >= tabX && mx < tabX + tabWidth) {
                    selectedCultureIndex  = i;
                    selectedVillageTypeId = null;
                    createButton.active   = false;
                    scrollOffset          = 0;
                    return true;
                }
            }
        }

        // Clic dans la liste
        if (mx >= listX && mx < listX + listWidth
         && my >= listY  && my < listY + listHeight
         && !cultures.isEmpty()) {
            int idx = ((int) my - listY + scrollOffset) / ITEM_H;
            List<OpenVillageCreationPayload.VillageTypeInfo> vtList =
                cultures.get(selectedCultureIndex).villageTypes();
            if (idx >= 0 && idx < vtList.size()) {
                selectedVillageTypeId = vtList.get(idx).id();
                createButton.active   = true;
                return true;
            }
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (mx >= listX && mx < listX + listWidth
         && my >= listY  && my < listY + listHeight
         && !cultures.isEmpty()) {
            int count     = cultures.get(selectedCultureIndex).villageTypes().size();
            int maxScroll = Math.max(0, count * ITEM_H - listHeight);
            scrollOffset  = Mth.clamp(scrollOffset - (int)(dy * ITEM_H), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void onCreateClicked() {
        if (selectedVillageTypeId == null || cultures.isEmpty()) return;
        String cultureId = cultures.get(selectedCultureIndex).id();
        ClientPlayNetworking.send(new CreateVillagePayload(cultureId, selectedVillageTypeId, goldPos));
        this.onClose();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int tabWidthFor(int count) {
        if (count == 0) return 80;
        return Math.min(80, (this.width - MARGIN * 2) / count);
    }
}
