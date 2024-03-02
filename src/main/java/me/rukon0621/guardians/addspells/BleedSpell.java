package me.rukon0621.guardians.addspells;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.main;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BleedSpell extends TargetedPowerSpell implements TargetedEntitySpell {
    private final double damage;
    private final double duration;
    private final int tick;
    private final boolean powerAffectDuration;
    private final boolean powerAffectDamage;
    private final double damageMultiply;
    private final double boundaryValue;

    public BleedSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        damage = this.getConfigDouble("damage", 0.05);
        tick = this.getConfigInt("tick", 5);
        damageMultiply = getConfigDouble("damageMultiply", 1.5);
        boundaryValue = getConfigDouble("boundaryValue", 0.5);
        duration = this.getConfigDouble("duration", 2);
        powerAffectDuration = getConfigBoolean("powerAffectDuration", true);
        powerAffectDamage = getConfigBoolean("powerAffectDamage", false);
    }

    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            TargetInfo<LivingEntity> target = this.getTargetedEntity(caster, power);
            if (target == null) {
                return this.noTarget(caster);
            } else {
                boolean done;
                done = this.causeBleeding(caster, target.getTarget(), target.getPower());
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
        return this.validTargetList.canTarget(caster, target) && this.causeBleeding(caster, target, power);
    }

    public boolean castAtEntity(LivingEntity target, float power) {
        return this.validTargetList.canTarget(target) && this.causeBleeding(null, target, power);
    }
    public double getDamage() {
        return damage;
    }

    private boolean causeBleeding(LivingEntity caster, LivingEntity target, float power) {
        if (target == null) {
            return false;
        }
        else if (target.isDead()) {
            return false;
        }
        else if (target.getType().equals(EntityType.HUSK)) {
            return false;
        }
        else {
            float power2 = modifyPower(caster, 1);
            target.setNoDamageTicks(0);
            double localDamage = damage;
            if(powerAffectDamage) localDamage *= power;
            localDamage *= power2;
            int repeat;
            if(powerAffectDuration) repeat = (int) ((duration * power * power2 * 20) / tick);
            else repeat = (int) (duration * 20 * power2 / tick);
            if(caster instanceof Player player) {
                localDamage *= Stat.ATTACK_DAMAGE.getTotal(player);
            }
            new BleedTimer(caster, target, localDamage, repeat);
            this.playSpellEffects(caster, target);
            return true;
        }
    }

    class BleedTimer extends BukkitRunnable {
        final LivingEntity caster;
        final LivingEntity target;
        final double damage;
        int tick;

        public BleedTimer(LivingEntity caster, LivingEntity target, double damage, int repeat) {
            this.caster = caster;
            this.target = target;
            this.damage = damage;
            tick = repeat;
            runTaskTimer(main.getPlugin(), 0, BleedSpell.this.tick);
        }

        @Override
        public void run() {
            if(target.isDead() || tick == 0) {
                cancel();
                return;
            }
            tick--;
            target.setNoDamageTicks(0);
            double damage = this.damage;
            if(target.getHealth()/target.getMaxHealth() < boundaryValue) {
                damage *= damageMultiply;
                target.getWorld().spawnParticle(Particle.BLOCK_CRACK, target.getLocation().add(0,1,0), 6, Material.RED_NETHER_BRICKS.createBlockData());
            }
            else {
                target.getWorld().spawnParticle(Particle.BLOCK_CRACK, target.getLocation().add(0,1,0), 4, Material.FIRE_CORAL_BLOCK.createBlockData());
            }
            if(target instanceof Player player) {
                damage *= (1 - Stat.ABSOLUTE_ARMOR.getTotal(player));
                BarManager.reloadBar(player, damage);
            }
            target.damage(damage);
            target.setNoDamageTicks(0);
        }
    }
}
