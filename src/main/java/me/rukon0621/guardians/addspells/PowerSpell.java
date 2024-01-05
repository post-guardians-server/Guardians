package me.rukon0621.guardians.addspells;

import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.equipment.EquipmentManager;
import org.apache.maven.model.plugin.LifecycleBindingsInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PowerSpell extends BuffSpell {
    private Stat stat;
    private final double value;
    private final Set<LivingEntity> actives = new HashSet<>();

    public PowerSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        cancelOnDeath = true;
        cancelOnLogout = true;
        try {
            stat = Stat.valueOf(getConfigString("stat", "armor").toUpperCase());
        } catch (IllegalArgumentException e) {
            StringBuilder sb = new StringBuilder("알 수 없는 스텟입니다. 사용 가능한 상수 값: ");
            for(Stat s : Stat.values()) {
                sb.append(s.toString()).append(", ");
            }
            Bukkit.getLogger().severe(sb.toString());
            stat = Stat.ATTACK_DAMAGE;
        }
        value = getConfigDouble("value", 10);
    }

    @Override
    public boolean castBuff(LivingEntity livingEntity, float v, String[] strings) {
        if(!(livingEntity instanceof Player player)) return false;
        stat.setAdd(player, stat.getAdd(player) + value);
        actives.add(player);
        EquipmentManager.reloadEquipment(player, false);
        return true;
    }


    @Override
    public boolean isActive(LivingEntity livingEntity) {
        return actives.contains(livingEntity);
    }

    @Override
    protected void turnOffBuff(LivingEntity livingEntity) {
        actives.remove(livingEntity);
        if(!(livingEntity instanceof Player player)) return;
        if(!player.isOnline()) return;
        stat.setAdd(player, stat.getAdd(player) - value);
        EquipmentManager.reloadEquipment(player, false);
    }

    @Override
    protected void turnOff() {
        for(LivingEntity livingEntity : actives) {
            turnOff(livingEntity);
        }
        actives.clear();
    }
}
