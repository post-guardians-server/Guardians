package me.rukon0621.guardians.dropItem;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class DropCommand implements CommandExecutor {
    public static String[] arguments = {"리로드", "생성", "삭제", "목록", "얻기", "배율", "속성배율"};

    public DropCommand() {
        main.getPlugin().getCommand("dropdata").setExecutor(this);
        main.getPlugin().getCommand("dropdata").setTabCompleter(new DropCommandTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7드롭데이터 리로드중...");
            DropManager.reloadAllDropData();
            MsgUtil.cmdMsg(sender, "&a드롭데이터 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) {
            return false;
        }

        if(args.length==0) {
            usages(player);
            return true;
        }
        else if(args[0].equals("생성")) {
            if(args.length<3) {
                usage(player, "생성", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 2);
            String path = args[1];
            DropManager.createNewDropData(player, name, path);
        }
        else if(args[0].equals("삭제")) {
            if(args.length<2) {
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            DropManager.deleteDropData(player, name);
        }
        else if(args[0].equals("배율")) {
            if(args.length<2) {
                usage(player, "배율", true);
                return true;
            }
            double multiply = Double.parseDouble(args[1]);
            Drop.setBurning(multiply);
            for(Player p : main.getPlugin().getServer().getOnlinePlayers()) {
                if(!p.isOp()) continue;
                Msg.send(p, "&e운영자에 의해 &c드롭 배율&e이 변경되었습니다.", pfix);
            }
        }
        else if(args[0].equals("속성배율")) {
            if(args.length<2) {
                usage(player, args[0], true);
                return true;
            }
            double multiply = Double.parseDouble(args[1]);
            Drop.setAttrBurning(multiply);
            for(Player p : main.getPlugin().getServer().getOnlinePlayers()) {
                if(!p.isOp()) continue;
                Msg.send(p, "&e운영자에 의해 &b속성 드롭 배율&e이 변경되었습니다.", pfix);
            }
        }
        else if(args[0].equals("얻기")) {
            if(args.length<4) {
                usage(player, args[0], true);
                return true;
            }

            int level, repeat;
            try {
                level = Integer.parseInt(args[1]);
                repeat = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                Msg.warn(player, "제대로된 정수를 입력해주세요.");
                return true;
            }
            String name = ArgHelper.sumArg(args, 3);


            if(!DropManager.hasDrop(name)) {
                Msg.warn(player, "&c해당 드롭은 존재하지 않습니다.", pfix);
                return true;
            }
            List<ItemStack> items = new ArrayList<>();
            for(int i = 0;i < repeat;i++) {
                items.addAll(DropManager.getDropList(player, name, level, 1));
            }
            MailBoxManager.giveAllOrMailAll(player, items);
            Msg.send(player, "드롭을 지급 받았습니다.", pfix);
        }
        else if(args[0].equals("목록")) {
            DropManager.showDropDataList(player);
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
            Msg.send(player, "&6/드롭 리로드");
            Msg.send(player, "&7   모든 드롭 데이터를 새로고침합니다.");
        }
        else if(arg.equals("생성")) {
            Msg.send(player, "&6/드롭 생성 <경로> <이름>");
            Msg.send(player, "&7   새로운 드롭 정보를 생성합니다.");
            Msg.send(player, "&7   경로 작성 예시) mob/boar.yml");
        }
        else if(arg.equals("삭제")) {
            Msg.send(player, "&6/드롭 삭제 <이름>");
            Msg.send(player, "&7   드롭 정보를 삭제합니다.");
        }
        else if(arg.equals("목록")) {
            Msg.send(player, "&6/드롭 목록");
            Msg.send(player, "&7   서버에 존재하는 드롭 정보를 확인합니다.");
        }
        else if(arg.equals("얻기")) {
            Msg.send(player, "&6/드롭 얻기 <레벨> <반복> <이름>");
            Msg.send(player, "&7   드롭을 얻습니다.");
        }
        else if(arg.equals("배율")) {
            Msg.send(player, "&6/드롭 배율 <배율>");
            Msg.send(player, "&7   드롭의 배율을 설정합니다.");
        }
        else if(arg.equals("속성배율")) {
            Msg.send(player, "&6/드롭 속성배율 <배율>");
            Msg.send(player, "&7   드롭의 속성배율을 설정합니다.");
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
