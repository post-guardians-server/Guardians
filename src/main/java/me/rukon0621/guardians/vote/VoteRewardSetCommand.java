package me.rukon0621.guardians.vote;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.rukon0621.guardians.main.pfix;

public class VoteRewardSetCommand implements CommandExecutor {

    private static final VoteManager manager = main.getPlugin().getVoteManager();

    public VoteRewardSetCommand() {
        main.getPlugin().getCommand("setVoteReward").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player player)) return false;

        if(player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            Msg.warn(player, "손에 아이템을 들어주세요.");
            return true;
        }
        if(args.length < 1) {
            Msg.send(player, "&6/출석보상설정 <일>");
            Msg.send(player, "&7손에 들고 있는 아이템을 해당 출석일의 보상으로 설정합니다.");
            return true;
        }

        if(args[0].equalsIgnoreCase("리로드")) {
            manager.reload();
            return true;
        }

        try {
            int day = Integer.parseInt(args[0]);
            manager.setItem(day, player.getInventory().getItemInMainHand());
            Msg.send(player, "설정 완료.", pfix);
        } catch (NumberFormatException e) {
            Msg.warn(player, "정확한 숫자를 입력해주세요.");
        }
        return true;
    }
}
