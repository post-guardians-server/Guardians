package me.rukon0621.guardians.helper;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ActionBar {

    public static void sendActionBar(Player player, String msg) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Msg.color(msg)));
    }
    public static void sendActionBar(Player player, TextComponent text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, text);
    }


}
