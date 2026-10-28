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
                int index = col + row * 9;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                if (index == 4) { x = 142; y = 8; }
                else if (index == 10) { x = 10; y = 38; }
                else if (index == 19) { x = 10; y = 56; }
                else if (index == 28) { x = 10; y = 74; }
                else if (index == 37) { x = 10; y = 92; }
                else if (index == 46) { x = 10; y = 110; }
                else if (index == 12) { x = 10; y = 128; }
                else if (index == 21) { x = 10; y = 146; }
                else if (index == 31) { x = 104; y = 38; }
                else { x = -1000; y = -1000; }
                this.addSlot(new Slot(ranks, index, x, y) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                    @Override public boolean mayPickup(Player player) { return false; }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 168 + row * 18));
        }
        for (int col = 0; col < 9; col++) this.addSlot(new Slot(inventory, col, 8 + col * 18, 222));
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
