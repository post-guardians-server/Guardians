package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.DamagingListener;
import me.rukon0621.guardians.main;
import me.rukon0621.pay.shop.MONEY;
import me.rukon0621.pay.shop.ShopWindow;
import me.rukon0621.rinstance.RukonInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AfkShopCommand implements CommandExecutor {

    public AfkShopCommand() {
        main.getPlugin().getCommand("afkShop").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player player)) return false;
        if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(player) || DamagingListener.getRemainCombatTime(player) != -1) {
            Msg.warn(player, "지금은 이 명령어를 이용할 수 없습니다.");
            return true;
        }
        new ShopWindow(player, "잠수", MONEY.AFK_POINT);
        return true;
    }
}
