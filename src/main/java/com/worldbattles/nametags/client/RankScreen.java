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
    private static final int PANEL_W = 260;
    private static final int PANEL_H = 320;

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

        g.drawString(font, "WORLD BATTLES", x + 12, y + 17, 0xFFE6EDF7, false);
        g.drawString(font, "GRADES", x + 142, y + 17, 0xFFFF922E, false);
        g.drawString(font, "SOLDE DU SHOP", x + 12, y + 34, 0xFF9CAFC4, false);
        String balanceText = menu.getBalance() < 0 ? "INDISPONIBLE"
            : String.format(java.util.Locale.FRANCE, "%,d", menu.getBalance()).replace('\u202f', ' ') + " $";
        g.drawString(font, balanceText, x + 100, y + 34,
            menu.getBalance() < 0 ? 0xFFFF6B6B : 0xFFFFB34D, false);

        // Rank cards are custom panels. Only item icons are drawn from the menu;
        // no vanilla slot frames or inventory grid are rendered.
        for (int i = 0; i < 8; i++) {
            int slotIndex = i;
            int cardX = i == 7 ? x + 150 : x + 14;
            int cardY = i == 7 ? y + 68 : y + 58 + i * 32;
            int cardW = i == 7 ? 96 : 136;
            int cardH = 28;
            boolean hover = mouseX >= cardX && mouseX < cardX + cardW && mouseY >= cardY && mouseY < cardY + cardH;
            g.fill(cardX, cardY, cardX + cardW, cardY + cardH, hover ? 0xFF34465B : 0xFF1B2A3D);
            g.fill(cardX, cardY, cardX + 3, cardY + cardH, i == 7 ? 0xFFFF922E : 0xFF526982);
            ItemStack stack = menu.getSlot(slotIndex).getItem();
            if (!stack.isEmpty()) {
                boolean blocked = stack.getTag() != null && stack.getTag().getBoolean("WorldBattlesRankBlocked");
                boolean selected = stack.getTag() != null && stack.getTag().getBoolean("WorldBattlesRankSelected");
                boolean owned = stack.getTag() != null && stack.getTag().getBoolean("WorldBattlesRankOwned");
                if (selected) {
                    // Orange outline and marker make the currently equipped rank easy to spot.
                    g.fill(cardX, cardY, cardX + cardW, cardY + 2, 0xFFFF922E);
                    g.fill(cardX, cardY + cardH - 2, cardX + cardW, cardY + cardH, 0xFFFF922E);
                    g.fill(cardX, cardY, cardX + 2, cardY + cardH, 0xFFFF922E);
                    g.fill(cardX + cardW - 2, cardY, cardX + cardW, cardY + cardH, 0xFFFF922E);
                    g.fill(cardX + cardW - 8, cardY + 5, cardX + cardW - 4, cardY + 9, 0xFFFFB347);
                }
                drawLock(g, cardX + 6, cardY + 6,
                    stack.is(com.worldbattles.nametags.ModItems.RANK_LOCK_OPEN.get()), blocked);
                g.drawString(font, stack.getHoverName().getString(), cardX + 26, cardY + 2, selected ? 0xFFFFB347 : 0xFF55FF55, false);
                String statusText = selected ? "✓ ACTIF" : owned ? "OBTENU" : rankPrice(i);
                int statusColor = selected ? 0xFFFF922E : owned ? 0xFF55DD88 : 0xFFFFC66D;
                g.drawString(font, statusText, cardX + 26, cardY + 16, statusColor, false);
            }
        }

        g.fill(x + 12, y + 290, x + PANEL_W - 12, y + 291, 0xFF34465B);
        g.drawString(font, "Clique sur un grade pour le choisir", x + 12, y + 300, 0xFF9CAFC4, false);
        int bx = x + PANEL_W - 68;
        int by = y + PANEL_H - 22;
        boolean closeHover = mouseX >= bx && mouseX < bx + 54 && mouseY >= by && mouseY < by + 16;
        g.fill(bx, by, bx + 56, by + 16, closeHover ? 0xFF9A3D32 : 0xFF542B2B);
        g.drawString(font, "FERMER", bx + 4, by + 4, 0xFFFFFFFF, false);

        // Preserve vanilla packet-based slot clicks without rendering its chest UI.
        // Inventory slots are not drawn or exposed in this screen.
        renderTooltip(g, mouseX, mouseY);
    }

    private String rankPrice(int index) {
        return switch (index) {
            case 0 -> "GRATUIT";
            case 1 -> "1 000 $";
            case 2 -> "10 000 $";
            case 3 -> "100 000 $";
            case 4 -> "1 000 000 $";
            case 5 -> "5 000 000 $";
            case 6 -> "20 000 000 $";
            default -> "ADMIN";
        };
    }

    private void drawLock(GuiGraphics g, int x, int y, boolean open, boolean blocked) {
        int shackle = open ? x + 7 : x + 4;
        int metal = blocked ? 0xFFFF3030 : 0xFFFFD45A;
        int body = blocked ? 0xFFB51F2A : 0xFFFFB52E;
        int highlight = blocked ? 0xFFFF6464 : 0xFFFFD84D;
        g.fill(shackle, y, shackle + 8, y + 3, metal);
        g.fill(shackle, y + 1, shackle + 3, y + 7, metal);
        g.fill(shackle + 5, y + 1, shackle + 8, y + 7, metal);
        g.fill(x + 1, y + 6, x + 15, y + 15, body);
        g.fill(x + 3, y + 8, x + 13, y + 13, highlight);
        g.fill(x + 7, y + 9, x + 9, y + 12, blocked ? 0xFF650B12 : 0xFF5A3418);
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
                int cardX = i == 7 ? x + 150 : x + 14;
                int cardY = i == 7 ? y + 68 : y + 58 + i * 32;
                int cardW = i == 7 ? 96 : 136;
                if (mouseX >= cardX && mouseX < cardX + cardW && mouseY >= cardY && mouseY < cardY + 28) {
                    if (minecraft != null && minecraft.gameMode != null && minecraft.player != null) {
                        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, i, 0,
                            net.minecraft.world.inventory.ClickType.PICKUP, minecraft.player);
                    }
                    return true;
                }
            }
            int bx = x + PANEL_W - 68;
            int by = y + PANEL_H - 24;
            if (mouseX >= bx && mouseX < bx + 54 && mouseY >= by && mouseY < by + 16) onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}