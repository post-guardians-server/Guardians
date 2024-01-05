package me.rukon0621.guardians.party;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import net.playavalon.avnparty.AvNParty;
import net.playavalon.avnparty.party.Party;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.rukon0621.guardians.main.pfix;

public class PartyRecruitCommand implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    private static final AvNParty partyPlugin = AvNParty.plugin;

    public PartyRecruitCommand() {
        plugin.getCommand("recruit").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;

        if(label.equals("파티참가수락")) {
            if(args.length < 1) {
                Msg.send(player, "&6/파티참가수락 <플레이어>");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[0]);
            if(target==null) {
                Msg.warn(player, "해당 이름의 플레이어는 존재하지 않습니다.");
                return true;
            }
            PartyRecruitListener listener = PartyRecruitListener.getPlayerListener(player);
            if(listener==null) {
                Msg.warn(player, "파티 모집을 진행하고 있지 않습니다.");
                return true;
            }
            listener.accepted(target);
            return true;
        }
        else if(label.equals("모집중단")) {
            PartyRecruitListener listener = PartyRecruitListener.getPlayerListener(player);
            if(listener==null) {
                Msg.warn(player, "파티 모집을 진행하고 있지 않습니다.");
                return true;
            }
            listener.disable();
            listener.end();
            Msg.send(player, "파티 모집을 중단했습니다.", pfix);
            return true;
        }

        Party party = partyPlugin.getParty(player);
        if(party==null) {
            Msg.warn(player, "파티 모집을 하려면 먼저 파티를 만들어야합니다.");
            new PartyCreateWindow(player);
            return true;
        }
        if(!party.getLeader().getPlayer().equals(player)) {
            Msg.warn(player, "파티의 리더만 파티원을 모집할 수 있습니다.");
            return true;
        }
        if(PartyRecruitListener.getPlayerListener(player)!=null) {
            Msg.warn(player, "이미 파티 모집을 진행하고 있습니다.");
            return true;
        }

        new PartyRecruitListener(player);
        return true;
    }
}
