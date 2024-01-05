package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.rinstance.RukonInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RespawnCommand implements CommandExecutor {

    public RespawnCommand() {
        main.getPlugin().getCommand("respawn").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;

        PlayerData pdc = new PlayerData(player);
        if(StoryManager.getPlayingStory(player)!=null || RukonInstance.inst().getInstanceManager().isPlayerInInstance(player)||
                LogInOutListener.getPreviousStories().containsKey(player) || pdc.getStoryCode() < 1) {
            Msg.warn(player, "지금은 이 명령어를 사용할 수 없습니다.");
            return true;
        }

        player.setHealth(0);
        Msg.send(player, "&4깩!");
        return true;
    }
}
