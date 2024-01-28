package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.GUI.item.enhance.StoneRemoveWindow;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class test extends AbstractCommand {
    public test() {
        super("test", main.getPlugin());
    }

    @Override
    protected void usage(Player player, String usage) {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        new StoneRemoveWindow(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
