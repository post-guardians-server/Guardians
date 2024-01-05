package me.rukon0621.guardians.helper;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionManager {
    public static void effectGive(LivingEntity target, PotionEffectType type, double duration, int amplifier) {
        PotionEffect potion = new PotionEffect(type, (int) (duration*20), amplifier, false, false, false);
        target.addPotionEffect(potion, false);
    }

    public static void effectRemove(LivingEntity target, PotionEffectType type) {
        target.removePotionEffect(type);
    }

    public static void effectRemoveAll(LivingEntity target) {
        for(PotionEffectType type : PotionEffectType.values()) {
            target.removePotionEffect(type);
        }
    }
}
