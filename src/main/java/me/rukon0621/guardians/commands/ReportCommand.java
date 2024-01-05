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

import static me.rukon0621.guardians.main.mainChannel;
import static me.rukon0621.guardians.main.pfix;

public class ReportCommand implements CommandExecutor {

    public ReportCommand() {
        main.getPlugin().getCommand("report").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length < 1) {
            Msg.send(player, "&6/버그제보 <내용>");
            return true;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("report");
        out.writeUTF(player.getName());
        out.writeUTF(ArgHelper.sumArg(args));
        player.sendPluginMessage(main.getPlugin(), mainChannel, out.toByteArray());
        Msg.send(player, "&a소중한 의견 제보해주셔서 감사합니다!", pfix);
        return true;

        /*

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("broadcast");
        out.writeUTF(ArgHelper.sumArg(args));
        main.getPlugin().getServer().sendPluginMessage(main.getPlugin(), chatChannel, out.toByteArray());
         */
    }
}
