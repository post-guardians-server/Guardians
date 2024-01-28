package me.rukon0621.guardians.vote;

import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VoteWindowCommand implements CommandExecutor {

    public VoteWindowCommand() {
        main.getPlugin().getCommand("voteCheck").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player player)) return false;
        new VoteWindow(player, false);
        return true;
    }
}
