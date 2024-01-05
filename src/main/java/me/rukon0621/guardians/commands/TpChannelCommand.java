package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TpChannelCommand extends AbstractCommand {

    public TpChannelCommand() {
        super("chc", main.getPlugin());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;

        if(args.length!=1) {
            Msg.send(player, "&6/chc <player>");
            return true;
        }


        return true;
    }

    @Override
    protected void usage(Player player, String usage) {

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
