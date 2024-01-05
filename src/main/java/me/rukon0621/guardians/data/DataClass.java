package me.rukon0621.guardians.data;

import me.rukon0621.guardians.main;
import org.bukkit.OfflinePlayer;

public class DataClass {
    private static final main plugin = main.getPlugin();
    public static OfflinePlayer getPlayerFromUUID(String uuid) {
        return plugin.getServer().getOfflinePlayer(uuid);
    }
    public static int toInt(Object obj) {
        return ((Number) obj).intValue();
    }
    public static double toDouble(Object obj) {
        return ((Number) obj).doubleValue();
    }
}
