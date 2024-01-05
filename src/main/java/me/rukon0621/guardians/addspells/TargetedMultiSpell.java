//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.rukon0621.guardians.addspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.RegexUtil;
import com.nisovin.magicspells.util.TargetInfo;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import me.rukon0621.guardians.main;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class TargetedMultiSpell extends TargetedSpell implements TargetedEntitySpell, TargetedLocationSpell {
    private static final Pattern DELAY_PATTERN = Pattern.compile("DELAY [0-9]+");
    private final Random random = ThreadLocalRandom.current();
    private final List<TargetedMultiSpell.Action> actions = new ArrayList<>();
    private List<String> spellList = this.getConfigStringList("spells", null);
    private final float yOffset = this.getConfigFloat("y-offset", 0.0F);
    private final int hitDelay = this.getConfigInt("hit-delay", 0);
    private final boolean pointBlank = this.getConfigBoolean("point-blank", false);
    private final boolean stopOnFail = this.getConfigBoolean("stop-on-fail", true);
    private final boolean passTargeting = this.getConfigBoolean("pass-targeting", true);
    private final boolean requireEntityTarget = this.getConfigBoolean("require-entity-target", false);
    private final boolean castRandomSpellInstead = this.getConfigBoolean("cast-random-spell-instead", false);

    private final Map<LivingEntity, LivingEntity> hitDelayMap = new HashMap<>();

    public TargetedMultiSpell(MagicConfig config, String spellName) {
        super(config, spellName);
    }

    public void initialize() {
        super.initialize();
        if (this.spellList != null) {

            for (String s : this.spellList) {
                if (RegexUtil.matches(DELAY_PATTERN, s)) {
                    int delay = Integer.parseInt(s.split(" ")[1]);
                    this.actions.add(new Action(delay));
                }
                else {
                    Subspell spell = new Subspell(s);
                    if (spell.process()) {
                        this.actions.add(new Action(spell));
                    }
                    else {
                        MagicSpells.error("TargetedMultiSpell '" + this.internalName + "' has an invalid spell '" + s + "' defined!");
                    }
                }
            }

            this.spellList = null;
        }
    }

    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            Location locTarget = null;
            LivingEntity entTarget = null;
            if (this.requireEntityTarget) {
                TargetInfo<LivingEntity> info = this.getTargetedEntity(caster, power);
                if (info != null) {
                    entTarget = info.getTarget();
                    power = info.getPower();
                }
            } else if (this.pointBlank) {
                locTarget = caster.getLocation();
            } else {
                try {
                    Block b = this.getTargetedBlock(caster, power);
                    if (b != null && !BlockUtils.isAir(b.getType())) {
                        locTarget = b.getLocation();
                        locTarget.add(0.5D, 0.0D, 0.5D);
                    }
                } catch (IllegalStateException var9) {
                    DebugHandler.debugIllegalState(var9);
                }
            }

            if (locTarget == null && entTarget == null) {
                return this.noTarget(caster);
            }

            if (locTarget != null) {
                locTarget.setY(locTarget.getY() + (double)this.yOffset);
                locTarget.setDirection(caster.getLocation().getDirection());
            }

            boolean somethingWasDone = this.runSpells(caster, entTarget, locTarget, power);
            if (!somethingWasDone) {
                return this.noTarget(caster);
            }

            if (entTarget != null) {
                this.sendMessages(caster, entTarget, args);
                return PostCastAction.NO_MESSAGES;
            }
        }

        return PostCastAction.HANDLE_NORMALLY;
    }

    public boolean castAtLocation(LivingEntity caster, Location target, float power) {
        return this.runSpells(caster, null, target.clone().add(0.0D, this.yOffset, 0.0D), power);
    }

    public boolean castAtLocation(Location location, float power) {
        return this.runSpells(null, null, location.clone().add(0.0D, this.yOffset, 0.0D), power);
    }

    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        return this.runSpells(caster, target, null, power);
    }

    public boolean castAtEntity(LivingEntity target, float power) {
        return this.runSpells(null, target, null, power);
    }

    private boolean runSpells(LivingEntity livingEntity, LivingEntity entTarget, Location locTarget, float power) {
        if(hitDelay > 0) {
            if(hitDelayMap.containsKey(entTarget)) return false;
            hitDelayMap.put(entTarget, livingEntity);
            new BukkitRunnable() {
                @Override
                public void run() {
                    hitDelayMap.remove(entTarget);
                }
            }.runTaskLaterAsynchronously(main.getPlugin(), hitDelay);
        }
        boolean somethingWasDone = false;
        if (!this.castRandomSpellInstead) {
            int delay = 0;
            ArrayList<DelayedSpell> delayedSpells = new ArrayList<>();
            for (Action action : this.actions) {
                if (action.isDelay()) {
                    delay += action.getDelay();
                }
                else if (action.isSpell()) {
                    Subspell spell = action.getSpell();
                    if (delay == 0) {
                        boolean ok = this.castTargetedSpell(spell, livingEntity, entTarget, locTarget, power);
                        if (ok) {
                            somethingWasDone = true;
                        }
                        else if (this.stopOnFail) {
                            break;
                        }
                    }
                    else {
                        DelayedSpell ds = new DelayedSpell(spell, livingEntity, entTarget, locTarget, power, delayedSpells);
                        delayedSpells.add(ds);
                        MagicSpells.scheduleDelayedTask(ds, delay);
                        somethingWasDone = true;
                    }
                }
            }
        }
        else {
            TargetedMultiSpell.Action action = this.actions.get(this.random.nextInt(this.actions.size()));
            if (action.isSpell()) {
                somethingWasDone = this.castTargetedSpell(action.getSpell(), livingEntity, entTarget, locTarget, power);
            }
        }

        if (somethingWasDone) {
            if (livingEntity != null) {
                if (entTarget != null) {
                    this.playSpellEffects(livingEntity, entTarget);
                }
                else if (locTarget != null) {
                    this.playSpellEffects(livingEntity, locTarget);
                }
            }
            else if (entTarget != null) {
                this.playSpellEffects(EffectPosition.TARGET, entTarget);
            }
            else if (locTarget != null) {
                this.playSpellEffects(EffectPosition.TARGET, locTarget);
            }
        }
        return somethingWasDone;
    }

    private boolean castTargetedSpell(Subspell spell, LivingEntity caster, LivingEntity entTarget, Location locTarget, float power) {
        if (spell.isTargetedEntitySpell() && entTarget != null) {
            return spell.castAtEntity(caster, entTarget, power, this.passTargeting);
        }
        else {
            if (spell.isTargetedLocationSpell()) {
                if (entTarget != null) {
                    return spell.castAtLocation(caster, entTarget.getLocation(), power);
                }

                if (locTarget != null) {
                    return spell.castAtLocation(caster, locTarget, power);
                }
            }

            PostCastAction action = spell.cast(caster, power);
            return action == PostCastAction.HANDLE_NORMALLY || action == PostCastAction.NO_MESSAGES;
        }
    }

    private static class Action {
        private final Subspell spell;
        private final int delay;

        private Action(Subspell spell) {
            this.spell = spell;
            this.delay = 0;
        }

        private Action(int delay) {
            this.delay = delay;
            this.spell = null;
        }

        public boolean isSpell() {
            return this.spell != null;
        }

        public Subspell getSpell() {
            return this.spell;
        }

        public boolean isDelay() {
            return this.delay > 0;
        }

        public int getDelay() {
            return this.delay;
        }
    }

    private class DelayedSpell implements Runnable {
        private final Subspell spell;
        private final LivingEntity caster;
        private final LivingEntity entTarget;
        private final Location locTarget;
        private final float power;
        private List<TargetedMultiSpell.DelayedSpell> delayedSpells;
        private boolean cancelled;

        private DelayedSpell(Subspell spell, LivingEntity caster, LivingEntity entTarget, Location locTarget, float power, List<TargetedMultiSpell.DelayedSpell> delayedSpells) {
            this.spell = spell;
            this.caster = caster;
            this.entTarget = entTarget;
            this.locTarget = locTarget;
            this.power = power;
            this.delayedSpells = delayedSpells;
            this.cancelled = false;
        }

        public void cancel() {
            this.cancelled = true;
            this.delayedSpells = null;
        }

        public void cancelAll() {

            for (DelayedSpell ds : this.delayedSpells) {
                if (ds != this) {
                    ds.cancel();
                }
            }

            this.delayedSpells.clear();
            this.cancel();
        }

        public void run() {
            if (this.cancelled) {
                this.delayedSpells = null;
            } else {
                if (this.caster != null && !this.caster.isValid()) {
                    this.cancelAll();
                } else {
                    boolean ok = TargetedMultiSpell.this.castTargetedSpell(this.spell, this.caster, this.entTarget, this.locTarget, this.power);
                    this.delayedSpells.remove(this);
                    if (!ok && TargetedMultiSpell.this.stopOnFail) {
                        this.cancelAll();
                    }
                }

                this.delayedSpells = null;
            }
        }
    }
}
