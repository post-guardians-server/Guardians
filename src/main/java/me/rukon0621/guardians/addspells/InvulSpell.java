package me.rukon0621.guardians.addspells;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.spells.DamageSpell;
import com.nisovin.magicspells.util.MagicConfig;
import me.rukon0621.guardians.helper.Msg;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.*;

public class InvulSpell extends BuffSpell {
    boolean onlyDamage;
    private static final Set<InvulSpell> spells = new HashSet<>();
    private final Set<UUID> entities = new HashSet<>();

    public static boolean isInvincible(LivingEntity le) {
        for(InvulSpell spell : spells) {
            if(spell.isActive(le)) return true;
        }
        return false;
    }

    public InvulSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        onlyDamage = this.getConfigBoolean("only-damage", true);
        spells.removeIf(s -> s.internalName.equals(internalName));
        spells.add(this);
    }

    @Override
    public boolean castBuff(LivingEntity livingEntity, float v, String[] strings) {
        entities.add(livingEntity.getUniqueId());
        return true;
    }

    @Override
    public boolean isActive(LivingEntity livingEntity) {
        return entities.contains(livingEntity.getUniqueId());
    }

    @Override
    protected void turnOffBuff(LivingEntity livingEntity) {
        entities.remove(livingEntity.getUniqueId());
    }

    @Override
    protected void turnOff() {
        for (EffectPosition pos : EffectPosition.values()) {
            this.cancelEffectForAllPlayers(pos);
        }
        entities.clear();
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
