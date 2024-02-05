package me.rukon0621.guardians.fishing;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FishingCommand implements CommandExecutor {

    public FishingCommand() {
        main.getPlugin().getCommand("reloadFishing").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(args.length > 0) {
            if(commandSender instanceof Player player) {
                for(int i = 0; i < Integer.parseInt(args[0]); i++) {
                    player.getInventory().addItem(main.getPlugin().getFishingManager().getResult(player, new ItemData(player.getInventory().getItemInMainHand())).getItemStack());
                }
            }
            return true;
        }

        main.getPlugin().getFishingManager().reloadFishingData();
        return true;
    }
}
