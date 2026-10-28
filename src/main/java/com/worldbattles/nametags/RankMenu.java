package com.worldbattles.nametags;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class RankMenu extends AbstractContainerMenu {
    private final SimpleContainer ranks = new SimpleContainer(54);
    private final DataSlot balance = DataSlot.standalone();

    public RankMenu(int id, Inventory inventory) {
        super(ModMenus.RANK_MENU.get(), id);
        addDataSlot(balance);
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(ranks, col + row * 9, 8 + col * 18, 18 + row * 18) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                    @Override public boolean mayPickup(Player player) { return false; }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
        }
        for (int col = 0; col < 9; col++) this.addSlot(new Slot(inventory, col, 8 + col * 18, 198));
    }

    public void setRankItem(int slot, ItemStack stack) { ranks.setItem(slot, stack); }
    public void setBalance(long value) { balance.set((int)Math.max(0, Math.min(Integer.MAX_VALUE, value))); }
    public int getBalance() { return balance.get(); }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < 54 && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            RankSystem.handleRankClick(serverPlayer, slotId);
            return;
        }
        if (slotId >= 54) super.clicked(slotId, button, clickType, player);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return true; }
}
