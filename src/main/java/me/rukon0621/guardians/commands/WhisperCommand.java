package me.rukon0621.guardians.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.DateUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.listeners.ChatEventListener.getRemainMuteSecond;
import static me.rukon0621.guardians.main.chatChannel;
import static me.rukon0621.guardians.main.pfix;

public class WhisperCommand extends AbstractCommand {
    public WhisperCommand() {
        super("wp", main.getPlugin());
    }

    @Override
    protected void usage(Player player, String usage) {
        Msg.send(player, "&6/귓 <player>");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player player)) return false;
        if(args.length < 1) {
            usage(player, null);
            return true;
        }
        if(!LogInOutListener.getProxyPlayerNames().contains(args[0])) {
            Msg.send(player, "&c해당 플레이어는 온라인이 아니거나 존재하지 않습니다.", pfix);
            return true;
        }
        if(args.length < 2) {
            usage(player, null);
            return true;
        }
        if(args[0].equals(player.getName())) {
            Msg.send(player, "&c자신에게 메세지를 보낼 수 없습니다.", pfix);
            return true;
        }

        if(getRemainMuteSecond(player) > -1) {
            Msg.warn(player, String.format("채팅 금지 시간이 %s 남아있습니다. 이 동안은 전체, 채널 채팅을 이용하실 수 없습니다.", DateUtil.formatDate(getRemainMuteSecond(player))));
            return true;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        String msg = ArgHelper.sumArg(args, 1);
        out.writeUTF("whisperSend");
        out.writeUTF(player.getName());
        out.writeUTF(args[0]);
        out.writeUTF(msg);
        player.sendPluginMessage(main.getPlugin(), chatChannel, out.toByteArray());
        Msg.send(player, String.format("&7[ To.%s ] %s", args[0], msg));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length==1) return TabCompleteUtils.search(LogInOutListener.getProxyPlayerNames(), args[0]);
        return new ArrayList<>();
    }
}
