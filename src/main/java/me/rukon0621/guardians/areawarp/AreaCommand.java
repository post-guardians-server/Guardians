package me.rukon0621.guardians.areawarp;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AreaCommand implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"생성", "삭제", "위치수정", "귀환설정", "아이콘수정", "이동", "목록","리로드"};

    public AreaCommand() {
        plugin.getCommand("area").setExecutor(this);
        plugin.getCommand("area").setTabCompleter(new AreaCommandTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)  {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7에리어 데이터 리로드중...");
            AreaManger.reloadAreaData();
            MsgUtil.cmdMsg(sender, "&a에리어 데이터 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }
        if(args[0].startsWith("생")) {
            if(args.length < 2 ){
                usage(player, "생성", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            AreaManger.createNewArea(player, name);
        }
        else if(args[0].startsWith("삭")) {
            if(args.length < 2 ){
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            AreaManger.deleteArea(player, name);
        }
        else if(args[0].startsWith("위")) {
            if(args.length < 2 ){
                usage(player, "위치수정", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            AreaManger.changeAreaLocation(player, name);
        }
        else if(args[0].startsWith("귀")) {
            if(args.length < 2 ){
                usage(player, "귀환설정", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            AreaManger.changeReturnLocation(player, name);
        }
        else if(args[0].startsWith("아")) {
            if(args.length < 2 ){
                usage(player, "아이콘수정", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            AreaManger.changeAreaIcon(player, name);
        }
        else if(args[0].startsWith("이")) {
            if(args.length < 2 ){
                usage(player, "이동", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            AreaManger.teleportToArea(player, name, true);
        }
        else if(args[0].startsWith("목")) {
            AreaManger.showListOfArea(player);
        }
        return true;
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equals("생성")) {
            Msg.send(player, "&6/에리어 생성 <이름>");
            Msg.send(player, "&7   새로운 지역을 생성합니다.");
        }
        else if(arg.equals("삭제")) {
            Msg.send(player, "&6/에리어 삭제 <이름>");
            Msg.send(player, "&7   지역을 삭제합니다.");
        }
        else if(arg.equals("위치수정")) {
            Msg.send(player, "&6/에리어 위치[수정] <이름>");
            Msg.send(player, "&7   지역의 위치를 수정합니다.");
        }
        else if(arg.equals("귀환설정")) {
            Msg.send(player, "&6/에리어 귀환설정 <이름>");
            Msg.send(player, "&7   지역 귀환 위치를 수정합니다.");
        }
        else if(arg.equals("아이콘수정")) {
            Msg.send(player, "&6/에리어 아이콘[수정] <이름>");
            Msg.send(player, "&7   지역의 아이콘을 수정합니다.");
        }
        else if(arg.equals("이동")) {
            Msg.send(player, "&6/에리어 이동 <이름>");
            Msg.send(player, "&7   지역으로 이동합니다.");
        }
        else if(arg.equals("목록")) {
            Msg.send(player, "&6/에리어 목록");
            Msg.send(player, "&7   지역의 목록을 확인합니다.");
        }
        else if(arg.equals("리로드")) {
            Msg.send(player, "&6/에리어 리로드");
            Msg.send(player, "&7   지역을 리로드 합니다.");
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
