package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.pfix;

public class LevelCommand implements CommandExecutor {
    private final main plugin;
    public static String[] arguments = {"설정", "변경", "데이터"};

    public LevelCommand(main plugin) {
        plugin.getCommand("level").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;

        if(args.length==0) {
            usages(player);
            return true;
        }

        if(args[0].equals("데이터")) {

            if(args.length<2) {
                usage(player, "데이터", true);
                return true;
            }

            int level;
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c올바른 숫자를 적어주세요.", pfix);
                return true;
            }
            if(level >= 250) {
                Msg.warn(player, "최대치는 250입니다.");
            }

            for(int i = 1; i <= level; i++) {
                Msg.send(player, String.format("%d : %d &7(장비 경험치: %d)", i, LevelData.expAtLevel.get(i), ItemData.getMaxExpAtLevel(i)));
            }

            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if(target==null) {
            Msg.send(player, "&c플레이어의 이름을 제대로 입력해주세요.", pfix);
            return true;
        }
        if(args.length==1) {
            usages(player);
            return true;
        }
        if(args[1].equals("설정")) {
            if(args.length<3) {
                usage(player, "설정", true);
                return true;
            }
            int value;
            //숫자 입력 확인
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c값을 제대로 입력해주세요.", pfix);
                return true;
            }
            if(value<=0) {
                Msg.send(player, "&c레벨은 반드시 1 이상이 되어야합니다.", pfix);
                return true;
            }
            if(value> LevelData.maxLevel) {
                Msg.send(player, String.format("&e최대레벨 %d보다 높은 레벨을 지정할 수 없습니다.", LevelData.maxLevel), pfix);
                return true;
            }
            PlayerData pdc = new PlayerData(target);
            pdc.setLevel(value);
            LevelData.reloadIndicator(target);
            Msg.send(player, "성공적으로 레벨을 설정했습니다.", pfix);
            return true;
        }
        else if (args[1].equals("변경")) {
            if(args.length<3) {
                usage(player, "변경", true);
                return true;
            }
            //숫자 입력 확인
            int value;
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c값을 제대로 입력해주세요.");
                return true;
            }
            PlayerData pdc = new PlayerData(target);
            int nowLevel = pdc.getLevel();
            if(nowLevel+value>=LevelData.maxLevel ||nowLevel+value<1) {
                Msg.send(player, "&c해당 양의 레벨을 주면 레벨의 범위를 벗어납니다.", pfix);
                return true;
            }
            pdc.setExp(0);
            pdc.setLevel(value+nowLevel);
            LevelData.reloadIndicator(target);
            Msg.send(player, "성공적으로 레벨을 변경했습니다.", pfix);
            return true;
        } else {
            usages(player);
            return true;
        }
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equalsIgnoreCase("설정")) {
            Msg.send(player, "&6/레벨관리 <플레이어> 설정 <값>");
            Msg.send(player, "&7   해당 플레이어의 레벨을 설정합니다.");
        }
        else if(arg.equalsIgnoreCase("변경")) {
            Msg.send(player, "&6/레벨관리 <플레이어> 변경 <값>");
            Msg.send(player, "&7   해당 플레이어의 레벨을 증감합니다.");
        }
        else if(arg.equalsIgnoreCase("데이터")) {
            Msg.send(player, "&6/레벨관리 데이터 <레벨>");
            Msg.send(player, "&7   레벨당 경험치를 확인합니다");
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
