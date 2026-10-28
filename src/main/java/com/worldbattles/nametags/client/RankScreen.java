package com.worldbattles.nametags.client;

import com.worldbattles.nametags.RankMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Fully custom rank UI. The container is only used as a reliable server-side
 * click channel; vanilla slot/chest rendering is deliberately never invoked.
 */
public class RankScreen extends AbstractContainerScreen<RankMenu> {
    private static final int PANEL_W = 300;
    private static final int PANEL_H = 300;

    public RankScreen(RankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
        titleLabelY = 9999;
        inventoryLabelY = 9999;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int x = (width - PANEL_W) / 2;
        int y = (height - PANEL_H) / 2;

        // Custom panel, not the vanilla container/chest background.
        g.fill(x, y, x + PANEL_W, y + PANEL_H, 0xF20A1422);
        g.fill(x, y, x + PANEL_W, y + 4, 0xFFFF922E);
        g.fill(x + 12, y + 12, x + PANEL_W - 12, y + 48, 0xFF17263A);
        g.fill(x + 12, y + 51, x + PANEL_W - 12, y + 52, 0xFF34465B);

        g.drawString(font, "WORLD BATTLES", x + 20, y + 20, 0xFFE6EDF7, false);
        g.drawString(font, "GRADES", x + 166, y + 20, 0xFFFFB34D, false);
        g.drawString(font, "SOLDE : " + String.format(java.util.Locale.CANADA_FRENCH, "%,d", menu.getBalance()) + " $",
            x + 20, y + 36, 0xFFFFB34D, false);

        // Rank cards are custom panels. Only item icons are drawn from the menu;
        // no vanilla slot frames or inventory grid are rendered.
        for (int i = 0; i < 8; i++) {
            int slotIndex = i;
            int cardX = i == 7 ? x + 174 : x + 18;
            int cardY = i == 7 ? y + 66 : y + 62 + i * 25;
            int cardW = i == 7 ? 108 : 150;
            boolean hover = mouseX >= cardX && mouseX < cardX + cardW && mouseY >= cardY && mouseY < cardY + 22;
            g.fill(cardX, cardY, cardX + cardW, cardY + 22, hover ? 0xFF34465B : 0xFF1B2A3D);
            g.fill(cardX, cardY, cardX + 3, cardY + 22, i == 7 ? 0xFFFF922E : 0xFF526982);
            ItemStack stack = menu.getSlot(slotIndex).getItem();
            if (!stack.isEmpty()) {
                drawLock(g, cardX + 8, cardY + 3, i == 0);
                g.drawString(font, stack.getHoverName().getString(), cardX + 30, cardY + 7, 0xFF55FF55, false);
            }
        }

        g.fill(x + 12, y + 270, x + PANEL_W - 12, y + 271, 0xFF34465B);
        g.drawString(font, "Clique sur un grade pour le choisir", x + 20, y + 280, 0xFF9CAFC4, false);
        int bx = x + PANEL_W - 74;
        int by = y + PANEL_H - 24;
        boolean closeHover = mouseX >= bx && mouseX < bx + 56 && mouseY >= by && mouseY < by + 16;
        g.fill(bx, by, bx + 56, by + 16, closeHover ? 0xFF9A3D32 : 0xFF542B2B);
        g.drawString(font, "FERMER", bx + 6, by + 4, 0xFFFFFFFF, false);

        // Preserve vanilla packet-based slot clicks without rendering its chest UI.
        // Inventory slots are not drawn or exposed in this screen.
        renderTooltip(g, mouseX, mouseY);
    }

    private void drawLock(GuiGraphics g, int x, int y, boolean open) {
        int shackle = open ? x + 7 : x + 4;
        g.fill(shackle, y, shackle + 8, y + 3, 0xFFFFD45A);
        g.fill(shackle, y + 1, shackle + 3, y + 7, 0xFFFFD45A);
        g.fill(shackle + 5, y + 1, shackle + 8, y + 7, 0xFFFFD45A);
        g.fill(x + 1, y + 6, x + 15, y + 15, 0xFFFFB52E);
        g.fill(x + 3, y + 8, x + 13, y + 13, 0xFFFFD84D);
        g.fill(x + 7, y + 9, x + 9, y + 12, 0xFF5A3418);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Intentionally empty: render() draws the complete custom interface.
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // All text is positioned explicitly in render().
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int x = (width - PANEL_W) / 2;
            int y = (height - PANEL_H) / 2;
            for (int i = 0; i < 8; i++) {
                int cardX = i == 7 ? x + 174 : x + 18;
                int cardY = i == 7 ? y + 66 : y + 62 + i * 25;
                int cardW = i == 7 ? 108 : 150;
                if (mouseX >= cardX && mouseX < cardX + cardW && mouseY >= cardY && mouseY < cardY + 22) {
                    if (minecraft != null && minecraft.gameMode != null && minecraft.player != null) {
                        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, i, 0,
                            net.minecraft.world.inventory.ClickType.PICKUP, minecraft.player);
                    }
                    return true;
                }
            }
            int x = (width - PANEL_W) / 2;
            int y = (height - PANEL_H) / 2;
            int bx = x + PANEL_W - 74;
            int by = y + PANEL_H - 24;
            if (mouseX >= bx && mouseX < bx + 56 && mouseY >= by && mouseY < by + 16) onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}