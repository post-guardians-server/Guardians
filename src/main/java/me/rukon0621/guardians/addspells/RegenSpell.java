//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.rukon0621.guardians.addspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.MagicSpellsEntityRegainHealthEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.util.ValidTargetChecker;
import com.nisovin.magicspells.util.compat.EventUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class RegenSpell extends TargetedSpell implements TargetedEntitySpell {
    private final double healAmount = this.getConfigFloat("heal-amount", 10.0F);
    private final int healPercent = this.getConfigInt("heal-percent", 0);
    private final boolean checkPlugins;
    private final boolean cancelIfFull;
    private final String strMaxHealth;
    private final boolean fixTargetToCaster;
    private final ValidTargetChecker checker;

    public RegenSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        if (this.healPercent < 0 || this.healPercent > 100) {
            MagicSpells.error("HealSpell '" + this.internalName + "' uses heal-percent outside bounds 0-100.");
        }

        this.checkPlugins = this.getConfigBoolean("check-plugins", true);
        this.cancelIfFull = this.getConfigBoolean("cancel-if-full", true);
        this.strMaxHealth = this.getConfigString("str-max-health", "%t is already at max health.");
        this.fixTargetToCaster = this.getConfigBoolean("fix-target-to-caster", false);
        this.checker = (entity) -> {
            return entity.getHealth() < Util.getMaxHealth(entity);
        };
    }

    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            TargetInfo<LivingEntity> targetInfo = this.getTargetedEntity(caster, power, this.checker);
            if (targetInfo == null) {
                return this.noTarget(caster);
            } else {
                LivingEntity target = (LivingEntity)targetInfo.getTarget();
                power = targetInfo.getPower();
                if (this.cancelIfFull && target.getHealth() == Util.getMaxHealth(target)) {
                    return this.noTarget(caster, this.formatMessage(this.strMaxHealth, new String[]{"%t", this.getTargetName(target)}));
                } else {
                    boolean healed = this.heal(caster, target, power);
                    if (!healed) {
                        return this.noTarget(caster);
                    } else {
                        this.sendMessages(caster, target, args);
                        return PostCastAction.NO_MESSAGES;
                    }
                }
            }
        } else {
            return PostCastAction.HANDLE_NORMALLY;
        }
    }

    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        return this.validTargetList.canTarget(caster, target) && target.getHealth() < Util.getMaxHealth(target) && this.heal(caster, target, power);
    }

    public boolean castAtEntity(LivingEntity target, float power) {
        return this.validTargetList.canTarget(target) && target.getHealth() < Util.getMaxHealth(target) && this.heal(null, target, power);
    }

    public ValidTargetChecker getValidTargetChecker() {
        return this.checker;
    }

    private boolean heal(LivingEntity livingEntity, LivingEntity target, float power) {
        if(fixTargetToCaster) {
            target = livingEntity;
        }
        double health = target.getHealth();
        double amount;
        if (this.healPercent == 0) {
            amount = this.healAmount * (double)power;
        } else {
            amount = Util.getMaxHealth(livingEntity) * (double)((float)this.healPercent / 100.0F);
        }

        if (this.checkPlugins) {
            MagicSpellsEntityRegainHealthEvent event = new MagicSpellsEntityRegainHealthEvent(target, amount, RegainReason.CUSTOM);
            EventUtil.call(event);
            if (event.isCancelled()) {
                return false;
            }

            amount = event.getAmount();
        }

        health += amount;
        if (health > Util.getMaxHealth(target)) {
            health = Util.getMaxHealth(target);
        }

        target.setHealth(health);
        if (livingEntity == null) {
            this.playSpellEffects(EffectPosition.TARGET, target);
        } else {
            this.playSpellEffects(livingEntity, target);
        }

        return true;
    }
}
