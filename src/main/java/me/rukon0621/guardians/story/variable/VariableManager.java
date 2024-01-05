package me.rukon0621.guardians.story.variable;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.events.GuardiansLoginEvent;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableManager implements Listener {
    private final Map<String, Map<String, Integer>> tempVars = new HashMap<>();
    //private final Map<String, Integer> global = new HashMap<>();

    public VariableManager() {
        Bukkit.getPluginManager().registerEvents(this, main.getPlugin());
    }

    @EventHandler
    public void onLogIn(GuardiansLoginEvent e) {
        tempVars.put(e.getPlayer().getUniqueId().toString(), new HashMap<>());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        tempVars.remove(e.getPlayer().getUniqueId().toString());
    }

    public int getTempVar(Player player, String varName) {
        return tempVars.get(player.getUniqueId().toString()).getOrDefault(varName, 0);
    }

    public void setTempVar(Player player, String varName, int value) {
        tempVars.get(player.getUniqueId().toString()).put(varName, value);
    }

    /**
     * %var% 형식의 변수를 모두 변환
     * @param player player
     * @param target 탐색할 문자열
     * @return 해당 변수가 출력된 문자열을 반환 (parsed)
     */
    public String parseString(Player player, String target) {
        Pattern pat = Pattern.compile("%(.*?)%");	// 따옴표 안에 있는 패턴 추출.
        Matcher matcher = pat.matcher(target);
        PlayerData pdc = new PlayerData(player);
        while(matcher.find()) {
            String found = matcher.group();
            String varName = found.replaceAll("%", "");
            int value;
            if(varName.startsWith("prm")) value = pdc.getVar(varName);
            else value = getTempVar(player, varName);
            target = target.replaceFirst(found, String.valueOf(value));
        }
        return target;
    }

}
