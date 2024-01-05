package me.rukon0621.guardians.craft;

import me.rukon0621.guardians.craft.craft.CraftManager;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.pfix;

public class CrafttableCommand implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"생성", "삭제", "목록", "즉시완료", "리로드", "열기"};

    public CrafttableCommand() {
        plugin.getCommand("crafttable").setExecutor(this);
        plugin.getCommand("crafttable").setTabCompleter(new CrafttableCommandTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7제작대 리로드중...");
            CraftManager.reloadCraftData();
            MsgUtil.cmdMsg(sender, "&a제작대 리로드 완료!");
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
            if(CraftManager.createNewCraftTable(name)) {
                Msg.send(player, "성공적으로 제작대를 생성했습니다.", pfix);
                CraftManager.reloadCraftData();
                return true;
            } else {
                Msg.send(player, "&c해당 제작대는 이미 존재하는 제작대입니다.", pfix);
                return true;
            }
        }
        else if(args[0].equals("삭제")) {
            if(args.length<2) {
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(CraftManager.deleteCraftTable(name)) {
                Msg.send(player, "&6성공적으로 제작대를 삭제했습니다.", pfix);
                CraftManager.reloadCraftData();
                return true;
            } else {
                Msg.send(player, "&c해당 제작대는 존재하지 않는 제작대입니다.", pfix);
                return true;
            }
        }
        else if(args[0].equals("목록")) {
            Msg.send(player, "서버에 존재하는 제작대 목록입니다.", pfix);
            for(String key : CraftManager.craftTableData.keySet()) {
                Msg.send(player, key);
            }
            return true;
        }
        else if(args[0].equals("즉시완료")) {
            if(args.length<2) {
                usage(player, "즉시완료", true);
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[1]);
            if(target==null) {
                Msg.warn(player, "제대로된 플레이어의 이름을 입력해주세요.");
                return true;
            }
            CraftManager.completeAllWaitingItems(target);
            Msg.send(player, "해당 플레이어의 모든 제작 대기열을 완료시켰습니다.", pfix);
            return true;
        }
        else if(args[0].equals("열기")) {
            if(args.length < 2) {
                usage(player, "열기", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!CraftManager.craftTableData.containsKey(name)) {
                Msg.send(player, "&c존재하는 제작대가 아닙니다.", pfix);
                return true;
            }
            CraftManager.openCraftTable(player, name);
            Msg.send(player, "&f제작대를 열었습니다.", pfix);
            return true;
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
        if(arg.equals("리로드")) {
            Msg.send(player, "&6/제작대 리로드");
            Msg.send(player, "&7    서버에 존재하는 모든 제작대를 리로드합니다.");
        }
        else if(arg.equals("생성")) {
            Msg.send(player, "&6/제작대 생성 <이름>");
            Msg.send(player, "&7    서버에 새로운 제작대를 생성합니다.");
        }
        else if(arg.equals("삭제")) {
            Msg.send(player, "&6/제작대 삭제 <제작대 이름>");
            Msg.send(player, "&7    기존에 존재하는 제작대를 삭제합니다.");
        }
        else if(arg.equals("목록")) {
            Msg.send(player, "&6/제작대 목록");
            Msg.send(player, "&7    서버에 존재하는 제작대와 그 경로를 살펴봅니다.");
        }
        else if(arg.equals("열기")) {
            Msg.send(player, "&6/제작대 열기 <제작대 이름>");
            Msg.send(player, "&7    해당 제작대를 엽니다.");
        }
        else if(arg.equals("즉시완료")) {
            Msg.send(player, "&6/제작대 즉시완료 <플레이어>");
            Msg.send(player, "&7    제작 대기열에 있는 모든 아이템을 즉시 완료시킵니다.");
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
