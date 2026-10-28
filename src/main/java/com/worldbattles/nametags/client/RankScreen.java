package com.worldbattles.nametags.client;

import com.worldbattles.nametags.RankMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RankScreen extends AbstractContainerScreen<RankMenu> {
    public RankScreen(RankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = 9999;
        this.titleLabelY = 9999;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xF20A1422);
        g.fill(x, y, x + imageWidth, y + 3, 0xFFFF922E);
        g.fill(x + 6, y + 6, x + imageWidth - 6, y + 29, 0xFF17263A);
        g.fill(x + 6, y + 31, x + imageWidth - 6, y + 32, 0xFF34465B);
        super.renderBg(g, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "WORLD BATTLES", 8, -10, 0xFFE6EDF7, false);
        g.drawString(font, "GRADES", 108, -10, 0xFFFFB34D, false);
        g.drawString(font, "SOLDE : " + String.format(java.util.Locale.CANADA_FRENCH, "%,d", menu.getBalance()) + " $", 8, 8, 0xFFFFB34D, false);
        g.drawString(font, "Clique sur un grade pour l'acheter ou le choisir", 8, 20, 0xFF9CAFC4, false);
        g.drawString(font, "INVENTAIRE", 8, 128, 0xFF9CAFC4, false);
    }
}
