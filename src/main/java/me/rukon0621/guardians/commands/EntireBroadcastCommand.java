package me.rukon0621.guardians.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.chatChannel;

public class EntireBroadcastCommand implements CommandExecutor {

    public EntireBroadcastCommand() {
        main.getPlugin().getCommand("broadcastall").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length < 1) {
            if(sender instanceof Player player) {
                Msg.send(player, "&6/전체공지 <내용>");
                return true;
            }
            return false;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("broadcast");
        out.writeUTF(ArgHelper.sumArg(args));
        if(sender instanceof Player player) {
            player.sendPluginMessage(main.getPlugin(), chatChannel, out.toByteArray());
        }
        else {
            main.getPlugin().getServer().sendPluginMessage(main.getPlugin(), chatChannel, out.toByteArray());
        }

        return true;
    }

    public static void entireBroadcast(Player player, String msg, boolean forceShowingDuringStory) {
        if(forceShowingDuringStory) msg = "ignoreStory" + msg;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("broadcast");
        out.writeUTF(msg);
        player.sendPluginMessage(main.getPlugin(), chatChannel, out.toByteArray());
    }

}
