package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.afk.AfkConfirmWindow;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.DamagingListener;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.rinstance.RukonInstance;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AfkCommand implements CommandExecutor {

    public AfkCommand() {
        main.getPlugin().getCommand("afk").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player player)) return false;
        if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(player) || DamagingListener.getRemainCombatTime(player) != -1 || StoryManager.getPlayingStory(player) != null) {
            Msg.warn(player, "지금은 이 명령어를 이용할 수 없습니다.");
            return true;
        }
        new AfkConfirmWindow(player);
        return true;
    }
}
