package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.events.WorldPeriodicEvent;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.print.DocFlavor;

public class DailyEvent implements CommandExecutor {
    public static String[] arguments = {"test"};

    public DailyEvent() {
        main.getPlugin().getCommand("dailyEvent").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length > 0) {
            Player player = Bukkit.getPlayerExact(args[0]);
            if(player != null) {
                WorldPeriodicEvent.dailyEvent(player);
                return true;
            }
        }

        for(Player p : main.getPlugin().getServer().getOnlinePlayers()) {
            WorldPeriodicEvent.dailyEvent(p);
        }
        return true;
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equalsIgnoreCase("test")) {
            Msg.send(player, "&6/test");
            Msg.send(player, "&7   test");
        }
        Msg.send(player, " ");
        if (forone) Msg.send(player, "&e└────────────────────────┘");
    }

    private void usages(Player player) {
        Msg.send(player, "&e┌────────────────────────┐");
        Msg.send(player, " ");
        for(String s : arguments) {
            usage(player, s, false);
        }
        Msg.send(player, "&e└────────────────────────┘");
    }
}
