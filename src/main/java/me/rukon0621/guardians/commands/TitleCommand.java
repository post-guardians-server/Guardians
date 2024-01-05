package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.GUI.TitleWindow;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.TitleListener;
import me.rukon0621.guardians.main;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class TitleCommand extends AbstractCommand {
    public TitleCommand() {
        super("chattitle", main.getPlugin());
    }

    @Override
    protected void usage(Player player, String usage) {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length < 1) {
            Msg.send(player, "&6/칭호 <이름>[!!<기간(일)>]");
            Msg.send(player, "&7    - 해당 이름의 칭호 아이템을 받습니다.");
            Msg.send(player, "&7    /칭호 가이드:7일");
            return true;
        }

        String fullTitle = StringEscapeUtils.unescapeJava(ArgHelper.sumArg(args)).replaceFirst("!!", TitleWindow.timeSpliter);

        if(TitleWindow.isPeriodic(fullTitle)) {
            double days = Double.parseDouble(fullTitle.split(TitleWindow.timeSpliter)[1].trim());
            player.getInventory().addItem(TitleListener.getTitleItem(fullTitle.split(TitleWindow.timeSpliter)[0].trim(), days));
        }
        else player.getInventory().addItem(TitleListener.getTitleItem(fullTitle));
        Msg.send(player, "아이템을 지급 받았습니다.", pfix);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
