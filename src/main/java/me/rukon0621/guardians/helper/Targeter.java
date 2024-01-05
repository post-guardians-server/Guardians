package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.List;

public class Targeter {
    private static final main plugin = main.getPlugin();

    public static Entity getTargetedEntity(Player player, double range) {
        return getTargetedEntity(player, range, false);
    }

    public static LivingEntity getTargetedEntity(Player player, double range, boolean includeRealPlayer) {
        List<Entity> entities = player.getNearbyEntities(range, range, range);
        for(Entity entity : entities) {
            BlockIterator bItr = new BlockIterator(player, (int) range);
            if(entity.getName().equals(player.getName())&&includeRealPlayer) continue;
            if(!(entity instanceof LivingEntity)) continue;
            if(main.getPlugin().getServer().getPlayer(entity.getName())!=null) continue;
            while(bItr.hasNext()) {
                double distance = entity.getLocation().distanceSquared(bItr.next().getLocation());
                if(distance <= 1) {
                    return (LivingEntity) entity;
                }
            }
        }
        return null;
    }

    public static LivingEntity getTargetStraight(Player player, double range, double spread, boolean pvp)  {
        Location loc = player.getEyeLocation();
        double now = 0;
        while(now < range - spread) {
            now += 0.4;
            Vector vec = loc.getDirection();
            vec.normalize();
            vec.multiply(0.4);
            loc.add(vec);
            Material mat = loc.getBlock().getType();
            if(!(mat==Material.AIR||mat==Material.SNOW||mat==Material.TALL_GRASS||mat==Material.GRASS||mat==Material.WATER)) return null;

            for(Entity entity : player.getWorld().getNearbyEntities(loc, spread, spread ,spread)) {
                if(!(entity instanceof LivingEntity)) continue;
                if(entity instanceof ArmorStand) continue;
                if(entity instanceof Player && !pvp) continue;
                if(entity.equals(player)) continue;
                return (LivingEntity) entity;
            }
        }
        return null;
    }

    public static Block getTargetBlockOn(Player player, int range) {
        Block block = player.getTargetBlockExact(range);
        if(block==null) return null;
        Location loc = block.getLocation();
        if(!block.getType().equals(Material.SNOW)&&!block.getType().equals(Material.GRASS)&&!block.getType().equals(Material.TALL_GRASS)) {
            loc.setY(loc.getY()+1);
        }
        return player.getWorld().getBlockAt(loc);
    }


}
