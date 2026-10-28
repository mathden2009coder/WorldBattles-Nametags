package com.worldbattles.nametags;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RankMenu extends AbstractContainerMenu {
    private final SimpleContainer ranks = new SimpleContainer(9);
    private final DataSlot balance = DataSlot.standalone();

    public RankMenu(int id, Inventory inventory) {
        super(ModMenus.RANK_MENU.get(), id);
        addDataSlot(balance);

        // Seven normal ranks stacked vertically; Admin stays separate on the right.
        for (int i = 0; i < 8; i++) {
            final int rankIndex = i;
            int x = i == 7 ? 150 : 14;
            int y = i == 7 ? 68 : 58 + i * 32;
            addSlot(new Slot(ranks, i, x, y) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        }
        // Balance display is not clickable.
        addSlot(new Slot(ranks, 8, 9999, 9999) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return false; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 9999, 9999));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 9999, 9999));
        }
    }

    public void setRankItem(int slot, ItemStack stack) { ranks.setItem(slot, stack); }
    public void setBalance(long value) { balance.set((int)Math.max(0, Math.min(Integer.MAX_VALUE, value))); }
    public int getBalance() { return balance.get(); }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < 8 && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            RankSystem.handleRankClick(serverPlayer, slotId);
            return;
        }
        if (slotId >= 9) super.clicked(slotId, button, clickType, player);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return true; }
}