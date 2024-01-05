package me.rukon0621.guardians.mailbox;

import me.rukon0621.guardians.commands.AbstractCommand;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class MailBoxCommand extends AbstractCommand {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"보내기"};

    public MailBoxCommand() {
        super("sendmail", main.getPlugin());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getType().equals(Material.AIR)) {
            Msg.send(player, "&c보낼 아이템을 손에 들어주세요.", pfix);
            return true;
        }

        if(args[0].equals("*")) {
            for(Player target : plugin.getServer().getOnlinePlayers()) {
                MailBoxManager.sendMail(target, item);
            }
            Msg.send(player, "&6모든 온라인 플레이어에게 메일을 전송했습니다.", pfix);
        }
        else if (args[0].equals("**")) {
            for(OfflinePlayer target : plugin.getServer().getOfflinePlayers()) {
                MailBoxManager.sendMail(target, item);
            }
            Msg.send(player, "&6오프라인을 포함한 모든 플레이어에게 메일을 전송했습니다.", pfix);
        }
        else {
            String target = args[0];
            if(!LogInOutListener.getProxyPlayerNames().contains(target)) {
                Msg.send(player, "&c제대로된 플레이어의 이름을 입력해주세요.", pfix);
                return true;
            }
            Msg.send(player, "&e성공적으로 메일을 전송했습니다.", pfix);
            MailBoxManager.sendMail(Bukkit.getOfflinePlayer(target), item);
        }
        return true;
    }

    @Override
    protected void usage(Player player, String usage) {
        if(usage.equals("보내기")) {
            Msg.send(player, "&6/메일보내기 <플레이어>");
            Msg.send(player, "&7   손에 들고 있는 아이템을 해당 플레이어에게 보냅니다.");
            Msg.send(player, "&7   ※<플레이어>에 *을 입력하면 접속중인 모든 플레이어에게 보냅니다.");
            Msg.send(player, "&7   ※<플레이어>에 **을 입력하면 오프라인을 포함한 모든 플레이어에게 보냅니다.");
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return TabCompleteUtils.search(LogInOutListener.getProxyPlayerNames(), args[0]);
    }
}
