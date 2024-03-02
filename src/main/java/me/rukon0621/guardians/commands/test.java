package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.GUI.item.enhance.StoneRemoveWindow;
import me.rukon0621.guardians.addspells.InvulSpell;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class test extends AbstractCommand {
    public test() {
        super("test", main.getPlugin());
    }

    @Override
    protected void usage(Player player, String usage) {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            System.out.println("updated");
            return true;
        }

        Msg.send(player, String.valueOf(InvulSpell.isInvincible(player)));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
