package me.rukon0621.guardians.addspells;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.PotionManager;
import me.rukon0621.guardians.helper.Rand;
import me.rukon0621.guardians.main;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class KnockdownSpell extends TargetedPowerSpell implements TargetedEntitySpell {
    private final double multiply;
    private final boolean powerAffectMultiply;
    private final boolean playEffect;
    private final double chance;
    private final int hitDelay;
    private final Map<Entity, Entity> hitDelayMap = new HashMap<>();

    public KnockdownSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        multiply = this.getConfigDouble("multiply", 1);
        powerAffectMultiply = getConfigBoolean("powerAffectMultiply", false);
        playEffect = getConfigBoolean("playEffect", false);
        chance = getConfigDouble("percent", 100);
        hitDelay = this.getConfigInt("hit-delay", 0);
    }

    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            TargetInfo<LivingEntity> target = this.getTargetedEntity(caster, power);
            if (target == null) {
                return this.noTarget(caster);
            } else {
                boolean done;
                done = this.knockdown(caster, target.getTarget(), target.getPower());
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
        return this.validTargetList.canTarget(caster, target) && this.knockdown(caster, target, power);
    }

    public boolean castAtEntity(LivingEntity target, float power) {
        return this.validTargetList.canTarget(target) && this.knockdown(null, target, power);
    }

    private boolean knockdown(LivingEntity caster, LivingEntity target, float power) {


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

            if(!Rand.chanceOf(chance)) {
                return true;
            }
            if(!(caster instanceof Player player)) return false;
            double multiply = this.multiply;
            float power2 = modifyPower(caster, 1);
            if(powerAffectMultiply) multiply *= power2 + power -1;
            else multiply *= power2;
            PotionManager.effectGive(target, PotionEffectType.SLOW, 1 + Stat.STUN_DUR.getTotal(player) * multiply, 10);
            PotionManager.effectGive(target, PotionEffectType.JUMP, 1 + Stat.STUN_DUR.getTotal(player) * multiply, 128);
            this.playSpellEffects(caster, target);
            if(playEffect) {
                Msg.send(player, "&9*둔화");
                player.playSound(player, Sound.BLOCK_ANVIL_LAND, 0.7f, 0.5f);
            }
            return true;
        }
    }
}
