package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand implements CommandExecutor {

    public HealCommand() {
        main.getPlugin().getCommand("heal").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        player.setHealth(player.getMaxHealth());
        BarManager.reloadBar(player);
        return true;
    }
}
