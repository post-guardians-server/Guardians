package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CloseAndKickCommand implements CommandExecutor {
    public static String[] arguments = {"basic"};

    public CloseAndKickCommand() {
        main.getPlugin().getCommand("closeKick").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for(Player player : main.getPlugin().getServer().getOnlinePlayers()) {
            player.closeInventory();
        }
        for(Player player : main.getPlugin().getServer().getOnlinePlayers()) {
            player.closeInventory();
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : main.getPlugin().getServer().getOnlinePlayers()) {
                    player.kickPlayer(Msg.color("&c서버가 재시작됩니다."));
                }
            }
        }.runTaskLater(main.getPlugin(), 5);
        return true;
    }
}
