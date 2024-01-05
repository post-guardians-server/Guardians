package me.rukon0621.guardians.commands;

import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.DateUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.listeners.ChatEventListener;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class MuteCommand extends AbstractCommand {
    public MuteCommand() {
        super("mute", main.getPlugin());
        arguments.add("해제");
        arguments.add("추가");
    }

    @Override
    protected void usage(Player player, String usage) {
        if (usage.equals("해제")) {
            Msg.send(player, "&6/뮤트 해제 <player> [<사유>]");
        }
        else if (usage.equals("추가")) {
            Msg.send(player, "&6/뮤트 추가 <player> <기간(단위:시간, 소수점 사용 가능)> [<사유>]");
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length == 0) {
            usages(player);
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if(target == null) {
            Msg.warn(player, "타겟의 이름을 적어주세요.");
            return true;
        }
        PlayerData pdc = new PlayerData(target);
        if(args[0].equals("해제")) {
            if(pdc.getMuteMillis() < System.currentTimeMillis()) {
                Msg.warn(player, target.getName() + "님은 채팅금지를 받지 않은 유저입니다.");
                return true;
            }
            String reason = String.format("%s의 뮤트를 해제함. 사유: ", target.getName());
            if(args.length > 2) {
                reason += ArgHelper.sumArg(args, 2);
            }
            pdc.setMuteMillis(0);
            Msg.send(player, "플레이어의 뮤트를 해제했습니다.", pfix);
            LogManager.log(player, "mute", reason);
        }
        else if(args[0].equals("추가")) {
            if(args.length < 3) {
                usage(player, args[0], true);
                return true;
            }
            double value;
            try {
                value = Double.parseDouble(args[2]);
                if(value <= 0 || value > 1000) {
                    Msg.warn(player, "최소 0시간 초과 1000시간 이하의 뮤트 시간을 적용할 수 있습니다");
                    return true;
                }
            } catch (NumberFormatException e) {
                Msg.warn(player, "제대로된 시간을 입력하십시오.");
                return true;
            }
            long millis = (long) (value * 1000L * 3600L);
            String reason = String.format("%s에게 %s 만큼의 뮤트를 지급함. 사유: ", target.getName(), DateUtil.formatDate(millis / 1000L));
            if(args.length > 3) {
                reason += ArgHelper.sumArg(args, 3);
            }
            pdc.setMuteMillis(System.currentTimeMillis() + millis);
            Msg.send(player, "플레이어의 채팅을 금지시켰습니다. 현재 " + target.getName() + "님의 채팅 금지 시간은 " + DateUtil.formatDate(ChatEventListener.getRemainMuteSecond(target)) + " 입니다", pfix);
            LogManager.log(player, "mute", reason);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length == 1) {
            return TabCompleteUtils.search(arguments, args[0]);
        }if(args.length == 2) {
            return null;
        }
        return new ArrayList<>();
    }
}
