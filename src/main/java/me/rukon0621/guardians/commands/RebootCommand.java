package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static me.rukon0621.guardians.main.getPlugin;
import static me.rukon0621.guardians.main.pfix;

public class RebootCommand implements CommandExecutor {
    public static String[] arguments = {"basic"};

    public RebootCommand() {
        main.getPlugin().getCommand("stop").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for(Player player : main.getPlugin().getServer().getOnlinePlayers()) {
            player.closeInventory();
        }

        if(!main.isDevServer()) {
            if(sender instanceof Player player) Msg.send(player, "서버를 정지할 수 없습니다. 으아 안돼");
            else System.out.println("서버를 정지할 수 없습니다. 으아 안돼");
            return true;
        }

        String msg;
        if(args.length==0) msg = "서버가 종료되었습니다.";
        else msg = ArgHelper.sumArg(args, 0);
        shutdown(msg);
        return true;
    }

    private void shutdown(String msg) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : main.getPlugin().getServer().getOnlinePlayers()) {
                    player.kickPlayer(msg);
                }
            }
        }.runTaskLater(getPlugin(), 5);
        new BukkitRunnable() {
            @Override
            public void run() {
                main.getPlugin().getServer().shutdown();
            }
        }.runTaskLater(getPlugin(), 10);

    }
}
