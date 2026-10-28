package com.worldbattles.nametags;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, WorldBattlesNametags.MOD_ID);
    public static final RegistryObject<MenuType<RankMenu>> RANK_MENU = MENUS.register("rank_menu", () -> IForgeMenuType.create((id, inv, data) -> new RankMenu(id, inv)));
    private ModMenus() {}
}
