package me.rukon0621.guardians.addspells;

import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PainControlSpell extends BuffSpell {
    protected double defendProportion;
    protected double damageProportion;

    public PainControlSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        defendProportion = getConfigDouble("defend-proportion", 0);
        damageProportion = getConfigDouble("damage-proportion", 0);
    }

    @Override
    public boolean castBuff(LivingEntity livingEntity, float v, String[] strings) {
        return true;
    }

    @Override
    public boolean isActive(LivingEntity livingEntity) {
        return getDuration(livingEntity) > 0;
    }

    @Override
    protected void turnOffBuff(LivingEntity livingEntity) {
        setDuration(livingEntity, 0);
    }

    @Override
    protected void turnOff() {
        durationEndTime.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onDamage(PainCauseEvent e) {
        if(isActive(e.getCaster())) {
            e.setDamage(e.getDamage() * (1 + damageProportion));
        }

        //플레이어에게 공격을 받음
        if(isActive(e.getTarget())) {
            if(!(e.getTarget() instanceof Player)) return;
            e.setDamage(e.getDamage() * (1 - defendProportion));
        }
    }

    //몬스터에게 공격을 받음
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamaged(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof LivingEntity le) {
            if(!isActive(le)) return;
            if(e.getDamager() instanceof Player) return;
            e.setDamage(e.getDamage() * (1 - defendProportion));
        }
    }
}
