package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Broadcaster;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BroadcastCommand implements CommandExecutor {

    public BroadcastCommand() {
        main.getPlugin().getCommand("broadcast").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) {
            if(sender instanceof Player player) {
                Msg.send(player, "&6/공지 <내용>");
                return true;
            }
            return false;
        }
        Broadcaster.broadcast(ArgHelper.sumArg(args, 0));
        return true;
    }
}
