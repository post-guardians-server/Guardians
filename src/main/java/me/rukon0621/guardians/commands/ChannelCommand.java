package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.GUI.ChannelWindow;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChannelCommand implements CommandExecutor {

    public ChannelCommand() {
        main.getPlugin().getCommand("channel").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        new ChannelWindow(player);
        return true;
    }
}
