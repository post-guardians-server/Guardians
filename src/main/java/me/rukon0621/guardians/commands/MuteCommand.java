package me.rukon0621.guardians.commands;

import com.google.common.io.ByteArrayDataOutput;
import me.rukon0621.callback.LogManager;
import me.rukon0621.callback.speaker.Speaker;
import me.rukon0621.callback.speaker.SpeakerListenEvent;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.DateUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class MuteCommand extends AbstractCommand implements Listener {
    public MuteCommand() {
        super("mute", main.getPlugin());
        arguments.add("해제");
        arguments.add("추가");
        Bukkit.getServer().getPluginManager().registerEvents(this, main.getPlugin());
    }

    @Override
    protected void usage(Player player, String usage) {
        if (usage.equals("해제")) {
            Msg.send(player, "&6/뮤트 해제 <player> [<사유>]");
        }
        else if (usage.equals("추가")) {
            Msg.send(player, "&6/뮤트 추가 <player> <기간(단위:시간, 소수점 사용 가능)> [<사유>]");
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length == 0) {
            usages(player);
            return true;
        }
        String target = args[1];
        if(!LogInOutListener.getProxyPlayerNames().contains(target)) {
            Msg.warn(player, "해당 플레이어는 서버에 존재하지 않습니다.");
            return true;
        }
        if(args[0].equals("해제")) {
            String reason = String.format("%s의 뮤트를 해제함.", target);
            if(args.length > 2) {
                reason += "사유: " + ArgHelper.sumArg(args, 2);
            }
            new Speaker("muteCommand", target, "해제");
            Msg.send(player, "플레이어의 뮤트를 해제했습니다.", pfix);
            LogManager.log(player, "mute", reason);
        }
        else if(args[0].equals("추가")) {
            if(args.length < 3) {
                usage(player, args[0], true);
                return true;
            }
            double value;
            try {
                value = Double.parseDouble(args[2]);
                if(value <= 0 || value > 1000) {
                    Msg.warn(player, "최소 0시간 초과 1000시간 이하의 뮤트 시간을 적용할 수 있습니다");
                    return true;
                }
            } catch (NumberFormatException e) {
                Msg.warn(player, "제대로된 시간을 입력하십시오.");
                return true;
            }
            long millis = (long) (value * 1000L * 3600L);
            String reason = String.format("%s에게 %s 만큼의 뮤트를 지급함. 사유: ", target, DateUtil.formatDate(millis / 1000L));
            if(args.length > 3) {
                reason += ArgHelper.sumArg(args, 3);
            }
            new Speaker("muteCommand", target, "추가") {
                @Override
                public void construct(ByteArrayDataOutput bo) {
                    bo.writeLong(millis);
                }
            };
            Msg.send(player, "해당 플레이어의 채팅을 " + DateUtil.formatDate(millis / 1000L) + " 간 금지시켰습니다. 뮤트 이후 3일 이내로 디스코드의 티켓을 통해 이의를 제기할 수 있습니다.", pfix);
            LogManager.log(player, "mute", reason);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length == 1) {
            return TabCompleteUtils.search(arguments, args[0]);
        }if(args.length == 2) {
            return TabCompleteUtils.search(LogInOutListener.getProxyPlayerNames(), args[1]);
        }
        return new ArrayList<>();
    }

    @EventHandler
    public void onListenMuteEvent(SpeakerListenEvent e) {
        if(!e.getMainAction().equals("muteCommand")) return;
        Player player;
        if((player = Bukkit.getPlayerExact(e.getIn().readUTF())) == null) return;
        PlayerData pdc = new PlayerData(player);
        String arg1 = e.getIn().readUTF();
        if(arg1.equals("추가")) {
            long millis = e.getIn().readLong();
            pdc.setMuteMillis(Math.max(System.currentTimeMillis(), pdc.getMuteMillis()) + millis);
            Msg.send(player, "관리자에 의해 " + DateUtil.formatDate(millis / 1000L) + " 간 채팅이 금지되었습니다. 뮤트 이후 3일 이내로 디스코드의 티켓을 통해 이의를 제기할 수 있습니다.", pfix);
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
        }
        else if(arg1.equals("해제")) {
            if(pdc.getMuteMillis() < System.currentTimeMillis()) return;
            Msg.send(player, "관리자에 의해 뮤트가 해제되었습니다.", pfix);
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
            pdc.setMuteMillis(0);
        }

    }

}
