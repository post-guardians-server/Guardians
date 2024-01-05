package me.rukon0621.guardians.listeners;

import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class ResourcePackListener implements Listener {

    private static final HashSet<Player> acceptedPlayers = new HashSet<>();

    public ResourcePackListener() {
        Bukkit.getPluginManager().registerEvents(this, main.getPlugin());
    }

    /**
     *
     * @param player player
     * @return 플레이어가 서버 리소스팩을 사용하는지 반환
     */
    public static boolean acceptResourcePack(Player player) {
        return true;
        //return acceptedPlayers.contains(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        acceptedPlayers.remove(e.getPlayer());
    }

    @EventHandler
    public void resourcePackStatus(PlayerResourcePackStatusEvent e) {
        if(e.getStatus().equals(PlayerResourcePackStatusEvent.Status.ACCEPTED)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    acceptedPlayers.add(e.getPlayer());
                }
            }.runTaskLater(main.getPlugin(), 10);
        }
    }
}
