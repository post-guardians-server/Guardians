package me.rukon0621.guardians.account;

import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AccountCommandTabComp implements TabCompleter {
    private final AccountManager accountManager = main.getPlugin().getAccountManager();
    private final List<String> ls = new ArrayList<>();
    public AccountCommandTabComp() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ls.clear();
                for(OfflinePlayer p : main.getPlugin().getServer().getOfflinePlayers()) {
                    ls.add(p.getName());
                }
            }
        }.runTaskTimerAsynchronously(main.getPlugin(), 0, 600);


    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return new ArrayList<>();
        if(args.length==2) {
            if(args[0].equals("보기")) {
                return TabCompleteUtils.searchAtList(ls, args[1]);
            }

            return TabCompleteUtils.searchAtList(accountManager.getAccountNames(), args[1]);
        }

        return result;
    }
}
