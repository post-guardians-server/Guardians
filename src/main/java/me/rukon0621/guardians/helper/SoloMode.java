package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;

public class SoloMode implements Listener {

    public static Set<Player> usingSoloMode = new HashSet<>();
    private static final main plugin = main.getPlugin();

    public SoloMode() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void soloMode(Player player, boolean bool) {
        if(bool) {
            usingSoloMode.add(player);
            for(Player loop_player : plugin.getServer().getOnlinePlayers()) {
                if(loop_player.equals(player)) continue;
                player.hidePlayer(plugin, loop_player);
            }
        } else {
            usingSoloMode.remove(player);
            for(Player loop_player : plugin.getServer().getOnlinePlayers()) {
                if(loop_player.equals(player)) continue;
                player.showPlayer(plugin, loop_player);
            }
        }
    }

    public static boolean isUsingSoloMode(Player player) {
        return usingSoloMode.contains(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        for(Player player : usingSoloMode) {
            player.hidePlayer(plugin, e.getPlayer());
        }
    }
}
