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

public class MoneyCommand implements CommandExecutor {
    private final main plugin;
    public static String[] arguments = {"설정", "변경", "수표"};
    public static final String moneyName = "디나르";

    public MoneyCommand(main plugin) {
        plugin.getCommand("money").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;

        if(args.length==0) {
            usages(player);
            return true;
        }

        if (args[0].equals("수표")) {
            if(args.length < 2) {
                usage(player, "수표", true);
                return true;
            }
            int cost;
            try {
                cost = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c올바른 숫자를 입력해주세요.", pfix);
                return true;
            }
            player.getInventory().addItem(LevelData.getDinarItem(cost));
            Msg.send(player, "수표를 발급했습니다.", pfix);
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
            PlayerData pdc = new PlayerData(target);
            pdc.setMoney(value);
            Msg.send(player, "성공적으로 돈을 설정했습니다.", pfix);
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
            PlayerData pdc = new PlayerData(target);
            pdc.setMoney(pdc.getMoney()+value);
            Msg.send(player, "성공적으로 돈을 변경했습니다.", pfix);
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
            Msg.send(player, "&6/돈관리 <플레이어> 설정 <값>");
            Msg.send(player, "&7   해당 플레이어의 돈을 설정합니다.");
        }
        else if(arg.equalsIgnoreCase("변경")) {
            Msg.send(player, "&6/돈관리 <플레이어> 변경 <값>");
            Msg.send(player, "&7   해당 플레이어의 돈을 증감합니다.");
        }
        else if(arg.equalsIgnoreCase("수표")) {
            Msg.send(player, "&6/돈관리 수표 <금액>");
            Msg.send(player, "&7   수표를 뽑습니다.");
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
