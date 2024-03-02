package me.rukon0621.guardians.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rukon0621.callback.speaker.Speaker;
import me.rukon0621.callback.speaker.SpeakerListenEvent;
import me.rukon0621.guardians.GUI.ChatSettingWindow;
import me.rukon0621.guardians.GUI.TitleWindow;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.DateUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.NullManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.party.PartyManager;
import me.rukon0621.guardians.story.StoryManager;
import net.playavalon.avnparty.AvNParty;
import net.playavalon.avnparty.party.Party;
import net.playavalon.avnparty.player.AvalonPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.rukon0621.guardians.main.chatChannel;
import static me.rukon0621.guardians.main.pfix;

public class ChatEventListener implements Listener, PluginMessageListener {

    public enum ChatChannel {
        ALL("&a『 전체 』", "all", false, 84, 60),
        CHANNEL("&6『 채널 』", "channel", false, 85, 40),
        PARTY("&b『 파티 』", "party", false, 86, 0),
        GUILD("&e『 길드 』", "guild", false, 86, 0),
        WHISPER("&7『 귓속말 』", "whisper", true, 0, 0);

        private final String channelPrefix;
        private final String str;
        private final int delayTick;
        private final boolean blockChoice;
        private final int cmd;

        ChatChannel(String channelPrefix, String str, boolean blockChoice, int cmd, int delayTick) {
            this.channelPrefix = channelPrefix;
            this.str = str;
            this.blockChoice = blockChoice;
            this.cmd = cmd;
            this.delayTick = delayTick;
        }

        public int getCmd() {
            return cmd;
        }

        public boolean isBlockChoice() {
            return blockChoice;
        }

        public String getChannelPrefix() {
            return channelPrefix;
        }
        public String getChannelPrefix(String channel) {
            if(channel.contains("ch")) {
                channel = channel.replaceAll("ch", "");
            }
            else if(channel.contains("dev")) {
                return "&7『 개발 서버 』";
            }
            else if (channel.equals("testServer")) {
                channel = "테스트 ";
            }
            else if (channel.equals("voidLand")) {
                return "&7『 공허의 땅 』";
            }
            return String.format("&7『 %s채널 』", channel);
        }

        public int getDelayTick() {
            return delayTick;
        }

        public String getStr() {
            return str;
        }
    }

    private static final main plugin = main.getPlugin();
    private static final AvNParty partyPlugin = AvNParty.plugin;

    private static final Map<Player, Set<String>> playerChatCache = new HashMap<>();
    private static final Map<Player, ChatChannel> playerChatChannel = new HashMap<>();
    private static final Set<Player> blockedPlayers = new HashSet<>();
    private static final Set<Player> delayedPlayers = new HashSet<>();

    public static Set<String> getPlayerChatCache(Player player) {
        if(!playerChatCache.containsKey(player)) {
            Set<String> cache = new HashSet<>();
            cache.add("all");
            cache.add("channel");
            cache.add("party");
            cache.add("whisper");
            playerChatCache.put(player, cache);
        }
        return playerChatCache.get(player);
    }

    public static void setPlayerChatChannel(Player player, Set<String> data) {
        playerChatCache.put(player, data);
    }

    public static ChatChannel getPlayerChatChannel(Player player) {
        return playerChatChannel.getOrDefault(player, ChatChannel.ALL);
    }
    public static void setPlayerChatChannel(Player player, ChatChannel chatChannel) {
        playerChatChannel.put(player, chatChannel);
    }
    public static void clearPlayerCache(Player player) {
        playerChatCache.remove(player);
        playerChatChannel.remove(player);
    }

    public ChatEventListener() {
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, chatChannel, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, chatChannel);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if(e.getPlayer().isOp()) return;

        if(e.getMessage().startsWith("/minecraft:")) {
            Msg.warn(e.getPlayer(), "사용할 수 없는 커맨드입니다.");
            e.setCancelled(true);
            return;
        }

        if(e.getMessage().startsWith("/w")||e.getMessage().startsWith("/whisper")||e.getMessage().startsWith("/tell")||e.getMessage().startsWith("/msg")) {
            Msg.warn(e.getPlayer(), "사용할 수 없는 커맨드입니다.");
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        e.setCancelled(true);

        if(delayedPlayers.contains(player)) {
            Msg.warn(player, "채팅을 치려면 잠시 기다려야합니다.");
            return;
        }

        ChatChannel chatChannel = getPlayerChatChannel(player);
        String title = new PlayerData(player).getTitle();
        if(title==null) title = "null";

        if(!playerChatCache.get(player).contains(chatChannel.getStr())) {
            Msg.warn(player, "활성화하지 않은 채널에서 채팅을 칠 수 없습니다. 먼저 채널을 활성화해주세요.");
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        new ChatSettingWindow(player);
                    } catch (NoSuchFieldException ex) {
                        ex.printStackTrace();
                    }
                }
            }.runTask(plugin);
            return;
        }

        if(chatChannel==ChatChannel.PARTY) {
            //파티 채팅
            AvalonPlayer avnP = partyPlugin.getAvalonPlayer(player);
            Party party = avnP.getParty();

            if(party==null) {
                Msg.warn(player, "파티 채팅을 하려면 먼저 파티를 생성하십시오.");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PartyManager.openPartyGUI(player);
                    }
                }.runTask(plugin);
                return;
            }

            for(AvalonPlayer loopAvnP : party.getPlayers()) {
                Player lp = loopAvnP.getPlayer();
                if(!getPlayerChatCache(lp).contains(ChatChannel.PARTY.str)) continue;
                if(blockedPlayers.contains(lp)) continue;
                if(StoryManager.getPlayingStory(lp)!=null) continue;
                Msg.send(lp, formatMessage(player.getName(), title, e.getMessage(), chatChannel));
            }
        }

        else if (chatChannel==ChatChannel.CHANNEL) {
            if(getRemainMuteSecond(player) > -1) {
                Msg.warn(player, String.format("채팅 금지 시간이 %s 남아있습니다. 이 동안은 전체, 채널 채팅을 이용하실 수 없습니다.", DateUtil.formatDate(getRemainMuteSecond(player))));
                return;
            }

            for(Player p : plugin.getServer().getOnlinePlayers()) {
                if(!playerChatCache.get(p).contains(ChatChannel.CHANNEL.str)) continue;
                if(blockedPlayers.contains(p)) continue;
                if(StoryManager.getPlayingStory(p)!=null) continue;
                Msg.send(p, formatMessage(player.getName(), title, e.getMessage(), chatChannel));
            }
        }

        else if (chatChannel==ChatChannel.ALL) {
            if(getRemainMuteSecond(player) > -1) {
                Msg.warn(player, String.format("채팅 금지 시간이 %s 남아있습니다. 이 동안은 전체, 채널 채팅을 이용하실 수 없습니다.", DateUtil.formatDate(getRemainMuteSecond(player))));
                return;
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("send");
            out.writeUTF(player.getName());
            out.writeUTF(title);
            out.writeUTF(e.getMessage());
            player.sendPluginMessage(plugin, main.chatChannel, out.toByteArray());
        }
        else if (chatChannel==ChatChannel.GUILD) {
            if(getRemainMuteSecond(player) > -1) {
                Msg.warn(player, String.format("채팅 금지 시간이 %s 남아있습니다. 이 동안은 전체, 채널 채팅을 이용하실 수 없습니다.", DateUtil.formatDate(getRemainMuteSecond(player))));
                return;
            }
            PlayerData pdc = new PlayerData(player);
            if(pdc.getGuildID() == null) {
                Msg.warn(player, "길드에 가입하여야 길드 채팅을 사용할 수 있습니다.");
                return;
            }
            new Speaker("guildChat", pdc.getGuildID().toString(), player.getName(), (String) NullManager.defaultNull(pdc.getTitle(), "null"), e.getMessage());
        }

        if(chatChannel.delayTick!=0) {
            if(player.isOp() || player.hasPermission("guardians.mute")) return;
            delayedPlayers.add(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    delayedPlayers.remove(player);
                }
            }.runTaskLater(plugin, chatChannel.delayTick);
        }
    }

    public static Set<Player> getBlockedPlayers() {
        return blockedPlayers;
    }

    @EventHandler
    public void onSpeakerListen(SpeakerListenEvent e) {
        if(e.getMainAction().equals("guildChat")) {
            UUID guildID = UUID.fromString(e.getIn().readUTF());
            String sender = e.getIn().readUTF();
            String title = e.getIn().readUTF();
            String msg = e.getIn().readUTF();
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(LogInOutListener.getLoadingPlayers().contains(player.getName())) continue;
                PlayerData pdc = new PlayerData(player);
                if(pdc.getGuildID() == null || !pdc.getGuildID().equals(guildID)) continue;
                Msg.send(player, formatMessage(sender, title, msg, ChatChannel.GUILD));
            }
        }
        else if(e.getMainAction().equals("guildBroadcast")) {
            UUID guildID = UUID.fromString(e.getIn().readUTF());
            String msg = e.getIn().readUTF();
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(LogInOutListener.getLoadingPlayers().contains(player.getName())) continue;
                PlayerData pdc = new PlayerData(player);
                if(pdc.getGuildID() == null || !pdc.getGuildID().equals(guildID)) continue;
                Msg.send(player, msg, pfix);
                player.playSound(player, Sound.ENTITY_ENDER_DRAGON_HURT, 1, 1.5f);
            }
        }
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        blockedPlayers.remove(e.getPlayer());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if(!channel.equals(chatChannel)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);

        String subChannel = in.readUTF();
        if(subChannel.equals("whisper")) {
            String sender = in.readUTF();
            Player target = plugin.getServer().getPlayer(in.readUTF());
            String msg = in.readUTF();
            if(target==null) return;
            if(!getPlayerChatCache(target).contains(ChatChannel.WHISPER.getStr())) return;
            if(blockedPlayers.contains(target)) return;
            target.playSound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
            Msg.send(target, String.format("#aaddff[ %s ] %s", sender, msg));
        }
        else if(subChannel.equals("receive")) {
            String playerName = in.readUTF();
            String title = in.readUTF();
            String msg = Msg.uncolor(Msg.color(in.readUTF()));
            String chn = in.readUTF();
            String finalMsg = formatMessage(playerName, title, msg, ChatChannel.ALL, chn);
            for(Player p : plugin.getServer().getOnlinePlayers()) {
                if(!getPlayerChatCache(p).contains(ChatChannel.ALL.str)) continue;
                if(blockedPlayers.contains(p)) {
                    Msg.send(player, "");
                    continue;
                }
                if(StoryManager.getPlayingStory(p)!=null) continue;
                Msg.send(p, finalMsg);
            }
        }
        else if(subChannel.equals("broadcastReceive")) {
            String msg = in.readUTF();
            boolean ignoreStoringPlayer = msg.startsWith("ignoreStory");
            if(ignoreStoringPlayer) {
                msg = msg.replaceFirst("ignoreStory", "");
            }
            for(Player p : plugin.getServer().getOnlinePlayers()) {
                if(ignoreStoringPlayer && StoryManager.getPlayingStory(player) != null) continue;
                Msg.send(p, " ");
                Msg.send(p, msg ,"&e[ &c전체공지 &e] &f");
                Msg.send(p, " ");
            }
        }
    }

    private String formatMessage(String playerName, @NotNull String title, String message, ChatChannel chatChannel) {
        if(title.equals("null")) return Msg.color(String.format("%s &f<%s> %s",chatChannel.channelPrefix, playerName, message));
        return Msg.color(String.format("%s %s &f<%s> %s",chatChannel.channelPrefix, TitleWindow.getPureTitle(title), playerName, message));
    }
    private String formatMessage(String playerName, @NotNull String title, String message, ChatChannel chatChannel, String channelFrom) {
        if(title.equals("null")) return Msg.color(String.format("%s &f<%s> %s",chatChannel.getChannelPrefix(channelFrom), playerName, message));
        return Msg.color(String.format("%s %s &f<%s> %s",chatChannel.getChannelPrefix(channelFrom), TitleWindow.getPureTitle(title), playerName, message));
    }

    /**
     *
     * @param player player
     * @return 남은 뮤트 시간(초)를 반환, 뮤트 상태가 아니면 -1 반환
     */
    public static long getRemainMuteSecond(Player player) {
        PlayerData pdc = new PlayerData(player);
        return Math.max(-1, (pdc.getMuteMillis() - System.currentTimeMillis()) / 1000L);
    }
}
