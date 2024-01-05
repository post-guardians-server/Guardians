package me.rukon0621.guardians.party;

import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyAcceptCommand implements CommandExecutor {
    private static final main plugin = main.getPlugin();

    public PartyAcceptCommand() {
        plugin.getCommand("accept").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        PartyManager.acceptParty(player);
        return true;
    }
}
