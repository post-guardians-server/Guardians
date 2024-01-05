package me.rukon0621.guardians.region;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.pfix;

public class RegionCommands implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"생성", "삭제", "수정", "목록", "리로드"};

    public RegionCommands() {
        plugin.getCommand("region").setExecutor(this);
        plugin.getCommand("region").setTabCompleter(new RegionCommandsTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7지역 리로드중...");
            RegionManager.reloadRegionData();
            MsgUtil.cmdMsg(sender, "&a지역 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }
        if(args[0].equals("생성")) {
            if(args.length<2) {
                usage(player, "생성", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            RegionManager.createNewRegion(player, name);
        }
        else if (args[0].equals("삭제")) {
            if(args.length<2) {
                usage(player,"삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            RegionManager.deleteRegion(player, name);
        }
        else if (args[0].equals("수정")) {
            if(args.length<2) {
                usage(player,"수정", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            RegionManager.redefineRegion(player, name);
        }
        else if (args[0].equals("목록")) {
            if(args.length>1) {
                String name = ArgHelper.sumArg(args, 1);
                for(String story : RegionManager.getRegions().keySet()) {
                    if(story.startsWith(name)) {
                        Msg.send(player, story);
                    }
                }
                return true;
            }
            Msg.send(player, "&6서버에 존재하는 지역의 목록입니다.", pfix);
            for(String name : RegionManager.getRegions().keySet()) {
                Msg.send(player, name);
            }
        }
        else {
            usages(player);
        }

        return true;
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equals("생성")) {
            Msg.send(player, "&6/지역 생성 <이름>");
            Msg.send(player, "&7   특정 구역을 새롭게 생성합니다.");
        }
        else if (arg.equals("삭제")) {
            Msg.send(player, "&6/지역 삭제 <이름>");
            Msg.send(player, "&7   해당 지역을 영구적으로 삭제합니다.");
        }
        else if (arg.equals("수정")) {
            Msg.send(player, "&6/지역 수정 <이름>");
            Msg.send(player, "&7   해당 지역의 포지션을 수정합니다.");
        }
        else if (arg.equals("목록")) {
            Msg.send(player, "&6/지역 목록 [<검색필터>]");
            Msg.send(player, "&7   서버에 존재하는 지역 목록을 확인합니다.");
            Msg.send(player, "&7   필터에 내용을 입력하면 해당 문자열로 시작하는 스토리만 검색합니다.");
        }
        else if (arg.equals("리로드")) {
            Msg.send(player, "&6/지역 [이벤트]리로드");
            Msg.send(player, "&7   서버에 존재하는 지역 이벤트를 새로 고침합니다.");
        }
        Msg.send(player, " ");
        if (forone) Msg.send(player, "&e└────────────────────────┘");
    }

    private void usages(Player player) {
        Msg.send(player, "&e┌────────────────────────┐");
        Msg.send(player, " ");
        for(String s : arguments) {
            usage(player, s, false);
        }
        Msg.send(player, "&e└────────────────────────┘");
    }
}
