package com.worldbattles.nametags;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.TabListNameFormat;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.TabListNameFormat;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = WorldBattlesNametags.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RankSystem {
    private static final String DATA_KEY = "worldbattles_ranks_v1";
    private static final String SHOP_MOD = "worldbattlesshop";
    private static final Rank[] RANKS = new Rank[] {
        new Rank("novice", "Novice", 0L, ChatFormatting.GRAY, Items.GRAY_DYE),
        new Rank("debutant", "Débutant", 1_000L, ChatFormatting.GREEN, Items.LIME_DYE),
        new Rank("adepte", "Adepte", 10_000L, ChatFormatting.YELLOW, Items.GOLD_INGOT),
        new Rank("combattant", "Combattant", 100_000L, ChatFormatting.GOLD, Items.ORANGE_DYE),
        new Rank("conquerant", "Conquérant", 1_000_000L, ChatFormatting.BLUE, Items.LAPIS_LAZULI),
        new Rank("sage", "Sage", 5_000_000L, ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_SHARD),
        new Rank("fou", "Fou du jeu", 20_000_000L, ChatFormatting.WHITE, Items.NETHER_STAR),
        new Rank("admin", "Admin", -1L, ChatFormatting.RED, Items.REDSTONE)
    };

    private RankSystem() {}

    private record Rank(String id, String label, long price, ChatFormatting color, Item icon) {}

    private static Rank rank(String id) {
        for (Rank r : RANKS) if (r.id.equals(id)) return r;
        return RANKS[0];
    }

    public static boolean allowedDimension(Player player) {
        ResourceLocation id = player.level().dimension().location();
        String key = id.toString().toLowerCase(Locale.ROOT);
        return !Level.OVERWORLD.equals(player.level().dimension())
            && (key.contains("secondworld") || key.contains("freelands") || key.contains("minage"));
    }

    private static RankData data(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(RankData::load, RankData::new, DATA_KEY);
    }

    private static String selected(Player player) {
        if (!(player instanceof ServerPlayer sp)) return "novice";
        RankData d = data(sp.getServer());
        if (sp.getServer().getPlayerList().isOp(sp.getGameProfile())) {
            d.unlock(sp.getUUID(), "admin");
            if (d.selected(sp.getUUID()).equals("novice")) d.select(sp.getUUID(), "admin");
        }
        d.ensure(sp.getUUID());
        return d.selected(sp.getUUID());
    }

    public static Component prefixFor(Player player) {
        if (!allowedDimension(player)) return Component.empty();
        String id = selected(player);
        Rank r = rank(id);
        if (id.equals("fou")) {
            int offset = (int)((System.currentTimeMillis() / 180L) % 6);
            ChatFormatting[] colors = {ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW,
                ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.LIGHT_PURPLE};
            MutableComponent out = Component.literal("[");
            String label = r.label.toUpperCase(Locale.ROOT);
            for (int i = 0; i < label.length(); i++) out.append(Component.literal(String.valueOf(label.charAt(i))
                ).withStyle(colors[(i + offset) % colors.length]));
            return out.append(Component.literal("] ").withStyle(ChatFormatting.WHITE));
        }
        return Component.literal("[" + r.label.toUpperCase(Locale.ROOT) + "] ").withStyle(r.color);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("grades")
            .executes(ctx -> {
                if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                    open(player);
                    return 1;
                }
                return 0;
            }));

        event.getDispatcher().register(Commands.literal("gradeadmin")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("joueur", EntityArgument.player())
                .then(Commands.argument("grade", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        for (Rank r : RANKS) builder.suggest(r.id);
                        return builder.buildFuture();
                    })
                    .then(Commands.literal("bloquer").executes(ctx -> setRankBlocked(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "joueur"), StringArgumentType.getString(ctx, "grade"), true)))
                    .then(Commands.literal("debloquer").executes(ctx -> setRankBlocked(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "joueur"), StringArgumentType.getString(ctx, "grade"), false)))
                )
            ));
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RankData d = data(player.getServer());
            d.ensure(player.getUUID());
            if (player.getServer().getPlayerList().isOp(player.getGameProfile())) {
                d.unlock(player.getUUID(), "admin");
                d.select(player.getUUID(), "admin");
            }
            player.refreshDisplayName();
            player.refreshTabListName();
        }
    }

    @SubscribeEvent
    public static void animateRainbowTab(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 5 != 0 || !allowedDimension(player)) return;
        if ("fou".equals(selected(player))) player.refreshTabListName();
    }

    @SubscribeEvent
    public static void onDimension(PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.refreshDisplayName();
            player.refreshTabListName();
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.refreshDisplayName();
            player.refreshTabListName();
        }
    }

    @SubscribeEvent
    public static void onNameFormat(NameFormat event) {
        Player player = event.getEntity();
        if (!allowedDimension(player)) return;
        Component base = Component.literal(player.getGameProfile().getName()).withStyle(ChatFormatting.WHITE);
        event.setDisplayname(prefixFor(player).copy().append(base));
    }

    @SubscribeEvent
    public static void onTabName(TabListNameFormat event) {
        Player player = event.getEntity();
        if (!allowedDimension(player)) return;
        Component base = Component.literal(player.getGameProfile().getName()).withStyle(ChatFormatting.WHITE);
        event.setDisplayName(prefixFor(player).copy().append(base));
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        // ServerChatEvent does not expose a mutable username on Forge 1.20.1.
        // PlayerEvent.NameFormat supplies the prefix for the chat sender component.
    }

    private static int setRankBlocked(CommandSourceStack source, ServerPlayer target, String rankId, boolean blocked) {
        Rank rank = null;
        for (Rank candidate : RANKS) {
            if (candidate.id.equalsIgnoreCase(rankId) || candidate.label.equalsIgnoreCase(rankId)) { rank = candidate; break; }
        }
        if (rank == null) {
            source.sendFailure(Component.literal("Grade inconnu. Utilise : novice, debutant, adepte, combattant, conquerant, sage, fou ou admin."));
            return 0;
        }
        if (rank.id.equals("novice")) {
            source.sendFailure(Component.literal("Le grade novice ne peut pas être bloqué."));
            return 0;
        }
        RankData d = data(target.getServer());
        d.ensure(target.getUUID());
        d.setBlocked(target.getUUID(), rank.id, blocked);
        if (blocked && d.selected(target.getUUID()).equals(rank.id)) {
            d.select(target.getUUID(), "novice");
            target.refreshDisplayName();
            target.refreshTabListName();
        }
        target.sendSystemMessage(Component.literal("Le grade " + rank.label + " a été " + (blocked ? "bloqué" : "débloqué") + " par un administrateur.").withStyle(blocked ? ChatFormatting.RED : ChatFormatting.GREEN));
        source.sendSuccess(() -> Component.literal("Grade " + rank.label + (blocked ? " bloqué pour " : " débloqué pour ") + target.getGameProfile().getName() + "."), true);
        return 1;
    }

    private static final int[] RANK_SLOTS = {10, 19, 28, 37, 46, 12, 21, 31};

    private static void open(ServerPlayer player) {
        if (!allowedDimension(player)) {
            player.sendSystemMessage(Component.literal("Les grades sont disponibles uniquement dans Freelands et Minage.").withStyle(ChatFormatting.RED));
            return;
        }
        RankData d = data(player.getServer());
        d.ensure(player.getUUID());
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> {
            RankMenu menu = new RankMenu(id, inv);
            long balance = shopBalance(player);
            menu.setBalance(Math.max(0, balance));
            for (int i = 0; i < RANKS.length; i++) {
                Rank r = RANKS[i];
                if (r.id.equals("admin") && !player.getServer().getPlayerList().isOp(player.getGameProfile())) continue;
                boolean blocked = d.isBlocked(player.getUUID(), r.id);
                boolean unlocked = !blocked && (d.has(player.getUUID(), r.id) || (r.id.equals("admin") && player.getServer().getPlayerList().isOp(player.getGameProfile())));
                ItemStack icon = new ItemStack(unlocked ? ModItems.RANK_LOCK_OPEN.get() : ModItems.RANK_LOCK_CLOSED.get());
                icon.setHoverName(Component.literal(r.label.toUpperCase(Locale.ROOT)).withStyle(unlocked ? ChatFormatting.GREEN : ChatFormatting.GRAY));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.literal(r.price < 0 ? "Réservé aux administrateurs" : r.price == 0 ? "Prix : Gratuit" : "Prix : " + String.format(Locale.CANADA_FRENCH, "%,d", r.price) + " $").withStyle(ChatFormatting.GOLD));
                lore.add(Component.literal(blocked ? "Bloqué par un administrateur" : unlocked ? (d.selected(player.getUUID()).equals(r.id) ? "GRADE ACTIF" : "Débloqué — cliquer pour choisir") : "Verrouillé — cliquer pour acheter").withStyle(blocked ? ChatFormatting.RED : unlocked ? ChatFormatting.GREEN : ChatFormatting.GRAY));
                CompoundTag tag = icon.getOrCreateTag();
                ListTag loreTag = new ListTag();
                for (Component line : lore) loreTag.add(StringTag.valueOf(Component.Serializer.toJson(line)));
                CompoundTag display = tag.getCompound("display");
                display.put("Lore", loreTag);
                tag.put("display", display);
                menu.setRankItem(RANK_SLOTS[i], icon);
            }
            ItemStack balanceItem = new ItemStack(Items.GOLD_INGOT);
            balanceItem.setHoverName(Component.literal("Solde : " + String.format(Locale.CANADA_FRENCH, "%,d", Math.max(0, balance)) + " $").withStyle(ChatFormatting.GOLD));
            menu.setRankItem(4, balanceItem);
            return menu;
        }, Component.literal("WORLD BATTLES | GRADES")));
    }

    public static void handleRankClick(ServerPlayer target, int slot) {
        int index = -1;
        for (int i = 0; i < RANK_SLOTS.length; i++) if (RANK_SLOTS[i] == slot) { index = i; break; }
        if (index < 0) return;
        Rank r = RANKS[index];
        if (!allowedDimension(target)) return;
        RankData current = data(target.getServer());
        current.ensure(target.getUUID());
        boolean op = target.getServer().getPlayerList().isOp(target.getGameProfile());
        if (r.id.equals("admin") && !op) {
            target.sendSystemMessage(Component.literal("Ce grade est réservé aux administrateurs.").withStyle(ChatFormatting.RED));
            open(target);
            return;
        }
        if (current.isBlocked(target.getUUID(), r.id)) {
            target.sendSystemMessage(Component.literal("Ce grade a été bloqué par un administrateur.").withStyle(ChatFormatting.RED));
            open(target);
            return;
        }
        if (!current.has(target.getUUID(), r.id)) {
            if (r.id.equals("admin") || r.price < 0) {
                target.sendSystemMessage(Component.literal("Ce grade ne peut pas être acheté.").withStyle(ChatFormatting.RED));
                open(target);
                return;
            }
            long balance = shopBalance(target);
            if (balance < r.price) {
                target.sendSystemMessage(Component.literal("Fonds insuffisants. Solde : " + Math.max(0, balance) + " $.").withStyle(ChatFormatting.RED));
                open(target);
                return;
            }
            if (!setShopBalance(target, balance - r.price)) {
                target.sendSystemMessage(Component.literal("Impossible de débiter le compte du Shop.").withStyle(ChatFormatting.RED));
                open(target);
                return;
            }
            current.unlock(target.getUUID(), r.id);
            target.sendSystemMessage(Component.literal("Grade " + r.label + " acheté pour " + r.price + " $.").withStyle(ChatFormatting.GREEN));
        }
        current.select(target.getUUID(), r.id);
        target.refreshDisplayName();
        target.refreshTabListName();
        target.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(java.util.EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME), java.util.List.of(target)));
        target.sendSystemMessage(Component.literal("Grade sélectionné : ").withStyle(ChatFormatting.GOLD).append(Component.literal(r.label).withStyle(r.color)));
        open(target);
    }

    private static long shopBalance(ServerPlayer player) {
        try {
            Class<?> dataClass = Class.forName("com.worldbattles.shop.ShopData");
            Object shop = dataClass.getMethod("get", MinecraftServer.class).invoke(null, player.getServer());
            return (long) dataClass.getMethod("balance", UUID.class).invoke(shop, player.getUUID());
        } catch (Exception ex) {
            return -1L;
        }
    }

    private static boolean setShopBalance(ServerPlayer player, long value) {
        try {
            Class<?> dataClass = Class.forName("com.worldbattles.shop.ShopData");
            Object shop = dataClass.getMethod("get", MinecraftServer.class).invoke(null, player.getServer());
            Object result = dataClass.getMethod("adminSet", UUID.class, String.class, long.class)
                .invoke(shop, player.getUUID(), player.getGameProfile().getName(), value);
            Method success = result.getClass().getMethod("ok");
            return (boolean) success.invoke(result);
        } catch (Exception ex) {
            return false;
        }
    }

    private static final class RankData extends SavedData {
        private final Map<UUID, String> selected = new HashMap<>();
        private final Map<UUID, Set<String>> unlocked = new HashMap<>();
        private final Map<UUID, Set<String>> blocked = new HashMap<>();

        static RankData load(CompoundTag tag) {
            RankData d = new RankData();
            ListTag entries = tag.getList("Players", 10);
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag e = entries.getCompound(i);
                if (!e.hasUUID("UUID")) continue;
                UUID id = e.getUUID("UUID");
                String selected = e.getString("Selected");
                d.selected.put(id, selected.isEmpty() ? "novice" : selected);
                Set<String> set = new HashSet<>();
                ListTag ranks = e.getList("Unlocked", 8);
                for (int j = 0; j < ranks.size(); j++) set.add(ranks.getString(j));
                set.add("novice");
                d.unlocked.put(id, set);
                Set<String> blockedSet = new HashSet<>();
                ListTag blockedRanks = e.getList("Blocked", 8);
                for (int j = 0; j < blockedRanks.size(); j++) blockedSet.add(blockedRanks.getString(j));
                d.blocked.put(id, blockedSet);
            }
            return d;
        }

        @Override public CompoundTag save(CompoundTag tag) {
            ListTag entries = new ListTag();
            for (UUID id : selected.keySet()) {
                CompoundTag e = new CompoundTag();
                e.putUUID("UUID", id);
                e.putString("Selected", selected.getOrDefault(id, "novice"));
                ListTag ranks = new ListTag();
                for (String rank : unlocked.getOrDefault(id, Set.of("novice"))) ranks.add(StringTag.valueOf(rank));
                e.put("Unlocked", ranks);
                ListTag blockedRanks = new ListTag();
                for (String rank : blocked.getOrDefault(id, Set.of())) blockedRanks.add(StringTag.valueOf(rank));
                e.put("Blocked", blockedRanks);
                entries.add(e);
            }
            tag.put("Players", entries);
            return tag;
        }

        void ensure(UUID id) {
            if (!selected.containsKey(id)) {
                selected.put(id, "novice");
                unlocked.put(id, new HashSet<>(Set.of("novice")));
                setDirty();
            }
        }
        String selected(UUID id) { ensure(id); return selected.getOrDefault(id, "novice"); }
        boolean has(UUID id, String rank) { ensure(id); return unlocked.getOrDefault(id, Set.of()).contains(rank); }
        boolean isBlocked(UUID id, String rank) { ensure(id); return blocked.getOrDefault(id, Set.of()).contains(rank); }
        void setBlocked(UUID id, String rank, boolean value) {
            ensure(id);
            Set<String> set = blocked.computeIfAbsent(id, k -> new HashSet<>());
            boolean changed = value ? set.add(rank) : set.remove(rank);
            if (changed) setDirty();
        }
        void unlock(UUID id, String rank) { ensure(id); if (unlocked.computeIfAbsent(id, k -> new HashSet<>()).add(rank)) setDirty(); }
        void select(UUID id, String rank) { ensure(id); if (!Objects.equals(selected.get(id), rank)) { selected.put(id, rank); setDirty(); } }
    }
}
