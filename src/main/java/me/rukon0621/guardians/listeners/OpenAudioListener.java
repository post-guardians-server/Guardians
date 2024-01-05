package me.rukon0621.guardians.listeners;

import com.craftmend.openaudiomc.api.interfaces.AudioApi;
import com.craftmend.openaudiomc.api.interfaces.Client;
import com.craftmend.openaudiomc.generic.media.objects.MediaOptions;
import com.craftmend.openaudiomc.spigot.modules.players.events.ClientConnectEvent;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.events.GuardiansLoginEvent;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.region.RegionManager;
import me.rukon0621.guardians.story.StoryManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static me.rukon0621.guardians.main.pfix;

public class OpenAudioListener implements Listener {
    private final static main plugin = main.getPlugin();
    private static final Map<String, String> playingBgm = new HashMap<>();
    private static final Set<Player> notConnectedPlayers = new HashSet<>();

    public OpenAudioListener() {
        main.getPlugin().getServer().getPluginManager().registerEvents(this, main.getPlugin());
    }

    @EventHandler
    public void onClientConnected(ClientConnectEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = e.getPlayer();
                playingBgm.remove(player.getUniqueId().toString());
                notConnectedPlayers.remove(player);
                ChatEventListener.getBlockedPlayers().remove(player);
                if(RegionManager.getIgnoreRegionPlayers().contains(player)) return;
                RegionManager.reloadBgm(player);
                if(StoryManager.getPlayingStory(player)!=null) return;
                PlayerData.setPlayerStun(player, false);
                ChatEventListener.getBlockedPlayers().remove(player);
            }
        }.runTaskLater(plugin, 5);
    }

    @EventHandler
    public void onPlayerJoin(GuardiansLoginEvent e) {
        Player player = e.getPlayer();
        ChatEventListener.getBlockedPlayers().remove(player);
        //notConnectedPlayers.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!notConnectedPlayers.contains(player)) return;
                Msg.send(player, " ");
                Msg.send(player, " ");
                Msg.send(player, "&e오디오 클라이언트에 연결해주시기 바랍니다!", pfix);
                Msg.send(player, "&7  - 혹시나 연결이 되지 않는다면 &a재접속&7하거나 &a/audio로 다시 연결&7해보세요!");
                Msg.send(player, " ");
                Msg.send(player, new ComponentBuilder(Msg.color("&7[ &c! &7] &e이곳&f을 클릭해 링크를 재생성"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder(Msg.color("&e클릭해 오디오 링크를 다시 띄웁니다.")).create()
                        ))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/audio")).create());
                player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1, 0.7f);
                player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1, 0.7f);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2f);
                player.playSound(player, Sound.ENTITY_ENDER_DRAGON_HURT, 1, 0.7f);
            }
        }.runTaskLater(plugin, 200);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        notConnectedPlayers.remove(e.getPlayer());
        playingBgm.remove(e.getPlayer().getUniqueId().toString());
    }

    /**
     *
     * @param player player
     * @param fileName 파일 이름
     * @return 기존 bgm 재생을 멈추고 새로운 bgm 재생, 이미 그 브금이 재생중이면 무시
     */
    public static boolean playBgm(Player player, String fileName) {
        AudioApi api = AudioApi.getInstance();
        Client client = api.getClient(player.getUniqueId());
        if(client==null) return false;

        String cntBgm = playingBgm.get(player.getUniqueId().toString());

        if(cntBgm!=null&&cntBgm.equals(fileName)) return true;
        stopSong(client, "bgm");
        return playSong(client, fileName, true, "bgm", 2500);
    }

    public static void stopSong(Player player, String id) {
        AudioApi api = AudioApi.getInstance();
        Client client = api.getClient(player.getUniqueId());
        if(client==null) return;
        if(!client.isConnected()) return;
        stopSong(client, id);
    }
    private static void stopSong(Client client, String id) {
        if(!client.isConnected()) return;
        playingBgm.remove(client.getUser().getUniqueId().toString());
        AudioApi api = AudioApi.getInstance();
        api.getMediaApi().stopMedia(client, id);
    }

    public static boolean playSong(Player player, String fileName, boolean loop, String id, int fadeMillis) {
        AudioApi api = AudioApi.getInstance();
        Client client = api.getClient(player.getUniqueId());
        if(client==null) return false;
        if(!client.isConnected()) return false;
        new BukkitRunnable() {
            @Override
            public void run() {
                AudioApi api = AudioApi.getInstance();
                MediaOptions options = new MediaOptions();
                options.setLoop(loop);
                options.setFadeTime(fadeMillis);
                options.setId(id);
                api.getMediaApi().playMedia(client, "files:"+fileName, options);
            }
        }.runTaskLaterAsynchronously(main.getPlugin(), 25);
        return true;
    }
    private static boolean playSong(Client client, String fileName, boolean loop, String id, int fadeMillis) {
        if(!client.isConnected()) return false;
        new BukkitRunnable() {
            @Override
            public void run() {
                playingBgm.put(client.getUser().getUniqueId().toString(), fileName);
                AudioApi api = AudioApi.getInstance();
                MediaOptions options = new MediaOptions();
                options.setLoop(loop);
                options.setFadeTime(fadeMillis);
                options.setId(id);
                api.getMediaApi().playMedia(client, "files:"+fileName, options);
            }
        }.runTaskLaterAsynchronously(main.getPlugin(), 25);
        return true;
    }

}
