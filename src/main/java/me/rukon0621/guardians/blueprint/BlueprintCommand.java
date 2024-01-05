package me.rukon0621.guardians.blueprint;

import me.rukon0621.guardians.commands.AbstractCommand;
import me.rukon0621.guardians.data.ItemGrade;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlueprintCommand extends AbstractCommand {
    private final List<String> grades = new ArrayList<>();

    public BlueprintCommand() {
        super("blueprint", main.getPlugin());
        arguments.add("생성");
        arguments.add("설정");
        arguments.add("삭제");
        arguments.add("지급");
        arguments.add("모두해금");
        arguments.add("데이터");
        for(ItemGrade grade : ItemGrade.values()) {
            grades.add(grade.toString());
        }
    }

    @Override
    protected void usage(Player player, String usage) {
        if(usage.equals("생성")) {
            Msg.send(player, "&6/청사진 생성 <등급> <레벨> <거래불가 여부> <일회성 여부> <이름>");
        }
        else if(usage.equals("삭제")) {
            Msg.send(player, "&6/청사진 삭제 <이름>");
        }
        else if(usage.equals("설정")) {
            Msg.send(player, "&6/청사진 설정 <이름>");
        }
        else if(usage.equals("지급")) {
            Msg.send(player, "&6/청사진 지급 <이름>");
        }
        else if(usage.equals("데이터")) {
            Msg.send(player, "&6/청사진 데이터 <플레이어> <추가/삭제> <이름>");
        }
        else if(usage.equals("모두해금")) {
            Msg.send(player, "&6/청사진 모두해금");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7청사진 데이터 리로드중...");
            main.getPlugin().getBluePrintManager().reloadBlueprints();
            MsgUtil.cmdMsg(sender, "&a청사진 데이터 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) return false;

        if(args.length==0) {
            usages(player);
            return true;
        }
        if(args[0].equals("생성")) {
            if(args.length<6) {
                usage(player, args[0], true);
                return true;
            }
            try {
                int level = Integer.parseInt(args[2]);
                boolean untradable = Boolean.parseBoolean(args[3]);
                boolean consumable = Boolean.parseBoolean(args[4]);
                main.getPlugin().getBluePrintManager().createNewBlueprint(player, ArgHelper.sumArg(args, 5), ItemGrade.valueOf(args[1]), level, untradable, consumable);
            } catch (NumberFormatException e) {
                Msg.warn(player, "올바른 숫자를 입력해주세요.");
            } catch (IllegalArgumentException e) {
                Msg.warn(player, "존재하지 않는 등급입니다.");
            }
        }
        else if(args[0].equals("모두해금")) {
            BluePrintManager manager = main.getPlugin().getBluePrintManager();
            PlayerData pdc = new PlayerData(player);
            for(String name : manager.getAllBlueprintName()) {
                if(manager.getConsumableBlueprintData().containsKey(name)) pdc.getConsumableBlueprintsData().put(name, 64);
                else pdc.addBlueprint(name);
            }
            Msg.send(player, "모든 청사진을 다 해금하였습니다.");
        }
        else if(args[0].equals("설정")) {
            if(args.length<3) {
                usage(player, args[0], true);
            }
            main.getPlugin().getBluePrintManager().setBlueprint(player, ArgHelper.sumArg(args, 1));
        }
        else if(args[0].equals("삭제")) {
            if(args.length<3) {
                usage(player, args[0], true);
            }
            main.getPlugin().getBluePrintManager().deleteBlueprint(player, ArgHelper.sumArg(args, 1));
        }
        else if(args[0].equals("지급")) {
            if(args.length<3) {
                usage(player, args[0], true);
            }
            main.getPlugin().getBluePrintManager().giveBlueprint(player, ArgHelper.sumArg(args, 1));
        }
        else if(args[0].equals("데이터")) {
            if(args.length<4) {
                usage(player, args[0], true);
            }
            Player target = main.getPlugin().getServer().getPlayer(args[1]);
            if(target==null) {
                Msg.warn(player, "해당 플레이어는 존재하지 않습니다.");
                return true;
            }
            BluePrintManager manager = main.getPlugin().getBluePrintManager();
            PlayerData pdc = new PlayerData(target);
            String name = ArgHelper.sumArg(args, 3);
            if(args[2].equals("추가")) {
               if(!manager.getAllBlueprintName().contains(name)) {
                   Msg.warn(player, "이 청사진은 존재하지 않는 청사진입니다.");
                   return true;
               }

               if(manager.getConsumableBlueprintData().containsKey(name)) pdc.addConsumableBlueprint(name);
               else pdc.addBlueprint(name);
               Msg.send(player, "성공적으로 청사진 데이터를 추가했습니다.");
               return true;

            }
            else if (args[2].equals("삭제")) {
                if(!manager.getAllBlueprintName().contains(name)) {
                    Msg.warn(player, "이 청사진은 존재하지 않는 청사진입니다.");
                    return true;
                }
                if(manager.getConsumableBlueprintData().containsKey(name)) pdc.removeConsumableBlueprint(name);
                else pdc.removeBlueprint(name);
                Msg.send(player, "성공적으로 청사진 데이터를 삭제했습니다.");
                return true;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length==0) return new ArrayList<>();
        else if(args.length==1) return TabCompleteUtils.searchAtList(arguments, args[0]);
        else if(args[0].equals("데이터")) {
            if(args.length==2) return null;
            if(args.length==3) {
                List<String> list = new ArrayList<>();
                list.add("추가");
                list.add("삭제");
                return TabCompleteUtils.search(list, args[2]);
            }
            else if(args.length==4) {
                if(args[2].equals("추가")) return TabCompleteUtils.searchAtSet(main.getPlugin().getBluePrintManager().getAllBlueprintName(), args[3]);
                if(args[2].equals("삭제")) {
                    Player player = main.getPlugin().getServer().getPlayer(args[1]);
                    if(player==null) return new ArrayList<>();
                    PlayerData pdc = new PlayerData(player);
                    return TabCompleteUtils.search(pdc.getBlueprintsData(), args[3]);
                }
            }
        }
        else if(args[0].equals("생성")) {
            if(args.length==2) return TabCompleteUtils.searchAtList(grades, args[1]);
            else if(args.length==4) {
                List<String> ls = new ArrayList<>();
                ls.add("true");
                ls.add("false");
                return TabCompleteUtils.searchAtList(ls, args[3]);
            }
            else if(args.length==5) {
                List<String> ls = new ArrayList<>();
                ls.add("true");
                ls.add("false");
                return TabCompleteUtils.searchAtList(ls, args[4]);
            }
            else if(args.length>=6) return TabCompleteUtils.searchAtSet(main.getPlugin().getBluePrintManager().getAllBlueprintName(), args[4]);
        }
        else if((args[0].equals("삭제")||args[0].equals("설정")||args[0].equals("지급"))&&args.length==2) return TabCompleteUtils.searchAtSet(main.getPlugin().getBluePrintManager().getAllBlueprintName(), args[1]);
        return new ArrayList<>();
    }
}
