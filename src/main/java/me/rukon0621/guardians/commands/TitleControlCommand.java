package me.rukon0621.guardians.commands;

import com.google.common.escape.Escaper;
import me.rukon0621.guardians.GUI.TitleWindow;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.main;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class TitleControlCommand extends AbstractCommand {
    public TitleControlCommand() {
        super("controlTitle", main.getPlugin());
        arguments.add("추가");
        arguments.add("삭제");
    }

    @Override
    protected void usage(Player player, String usage) {
        if(usage.equals("삭제")) {
            Msg.send(player, "&6/칭호관리 <player> 삭제 <칭호 이름>");
            Msg.send(player, "&7- <칭호이름>으로 시작되는 칭호 중 기간이 가장 짧게 남은 칭호를 삭제합니다. (색 무시)");
            Msg.send(player, "&7- 일반 칭호를 가지고 있을시 일반 칭호를 우선적으로 삭제합니다.");
        }
        else if(usage.equals("추가")) {
            Msg.send(player, "&6/칭호관리 <player> 추가 <칭호 이름[" +TitleWindow.timeSpliterAbb + "<days>]>");
            Msg.send(player, "&7- 일반 칭호 추가: /칭호관리 <player> 추가 가이드");
            Msg.send(player, "&7- 기간 칭호 추가: /칭호관리 <player> 추가 가이드!!7");
            Msg.send(player, "&7   - 7일 동안 지속되는 칭호입니다.");
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player player)) return false;

        if(args.length == 0) {
            usages(player);
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if(target == null) {
            usages(player);
            return true;
        }

        try {
            if(args[1].equals("추가")) {
                if(args.length < 3) {
                    usage(player, args[1], true);
                    return true;
                }
                PlayerData pdc = new PlayerData(target);
                if(pdc.getTitles().size() == 54) {
                    Msg.warn(player, "해당 플레이어는 이미 54개의 칭호를 가지고 있어 더 많은 칭호를 소유할 수 없습니다.");
                    return true;
                }

                String fullTitle = StringEscapeUtils.unescapeJava(ArgHelper.sumArg(args, 2)).replaceFirst(TitleWindow.timeSpliterAbb, TitleWindow.timeSpliter);
                if(TitleWindow.isPeriodic(fullTitle)) {
                    TitleWindow.addTitle(player, target, TitleWindow.getPureTitle(fullTitle), Double.parseDouble(fullTitle.split(TitleWindow.timeSpliter)[1]));
                }
                else {
                    TitleWindow.addTitle(player, target, TitleWindow.getPureTitle(fullTitle), -1);
                }
            }
            else if(args[1].equals("삭제")) {
                if(args.length < 3) {
                    usage(player, args[1], true);
                    return true;
                }
                TitleWindow.removeTitle(player, target, ArgHelper.sumArg(args, 2));
            }
            else usages(player);
        } catch (Exception e) {
            e.printStackTrace();
            usages(player);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length == 1) return null;
        else if(args.length == 2) return TabCompleteUtils.search(arguments,  args[1]);

        return new ArrayList<>();
    }
}
