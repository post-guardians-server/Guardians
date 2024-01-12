package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.listeners.ChatEventListener;
import me.rukon0621.guardians.main;
import net.playavalon.avnparty.AvNParty;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.rukon0621.guardians.main.pfix;

public class ChatCommand extends AbstractCommand {
    private final Set<String> set = new HashSet<>();
    public ChatCommand() {
        super("chat", main.getPlugin());
        for(ChatEventListener.ChatChannel chatChannel : ChatEventListener.ChatChannel.values()) {
            if(chatChannel.isBlockChoice()) continue;
            set.add(chatChannel.toString());
        }
    }

    @Override
    protected void usage(Player player, String usage) {
        Msg.send(player,  " ");
        Msg.send(player, "&6/채팅 <채팅범위>");
        Msg.send(player, "&7채팅의 범위를 설정합니다.");
        Msg.send(player, "&7- ALL : 모든 채널에서 사용되는 전체 채팅");
        Msg.send(player, "&7- CHANNEL : 현재 있는 채널에서 사용되는 채널 채팅");
        Msg.send(player, "&7- GUILD : 길드 채팅");
        Msg.send(player, "&7- PARTY : 현재 있는 파티에서 사용되는 파티 채팅");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usage(player, "help");
            return true;
        }
        try {
            ChatEventListener.ChatChannel chatChannel = ChatEventListener.ChatChannel.valueOf(args[0].toUpperCase(Locale.ROOT));
            if(chatChannel.isBlockChoice()) {
                Msg.warn(player, "올바른 범위를 써주세요.");
                return true;
            }
            else if(chatChannel.equals(ChatEventListener.ChatChannel.PARTY) && AvNParty.plugin.getParty(player)==null) {
                Msg.warn(player, "파티 채팅을 사용하려면 먼저 파티를 생성해주세요.");
                return true;
            }
            ChatEventListener.setPlayerChatChannel(player, chatChannel);
            ChatEventListener.getPlayerChatCache(player).add(chatChannel.getStr());
            Msg.send(player, "채팅 범위를 변경했습니다!", pfix);
        } catch (IllegalArgumentException e) {
            Msg.warn(player, "올바른 범위를 써주세요.");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length==1) return TabCompleteUtils.search(set, args[0]);
        return new ArrayList<>();
    }
}
