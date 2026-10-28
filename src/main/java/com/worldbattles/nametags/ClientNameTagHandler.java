package com.worldbattles.nametags;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = WorldBattlesNametags.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientNameTagHandler {
    private ClientNameTagHandler() {}

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Keep the original Overworld behavior unchanged: no nameplate at all.
        if (Level.OVERWORLD.equals(player.level().dimension())) {
            event.setResult(Event.Result.DENY);
            return;
        }

        // The Shop's server-side tab name carries the chosen rank and is synced to clients.
        // Reuse that synced component so nameplates match TAB/chat without a second economy.
        if (!RankSystem.allowedDimension(player)) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) return;
        PlayerInfo info = minecraft.getConnection().getPlayerInfo(player.getUUID());
        if (info == null) return;
        Component display = info.getTabListDisplayName();
        if (display == null) display = Component.literal(player.getGameProfile().getName());

        String plain = display.getString();
        if (plain.startsWith("[FOU DU JEU] ")) {
            int offset = (int) ((System.currentTimeMillis() / 180L) % 6);
            ChatFormatting[] colors = {ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW,
                ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.LIGHT_PURPLE};
            String label = "FOU DU JEU";
            MutableComponent animated = Component.literal("[");
            for (int i = 0; i < label.length(); i++) {
                animated.append(Component.literal(String.valueOf(label.charAt(i))).withStyle(colors[(i + offset) % colors.length]));
            }
            animated.append(Component.literal("] ").withStyle(ChatFormatting.WHITE));
            display = animated.append(Component.literal(plain.substring(13)));
        }
        event.setContent(display);
    }
}
