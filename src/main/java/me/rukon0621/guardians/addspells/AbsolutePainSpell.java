package me.rukon0621.guardians.addspells;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import me.rukon0621.guardians.bar.BarManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AbsolutePainSpell extends TargetedPowerSpell implements TargetedEntitySpell {
    private final double damage;
    private final boolean percent; // 0 to 1
    private final boolean fromCurrentHealth;
    private final boolean powerAffectDamage;
    private final Map<Entity, Entity> hitDelayMap = new HashMap<>();

    public AbsolutePainSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        damage = this.getConfigDouble("damage", 1);
        percent = getConfigBoolean("percent", true);
        fromCurrentHealth = getConfigBoolean("fromCurrentHealth", false);
        powerAffectDamage = getConfigBoolean("powerAffectDamage", false);
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
            target.setNoDamageTicks(0);
            double damage = this.damage;
            if(powerAffectDamage) {
                damage *= power;
            }

            if(percent) {
                double dam;
                if(fromCurrentHealth) {
                    dam = target.getHealth() * (damage / 100D);
                }
                else {
                    dam = target.getMaxHealth() * (damage / 100D);
                }
                target.setHealth(Math.max(0, target.getHealth() - dam));
            }
            else {
                target.setHealth(Math.max(0, target.getHealth() - damage));
            }
            if(target instanceof Player player) BarManager.reloadBar(player);
            this.playSpellEffects(caster, target);
            return true;
        }
    }
}
