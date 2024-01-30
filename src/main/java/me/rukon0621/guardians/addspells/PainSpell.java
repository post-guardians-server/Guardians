package me.rukon0621.guardians.addspells;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import me.rukon0621.guardians.main;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class PainSpell extends TargetedPowerSpell implements TargetedEntitySpell {
    private final boolean tryAvoidingAntiCheatPlugins;
    private final double damage;
    private final double knockBackResistance;
    private final boolean powerAffectDamage;
    private final int hitDelay;
    private final Map<Entity, Entity> hitDelayMap = new HashMap<>();

    public PainSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        damage = this.getConfigDouble("damage", 1);
        powerAffectDamage = getConfigBoolean("powerAffectDamage", true);
        this.tryAvoidingAntiCheatPlugins = this.getConfigBoolean("try-avoiding-anticheat-plugins", false);
        hitDelay = this.getConfigInt("hit-delay", 0);
        knockBackResistance = getConfigDouble("knockback", 0.0);
    }

    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            TargetInfo<LivingEntity> target = this.getTargetedEntity(caster, power);
            if (target == null) {
                return this.noTarget(caster);
            } else {
                boolean done;
                done = this.causePain(caster, target.getTarget(), target.getPower());
                if (!done) {
                    return this.noTarget(caster);
                } else {
                    this.sendMessages(caster, target.getTarget(), args);
                    return PostCastAction.NO_MESSAGES;
                }
            }
        } else {
            return PostCastAction.HANDLE_NORMALLY;
        }
    }

    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        return this.validTargetList.canTarget(caster, target) && this.causePain(caster, target, power);
    }

    public boolean castAtEntity(LivingEntity target, float power) {
        return this.validTargetList.canTarget(target) && this.causePain(null, target, power);
    }

    public double getDamage() {
        return damage;
    }

    private boolean causePain(LivingEntity caster, LivingEntity target, float power) {
        if (target == null) {
            return false;
        }
        else if (target.isDead()) {
            return false;
        }
        else {
            if(hitDelay > 0) {
                if(hitDelayMap.containsKey(target)) return true;
                hitDelayMap.put(target, caster);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        hitDelayMap.remove(target);
                    }
                }.runTaskLaterAsynchronously(main.getPlugin(), hitDelay);
            }
            target.setNoDamageTicks(0);
            PainCauseEvent event = new PainCauseEvent(this, caster, target);
            main.getPlugin().getServer().getPluginManager().callEvent(event);

            if(event.isCancelled()) return false;

            float power2 = modifyPower(caster, 1);
            double localDamage = Math.abs(event.getDamage());
            if(powerAffectDamage) localDamage *= power + power2 - 1;
            else localDamage *= power2;

            //if(localDamage < 0)
            localDamage *= -1;

            try {
                double kbr = knockBackResistance;
                AttributeInstance attr = target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                Vector v = target.getLocation().toVector().subtract(caster.getLocation().toVector()).normalize();
                v.multiply(kbr * Math.max(0, 1 - attr.getValue()));
                v.setY(0);
                target.setVelocity(target.getVelocity().subtract(v));
            } catch (NullPointerException ignored) {}

            if (this.tryAvoidingAntiCheatPlugins) {
                target.damage(localDamage);
            } else {
                target.damage(localDamage, caster);
            }

            this.playSpellEffects(caster, target);
            return true;
        }
    }
}
