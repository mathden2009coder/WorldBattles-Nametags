package com.worldbattles.nametags;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WorldBattlesNametags.MOD_ID)
public class WorldBattlesNametags {
    public static final String MOD_ID = "worldbattles_nametags";

    public WorldBattlesNametags() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
    }
}
