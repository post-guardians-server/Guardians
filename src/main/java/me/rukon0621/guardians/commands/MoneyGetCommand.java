package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static me.rukon0621.guardians.main.pfix;

public class MoneyGetCommand implements CommandExecutor {
    public static String[] arguments = {"basic"};
    public static final String moneyName = "디나르";

    public MoneyGetCommand() {
        main.getPlugin().getCommand("getmoney").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;

        if(args.length==0) {
            usages(player);
            return true;
        }

        int value, num = 1;
        try {
            if(args.length==2) {
                num = Integer.parseInt((args[1]));
            }
            value = Integer.parseInt((args[0]));
        } catch (NumberFormatException e) {
            Msg.warn(player, "제대로된 숫자를 입력해주세요.");
            return true;
        }
        if(num <= 0 || value <= 0) {
            Msg.warn(player, "수표를 0 이하의 수로 인출하시게요..?");
            return true;
        }
        int price = num * value;
        PlayerData pdc = new PlayerData(player);

        if(pdc.getMoney() < price) {
            Msg.warn(player, "돈이 부족합니다.");
            return true;
        }

        ItemStack it = LevelData.getDinarItem(value);
        it.setAmount(num);

        if(!InvClass.hasEnoughSpace(player.getInventory(), it)) {
            Msg.warn(player, "인벤토리에 공간이 부족합니다.");
            return true;
        }
        pdc.setMoney(pdc.getMoney() - price);
        player.getInventory().addItem(it);
        Msg.send(player, "수표를 발급했습니다.", pfix);
        return true;
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equalsIgnoreCase("basic")) {
            Msg.send(player, "&6/수표 <금액> [<수>]");
            Msg.send(player, "&7   해당 금액의 수표를 인출합니다.");
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
