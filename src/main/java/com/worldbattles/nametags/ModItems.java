package com.worldbattles.nametags;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, WorldBattlesNametags.MOD_ID);

    public static final RegistryObject<Item> RANK_LOCK_CLOSED =
        ITEMS.register("rank_lock_closed", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RANK_LOCK_OPEN =
        ITEMS.register("rank_lock_open", () -> new Item(new Item.Properties()));

    private ModItems() {}
}
