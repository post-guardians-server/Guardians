package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.helper.Msg;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {
    protected List<String> arguments = new ArrayList<>();

    public AbstractCommand(String command, JavaPlugin plugin) {
        plugin.getCommand(command).setExecutor(this);
        plugin.getCommand(command).setTabCompleter(this);
    }

    protected void usages(Player player) {
        Msg.send(player, "&7┌──────────────────────────┐");
        Msg.send(player, " ");
        for(String arg : arguments) {
            usage(player, arg, false);
        }
        Msg.send(player, "&7└──────────────────────────┘");
    }
    protected void usage(Player player, String usage, boolean forOne) {
        if(forOne) {
            Msg.send(player, "&7┌──────────────────────────┐");
            Msg.send(player, " ");
        }
        usage(player, usage);
        Msg.send(player, " ");
        if(forOne) Msg.send(player, "&7└──────────────────────────┘");
    }
    protected abstract void usage(Player player, String usage);
}
