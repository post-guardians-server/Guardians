package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.addspells.BlackOutSpell;
import me.rukon0621.guardians.main;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Msg {
    public static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");
    private static final String warnPrefix = main.pfix;

    public static void send(Player player, String message) {
        send(player, message, "&f");
    }
    public static void send(Player player, BaseComponent[] message) {
        player.spigot().sendMessage(message);
    }
    public static void send(Player player, String message, String prefix) {
        player.sendMessage(color(prefix+message));
    }

    public static void warn(Player player, String warnMessage) {
        player.sendMessage(color(warnPrefix+"&c"+warnMessage));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
    }
    public static void warn(Player player, String warnMessage, String prefix) {
        player.sendMessage(color(prefix+" &c"+warnMessage));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
    }

    public static String uncolor(String text) {
        return ChatColor.stripColor(text);
    }
    public static String recolor(String text) {
        return text.replaceAll("§", "&");
    }

    public static String color(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendTitle(Player player, String title, int stay) {
        sendTitle(player, title, null, 10, stay, 10);
    }
    public static void sendTitle(Player player, String title, int stay, int fadeIn, int fadeOut) {
        sendTitle(player, title, null, fadeIn, stay, fadeOut);
    }
    public static void sendTitle(Player player, String title, String subTitle, int stay) {
        sendTitle(player, title, subTitle, 10, stay, 10);
    }
    public static void sendTitle(Player player, String title, String subTitle, int stay, int fadeIn, int fadeOut) {
        if(BlackOutSpell.getBlackOutPlayers().contains(player)) return;

        if(subTitle==null) player.sendTitle(color(title), null, fadeIn, stay, fadeOut);
        else player.sendTitle(color(title), color(subTitle), fadeIn, stay, fadeOut);
    }


}
