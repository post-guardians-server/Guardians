package me.rukon0621.guardians.barcast;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.main;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BarBroadcastCommand implements CommandExecutor, TabCompleter {
    private final main plugin = main.getPlugin();
    private final String[] usages = new String[]{"생성", "삭제"};

    public BarBroadcastCommand() {
        plugin.getCommand("barcast").setExecutor(this);
        plugin.getCommand("barcast").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            if(args[0].equals("생성")) {
                String color = args[1];
                String style = args[2];
                String title = ArgHelper.sumArg(args, 3);
                BroadcastBar.createNewBroadcast(null, title, color, style);
            }
            else if(args[0].equals("삭제")) {
                String title = ArgHelper.sumArg(args, 1);
                BroadcastBar.removeBroadcast(null, title);
            }
            return true;
        }
        if(args.length < 1) {
            usages(player);
            return true;
        }
        if(args[0].equals("생성")) {
            if(args.length < 4) {
                usage(player, "생성", true);
                return true;
            }
            String color = args[1];
            String style = args[2];
            String title = ArgHelper.sumArg(args, 3);
            BroadcastBar.createNewBroadcast(player, title, color, style);
        }
        else if(args[0].equals("삭제")) {
            if(args.length < 2) {
                usage(player, "삭제", true);
                return true;
            }
            String title = ArgHelper.sumArg(args, 1);
            BroadcastBar.removeBroadcast(player, title);
        }
        return true;
    }

    private void usages(Player player) {
        Msg.send(player, "&7=================================================");
        Msg.send(player, " ");
        for(String us : usages) {
            usage(player, us, false);
            Msg.send(player, " ");
        }
        Msg.send(player, "&7=================================================");
    }

    private void usage(Player player, String usage, boolean forOne) {
        if(forOne) {
            Msg.send(player, "&7=================================================");
            Msg.send(player, " ");
        }
        if(usage.equals("생성")) {
            Msg.send(player, "&6/보스바공지 생성 <색> <모양> <내용>");
        }
        else if(usage.equals("삭제")) {
            Msg.send(player, "&6/보스바공지 삭제 <내용>");
        }
        if(forOne) {
            Msg.send(player, " ");
            Msg.send(player, "&7=================================================");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if(args.length==1) return TabCompleteUtils.searchAtList(List.of(usages), args[0]);
        else if (args[0].equals("생성")) {
            if(args.length==2) {
                List<String> list = new ArrayList<>();
                for(BarColor color : BarColor.values()) {
                    list.add(color.toString());
                }
                return TabCompleteUtils.searchAtList(list, args[1]);
            }
            else if(args.length==3) {
                List<String> list = new ArrayList<>();
                for(BarStyle style : BarStyle.values()) {
                    list.add(style.toString());
                }
                return TabCompleteUtils.searchAtList(list, args[2]);
            }
        }
        else if (args[0].equals("삭제")) {
            if(args.length==2) {
                return TabCompleteUtils.searchAtList(BroadcastBar.getNames(), args[1]);
            }
        }
        return new ArrayList<>();
    }
}
