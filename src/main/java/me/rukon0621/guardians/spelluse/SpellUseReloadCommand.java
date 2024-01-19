package me.rukon0621.guardians.spelluse;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.rukon0621.guardians.main.pfix;

public class SpellUseReloadCommand implements CommandExecutor {

    public SpellUseReloadCommand() {
        main.getPlugin().getCommand("reloadSpellUse").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        main.getPlugin().getSpellUseManager().reload();
        System.out.println("스펠 사용 리로드");
        if(commandSender instanceof Player player) Msg.send(player, "스펠 사용 리로드됨", pfix);

        return true;
    }
}
