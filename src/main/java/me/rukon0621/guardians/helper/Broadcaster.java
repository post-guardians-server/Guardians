package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Broadcaster {
    public static void broadcast(String message) {
        broadcast(message, true);
    }
    public static void broadcast(String message, boolean ignoreStory) {
        broadcast(message, ignoreStory, true);
    }
    public static void broadcast(String message, boolean ignoreStory, boolean outerLine) {
        for(Player player : main.getPlugin().getServer().getOnlinePlayers()) {

            if(!ignoreStory) {
                if(StoryManager.getPlayingStory(player)!=null) {
                    continue;
                }
            }

            if(outerLine) Msg.send(player, " ");
            Msg.send(player, message, "&e[ &c공 지 &e] &e");
            if(outerLine) Msg.send(player, " ");
        }
    }

    public static void broadcastSyncWithSound(String message) {
        broadcastSyncWithSound(message, false);
    }

    public static void broadcastSyncWithSound(String message, boolean ignoreStory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : main.getPlugin().getServer().getOnlinePlayers()) {
                    if(!ignoreStory&&StoryManager.getPlayingStory(player)!=null) continue;
                    Msg.send(player, " ");
                    Msg.send(player, message, "&e[ &c공 지 &e] &e");
                    Msg.send(player, " ");
                    player.playSound(player, Sound.ENTITY_ENDER_DRAGON_HURT, 1, 0.8f);
                }
            }
        }.runTask(main.getPlugin());
    }

}
