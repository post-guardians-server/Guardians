package me.rukon0621.guardians.addspells;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class MythicPainSpell extends TargetedSpell implements TargetedEntitySpell {
    private final int hitDelay;
    private final double multiply;
    private final boolean isPercentage;
    private final Map<Entity, Entity> hitDelayMap = new HashMap<>();

    public MythicPainSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        multiply = getConfigDouble("multiply", 1);
        isPercentage = getConfigBoolean("percent", false);
        hitDelay = this.getConfigInt("hit-delay", 0);
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
        }
        else {
            return PostCastAction.HANDLE_NORMALLY;
        }
    }

    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        return this.validTargetList.canTarget(caster, target) && this.causePain(caster, target, power);
    }

    public boolean castAtEntity(LivingEntity target, float power) {
        return this.validTargetList.canTarget(target) && this.causePain(null, target, power);
    }

    private boolean causePain(LivingEntity caster, LivingEntity target, float power) {
        if (target == null) {
            return false;
        }
        else if (target.isDead()) {
            return false;
        }
        else if (!(target instanceof Player player)) {
            return false;
        }
        else {
            ActiveMob mob = MythicBukkit.inst().getMobManager().getMythicMobInstance(caster);
            if(mob==null) return false;
            if(hitDelay > 0) {
                if(hitDelayMap.containsKey(player)) return true;
                hitDelayMap.put(target, caster);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        hitDelayMap.remove(target);
                    }
                }.runTaskLaterAsynchronously(main.getPlugin(), hitDelay);
            }


            if(isPercentage) {
                BarManager.reloadBar(player, target.getMaxHealth() * multiply);
                target.setNoDamageTicks(0);
                target.damage(player.getMaxHealth() * multiply);

            }
            else {
                double localDamage = (mob.getDamage()) * (double)power;
                localDamage *= multiply;
                target.damage(localDamage, caster);
            }
            this.playSpellEffects(caster, target);
            return true;
        }
    }
}
