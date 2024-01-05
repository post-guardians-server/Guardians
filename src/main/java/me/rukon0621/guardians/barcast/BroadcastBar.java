package me.rukon0621.guardians.barcast;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class BroadcastBar implements Listener {
    private static final ArrayList<BroadcastBar> bars = new ArrayList<>();
    private static final main plugin = main.getPlugin();
    private final BossBar bar;
    private final String name;

    public static List<String> getNames() {
        List<String> list = new ArrayList<>();
        for(BroadcastBar bar : bars) {
            list.add(bar.getName());
        }
        return list;
    }

    public static void removeBroadcast(Player player, String name) {
        BroadcastBar target = null;
        for(BroadcastBar bar : bars) {
            if(bar.getName().equals(name)) {
                target = bar;
                break;
            }
        }
        if(target==null) {
            if(player!=null) Msg.warn(player, "해당 이름의 공지는 존재하지 않습니다.", pfix);
            return;
        }
        target.disable();
        bars.remove(target);
        if(player!=null) Msg.send(player, "성공적으로 공지를 삭제했습니다.", pfix);
    }

    public static void createNewBroadcast(Player player, String title, String barColor, String barStyle) {
        BarColor color;
        BarStyle style;
        try {
            color = BarColor.valueOf(barColor);
            style = BarStyle.valueOf(barStyle);
        } catch (IllegalArgumentException e) {
            Msg.warn(player, "제대로된 값을 입력해주세요.");
            return;
        }
        new BroadcastBar(title, color, style);
        if(player!=null) {
            if(getNames().contains(title)) {
                Msg.warn(player, "이미 존재하는 내용의 공지입니다.", pfix);
                return;
            }
            Msg.send(player, "새로운 공지를 생성했습니다.", pfix);
        }
    }

    public BroadcastBar(String title, BarColor color, BarStyle style) {
        name = title;
        bar = Bukkit.createBossBar(Msg.color(title), color, style);
        bar.setVisible(true);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            bar.addPlayer(player);
        }
        bars.add(this);
    }

    public String getName() {
        return name;
    }

    public void disable() {
        bar.removeAll();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        bar.addPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        bar.removePlayer(e.getPlayer());
    }
}
