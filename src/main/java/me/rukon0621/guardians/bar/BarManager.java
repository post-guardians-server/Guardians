package me.rukon0621.guardians.bar;

import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ActionBar;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.listeners.ResourcePackListener;
import me.rukon0621.guardians.main;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;

public class BarManager implements Listener {
    private static final main plugin = main.getPlugin();
    private static final HashMap<Player, Double> healRate = new HashMap<>(); //현재 체력의 비율

    /*
    public BarManager() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            healRate.put(player, player.getHealth() / player.getMaxHealth());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        healRate.put(player, player.getHealth() / player.getMaxHealth());
    }
     */

    public static void reloadBar(Player player, double damage) {
        if(!ResourcePackListener.acceptResourcePack(player)) return;
        if(LogInOutListener.getLoadingPlayers().contains(player.getName())) return;
        if(player.getGameMode()==GameMode.SPECTATOR) return;
        PlayerData pdc = new PlayerData(player);

        StringBuilder s = new StringBuilder("\uE100");

        //"무조건 / 체력 /무조건/경험치/자릿수에 따라/렙
        //"\uE100\uF200\uE101\uF201\uE102\uE210"

        //HEALTH (max is 200)
        int healthPer = (int) ((player.getHealth()-damage) / player.getMaxHealth()*200);

        if(healthPer<=0) s.append("\uF000");
        else if(healthPer>=200) s.append("\uF200");
        else if(healthPer<10) s.append("\\uF00").append(healthPer);
        else if(healthPer<100) s.append("\\uF0").append(healthPer);
        else s.append("\\uF").append(healthPer);
        s.append("\uE101");

        //EXP
        int expPer = (int) Math.ceil((double) pdc.getExp() / LevelData.expAtLevel.get(pdc.getLevel()) * 100);
        if(expPer<0) expPer = 0;
        if(expPer>99) expPer = 99;
        if(expPer==0) s.append("\uF300");
        else if (expPer < 10) s.append("\\uF20").append(expPer);
        else s.append("\\uF2").append(expPer);

        //LEVEL
        int level = pdc.getLevel();
        if(level < 10) {
            s.append("\\uE102");
            s.append("\\uE21").append(level);
        }
        else if (level < 100) {
            s.append("\\uE103");
            s.append("\\uE21").append(level / 10);
            s.append("\\uE21").append(level % 10);
        }
        else {
            s.append("\\uE104");

            int index = 0;
            int[] i = new int[]{0,0,0};
            int n = level;
            while(n > 0) {
                i[index] = n % 10;
                n /= 10;
                index++;
            }
            s.append("\\uE21").append(i[2]);
            s.append("\\uE21").append(i[1]);
            s.append("\\uE21").append(i[0]);
        }
        TextComponent text = new TextComponent(StringEscapeUtils.unescapeJava(s.toString()));
        text.setFont("bar");
        ActionBar.sendActionBar(player, text);
    }

    public static void reloadBar(Player player) {
        reloadBar(player, 0);
    }

}
