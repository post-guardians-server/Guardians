package me.rukon0621.guardians.commands;

import me.rukon0621.buff.BuffData;
import me.rukon0621.buff.RukonBuff;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.pfix;

public class BuffClearCommand implements CommandExecutor {

    public BuffClearCommand() {
        main.getPlugin().getCommand("buffclear").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        BuffData buffData = RukonBuff.inst().getBuffManager().getPlayerBuffData(player);
        buffData.getBuffData().clear();
        buffData.reloadCache();
        Msg.send(player, "버프를 모두 삭제했습니다.", pfix);
        return true;
    }
}
