package me.rukon0621.guardians.helper;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Random;
import java.util.Set;

public class Rand {
    public static int randInt(int range1, int range2) {
        return new Random().nextInt(range2 - range1 + 1) + range1;
    }
    public static double randDouble(double range1, double range2) {
        return new Random().nextDouble(range2 - range1 + 0.001) + range1;
    }

    public static float randFloat(double range1, double range2) {
        return (float) randDouble(range1, range2);
    }

    /**
     * @param chance 0 ~ 100 사이의 확률
     * @return 해당 확률에 걸리면 true 아니면 false
     */
    public static boolean chanceOf(double chance) {
        return randDouble(0, 100) <= chance;
    }

    public static Location randLoc(Location loc, double range) {
        Location loc2 = loc.clone();
        loc2.setX(randDouble(loc2.getX()-range, loc2.getX()+range));
        loc2.setZ(randDouble(loc2.getZ()-range, loc2.getZ()+range));
        return loc2;
    }

    /**
     *
     * @param loc 중심 위치
     * @param range 랜덤 범위
     * @return 해당 범위 중 블럭이 없는(공기)의 랜덤 위치를 반환
     */
    public static Location randLocOnlyAir(Location loc, double range) {
        Location loc2 = loc.clone();
        loc2.setX(randDouble(loc2.getX()-range, loc2.getX()+range));
        loc2.setZ(randDouble(loc2.getZ()-range, loc2.getZ()+range));
        if(!loc2.getBlock().getType().equals(Material.AIR) || !loc2.add(0,1,0).getBlock().getType().equals(Material.AIR)) return randLocOnlyAir(loc, range);
        return loc2;
    }

    public static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }
    public static <E> E getRandomCollectionElement(Collection<E> collection) {
        return collection.stream().skip(new Random().nextInt(collection.size())).findFirst().orElse(null);
    }
}
