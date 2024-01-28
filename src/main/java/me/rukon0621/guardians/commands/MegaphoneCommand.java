package me.rukon0621.guardians.commands;

import me.rukon0621.callback.speaker.Speaker;
import me.rukon0621.callback.speaker.SpeakerListenEvent;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.DateUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.ChatEventListener;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MegaphoneCommand implements CommandExecutor, Listener {

    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    public MegaphoneCommand() {
        main.getPlugin().getCommand("megaphone").setExecutor(this);
        Bukkit.getServer().getPluginManager().registerEvents(this, main.getPlugin());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;

        if(ChatEventListener.getRemainMuteSecond(player) > -1) {
            Msg.warn(player, "뮤트 중에는 사용할 수 없습니다.");
            return true;
        }

        if(args.length == 0) {
            Msg.send(player, "&6/확성기 <메세지>");
            return true;
        }
        else if(getCooldown(player) > 0) {
            Msg.warn(player, "다시 확성기를 사용하려면 " + DateUtil.formatDate(getCooldown(player)) + "를 기다려야합니다.");
            return true;
        }
        new Speaker("megaphone", player.getName(), Msg.uncolor(Msg.color(ArgHelper.sumArg(args))));
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis() + 600000L);
        return true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldownMap.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSpeakerListen(SpeakerListenEvent e) {
        if(!e.getMainAction().equals("megaphone")) return;
        String msg = "&e[ &6확성기 &e] #ffff99<" + e.getIn().readUTF() + ">" + " " + e.getIn().readUTF();
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(StoryManager.getPlayingStory(p) != null) continue;
            Msg.send(p, " ");
            Msg.send(p, msg);
            Msg.send(p, " ");
        }
    }


    public long getCooldown(Player player) {
        return Math.max(0, ((cooldownMap.getOrDefault(player.getUniqueId(), System.currentTimeMillis()) - System.currentTimeMillis()) / 1000));
    }
}
