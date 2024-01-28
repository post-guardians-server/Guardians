package me.rukon0621.guardians.commands;

import me.rukon0621.buff.BuffManager;
import me.rukon0621.buff.RukonBuff;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.main;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class BuffClearCommand implements CommandExecutor, TabCompleter {

    public BuffClearCommand() {
        main.getPlugin().getCommand("buffclear").setExecutor(this);
        main.getPlugin().getCommand("buffclear").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        BuffManager manager = RukonBuff.inst().getBuffManager();
        if(args.length > 0) {
            String buffName = ArgHelper.sumArg(args);
            if(!manager.hasBuff(player, buffName)) {
                Msg.warn(player, "해당 플레이어는 해당 이름의 버프를 가지고 있지 않습니다.");
                return true;
            }
            manager.getBuffs(player).removeIf(buff -> buff.getBuffName().equals(buffName));
            manager.reloadBuffStats(player);
            Msg.send(player, buffName + " 버프를 삭제했습니다.", pfix);
            player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.5f);
            return true;
        }
        manager.getPlayerBuffDataMap().put(player.getUniqueId(), new ArrayList<>());
        manager.reloadBuffStats(player);
        Msg.send(player, "버프를 모두 삭제했습니다.", pfix);
        player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.5f);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player player)) return new ArrayList<>();
        if(args.length > 0) {
            List<String> list = new ArrayList<>();
            RukonBuff.inst().getBuffManager().getBuffs(player).forEach(buff -> {
                if(!buff.getBuffName().contains(args[0])) return;
                list.add(buff.getBuffName());
            });
            return list;
        }
        return new ArrayList<>();
    }
}
