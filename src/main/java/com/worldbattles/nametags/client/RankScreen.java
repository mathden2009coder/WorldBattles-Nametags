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
        this.imageHeight = 292;
        this.inventoryLabelY = 193;
        this.titleLabelY = 9999;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xF20A1422);
        g.fill(x, y, x + imageWidth, y + 3, 0xFFFF922E);
        g.fill(x + 6, y + 6, x + imageWidth - 6, y + 31, 0xFF17263A);
        g.fill(x + 6, y + 33, x + imageWidth - 6, y + 34, 0xFF34465B);
        g.fill(x + 7, y + 187, x + imageWidth - 7, y + 188, 0xFF34465B);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "WORLD BATTLES", 8, 8, 0xFFE6EDF7, false);
        g.drawString(font, "GRADES", 124, 8, 0xFFFFB34D, false);
        g.drawString(font, "SOLDE : " + String.format(java.util.Locale.CANADA_FRENCH, "%,d", menu.getBalance()) + " $", 8, 21, 0xFFFFB34D, false);
        g.drawString(font, "INVENTAIRE", 8, 193, 0xFF9CAFC4, false);
    }
}