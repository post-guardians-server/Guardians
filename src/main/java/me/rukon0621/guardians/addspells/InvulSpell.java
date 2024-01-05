package me.rukon0621.guardians.addspells;

import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.spells.DamageSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class InvulSpell extends BuffSpell {
    boolean onlyDamage;

    public InvulSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        onlyDamage = this.getConfigBoolean("only-damage", true);
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
        durationEndTime.remove(livingEntity.getUniqueId());
    }

    @Override
    protected void turnOff() {
        durationEndTime.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpellTarget(SpellTargetEvent e) {
        if(!isActive(e.getTarget())) return;
        if(!onlyDamage) {
            e.setCancelled(true);
            return;
        }
        if(e.getSpell() instanceof DamageSpell) {
            e.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamaged(EntityDamageByEntityEvent e) {
        if(!(e.getEntity() instanceof LivingEntity le)) return;
        if(!isActive(le)) return;
        e.setCancelled(true);
    }
}
