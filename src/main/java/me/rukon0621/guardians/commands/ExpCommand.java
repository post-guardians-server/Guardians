package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.pfix;

public class ExpCommand implements CommandExecutor {
    private final main plugin;
    public static String[] arguments = {"설정", "변경", "경험치책", "장비경험치책"};

    public ExpCommand(main plugin) {
        plugin.getCommand("exp").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }
        if (args[0].endsWith("책")) {
            if(args.length<2) {
                usage(player, "경험치책", true);
                return true;
            }
            int exp;
            try {
                exp = Integer.parseInt(args[1]);
            } catch (NumberFormatException er) {
                Msg.send(player, "&c제대로된 숫자를 입력해주세요.", pfix);
                return true;
            }
            player.getInventory().addItem(LevelData.getExpBook(exp));
            return true;
        }
        else if (args[0].startsWith("장")) {
            if(args.length<2) {
                usage(player, "장비경험치책", true);
                return true;
            }
            int exp;
            try {
                exp = Integer.parseInt(args[1]);
            } catch (NumberFormatException er) {
                Msg.send(player, "&c제대로된 숫자를 입력해주세요.", pfix);
                return true;
            }
            player.getInventory().addItem(LevelData.getEquipmentExpBook(exp));
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
            long value;
            //숫자 입력 확인
            try {
                value = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c값을 제대로 입력해주세요.", pfix);
                return true;
            }
            if(value<0) {
                Msg.send(player, "&c경험치는 반드시 0 이상이 되어야합니다.", pfix);
                return true;
            }
            PlayerData pdc = new PlayerData(target);
            pdc.setExp(value);
            LevelData.addExp(target, 0L);
            LevelData.reloadIndicator(target);
            Msg.send(player, "성공적으로 경험치를 설정했습니다.", pfix);
            return true;
        }
        else if (args[1].equals("변경")) {
            if(args.length<3) {
                usage(player, "변경", true);
                return true;
            }
            //숫자 입력 확인
            long value;
            try {
                value = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c값을 제대로 입력해주세요.");
                return true;
            }
            LevelData.addExp(target, value);
            Msg.send(player, "성공적으로 경험치를 변경했습니다.", pfix);
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
            Msg.send(player, "&6/경험치관리 <플레이어> 설정 <값>");
            Msg.send(player, "&7   해당 플레이어의 경험치를 설정합니다.");
        }
        else if(arg.equalsIgnoreCase("변경")) {
            Msg.send(player, "&6/경험치관리 <플레이어> 변경 <값>");
            Msg.send(player, "&7   해당 플레이어의 경험치를 증감합니다.");
        }
        else if (arg.equals("경험치책")) {
            Msg.send(player, "&6/경험치관리 [경험치]책 <값>");
            Msg.send(player, "&7   해당 양의 경험치책을 생성합니다.");
        }
        else if (arg.equals("장비경험치책")) {
            Msg.send(player, "&6/경험치관리 장비[경험치책] <값>");
            Msg.send(player, "&7   해당 양의 경험치책을 생성합니다.");
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
