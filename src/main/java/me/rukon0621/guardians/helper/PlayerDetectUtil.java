package me.rukon0621.guardians.helper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayerDetectUtil {
    public static List<Player> getPlayersWithin(Location loc, double radius) {
        List<Player> players = new ArrayList();
        World world = loc.getWorld();
        if (world == null) {
            return players;
        } else {
            Iterator var5 = world.getNearbyEntities(loc, radius, radius, radius, (entity) -> {
                return entity.getType() == EntityType.PLAYER;
            }).iterator();

            while(var5.hasNext()) {
                Entity ent = (Entity)var5.next();
                if (ent instanceof Player) {
                    Player player = (Player)ent;
                    if (Bukkit.getOnlinePlayers().contains(player)) {
                        players.add(player);
                    }
                }
            }
            return players;
        }
    }
}
