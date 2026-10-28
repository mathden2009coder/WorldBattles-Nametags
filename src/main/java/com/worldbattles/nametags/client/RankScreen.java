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
        g.drawString(font, "GRADES", x + 212, y + 20, 0xFFFFB34D, false);
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
                g.renderItem(stack, cardX + 8, cardY + 3);
                g.drawString(font, stack.getHoverName(), cardX + 30, cardY + 7, 0xFFE6EDF7, false);
            }
        }

        g.fill(x + 12, y + 270, x + PANEL_W - 12, y + 271, 0xFF34465B);
        g.drawString(font, "Clique sur un grade pour le choisir", x + 20, y + 280, 0xFF9CAFC4, false);

        // Preserve vanilla packet-based slot clicks without rendering its chest UI.
        // Inventory slots are not drawn or exposed in this screen.
        renderTooltip(g, mouseX, mouseY);
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
                    // Send a normal menu-slot click to the server. The menu stays open.
                    return super.mouseClicked(mouseX, mouseY, button);
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}