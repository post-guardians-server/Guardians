package me.rukon0621.guardians.party;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import net.playavalon.avnparty.AvNParty;
import net.playavalon.avnparty.player.AvalonPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.rukon0621.guardians.main.pfix;

public class PartyInviteCommand implements CommandExecutor {
    private static final AvNParty partyPlugin = AvNParty.plugin;
    private static final main plugin = main.getPlugin();

    public PartyInviteCommand() {
        plugin.getCommand("party").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length < 1) {
            Msg.send(player, "&6/파티초대 <플레이어>");
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[0]);
        if(target==null) {
            Msg.warn(player, "올바른 플레이어의 이름을 적어주세요.");
            return true;
        }
        AvalonPlayer avalonPlayer = partyPlugin.getAvalonPlayer(player);
        if(avalonPlayer.getParty()==null) {
            Msg.warn(player, "가진 파티가 존재하지 않습니다. 먼저 파티를 생성해주세요.");
            return true;
        }
        if(!avalonPlayer.getParty().getLeader().equals(avalonPlayer)) {
            Msg.warn(player, "파티의 리더만 플레이어를 초대할 수 있습니다.");
            return true;
        }
        if(target.equals(player)) {
            Msg.send(player, "흑흑 왜 자신을 초대하시는 건가요..ㅠㅠ",pfix);
            return true;
        }
        PartyManager.inviteParty(player, target, avalonPlayer.getParty());
        return true;
    }
}
