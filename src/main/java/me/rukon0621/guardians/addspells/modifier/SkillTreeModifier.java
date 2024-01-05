package me.rukon0621.guardians.addspells.modifier;

import com.nisovin.magicspells.castmodifiers.Condition;
import me.rukon0621.guardians.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SkillTreeModifier extends Condition {
    private String skillName;

    @Override
    public boolean initialize(String s) {
        if(s==null) return false;
        skillName = s.replaceAll("_", " ");
        return true;
    }

    @Override
    public boolean check(LivingEntity livingEntity) {
        if(!(livingEntity instanceof Player player)) return false;
        PlayerData pdc = new PlayerData(player);
        if(pdc.isDataNull()) return false;
        return pdc.hasSkill(skillName);
    }

    @Override
    public boolean check(LivingEntity livingEntity, LivingEntity livingEntity1) {
        return false;
    }

    @Override
    public boolean check(LivingEntity livingEntity, Location location) {
        return false;
    }
}
