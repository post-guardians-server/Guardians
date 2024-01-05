package me.rukon0621.guardians.party;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import net.playavalon.avnparty.AvNParty;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.rukon0621.guardians.main.pfix;

public class PartyJoinCommand implements CommandExecutor {
    private static final AvNParty partyPlugin = AvNParty.plugin;
    private static final main plugin = main.getPlugin();

    public PartyJoinCommand() {
        plugin.getCommand("partyjoin").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length < 1) {
            Msg.send(player, "&6/파티참여 <플레이어>");
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[0]);
        if(target==null) {
            Msg.warn(player, "올바른 플레이어의 이름을 적어주세요.");
            return true;
        }
        else if(target.equals(player)) {
            Msg.send(player, "흑흑 왜 자신을 초대하시는 건가요..ㅠㅠ",pfix);
            return true;
        }
        else if(partyPlugin.getParty(player)!=null) {
            Msg.warn(player, "플레이어님은 이미 파티에 들어가 있습니다.");
            return true;
        }
        PartyRecruitListener listener = PartyRecruitListener.getPlayerListener(target);
        if(listener==null) {
            Msg.warn(player, "해당 플레이어는 파티를 모집하고 있지 않습니다.");
            return true;
        }
        listener.join(player);
        return true;
    }
}
