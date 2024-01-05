//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.rukon0621.guardians.addspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.shaded.org.apache.commons.util.FastMath;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.util.compat.EventUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AreaEffectSpell extends TargetedPowerSpell implements TargetedLocationSpell {
    private final List<Subspell> spells = new ArrayList<>();
    private List<String> spellNames = this.getConfigStringList("spells", new ArrayList<>());
    private final int maxTargets = this.getConfigInt("max-targets", 0);
    private final double cone = this.getConfigDouble("cone", 0.0D);
    private double vRadius = this.getConfigDouble("vertical-radius", 5.0D);
    private double hRadius = this.getConfigDouble("horizontal-radius", 10.0D);
    private final double vRadiusSquared;
    private final double hRadiusSquared;
    private final boolean pointBlank = this.getConfigBoolean("point-blank", true);
    private final boolean circleShape = this.getConfigBoolean("circle-shape", false);
    private final boolean useProximity = this.getConfigBoolean("use-proximity", false);
    private final boolean passTargeting = this.getConfigBoolean("pass-targeting", true);
    private final boolean failIfNoTargets = this.getConfigBoolean("fail-if-no-targets", true);
    private final boolean reverseProximity = this.getConfigBoolean("reverse-proximity", false);
    private final boolean spellSourceInCenter = this.getConfigBoolean("spell-source-in-center", false);
    private final boolean powerAffectRadius = this.getConfigBoolean("powerAffectRadius", false);

    public AreaEffectSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        if (this.vRadius > (double)MagicSpells.getGlobalRadius()) {
            this.vRadius = (double)MagicSpells.getGlobalRadius();
        }

        if (this.hRadius > (double)MagicSpells.getGlobalRadius()) {
            this.hRadius = (double)MagicSpells.getGlobalRadius();
        }

        this.vRadiusSquared = this.vRadius * this.vRadius;
        this.hRadiusSquared = this.hRadius * this.hRadius;
    }

    public void initialize() {
        super.initialize();
        if (this.spellNames != null && !this.spellNames.isEmpty()) {
            for (String spellName : this.spellNames) {
                Subspell spell = new Subspell(spellName);
                if (!spell.process()) {
                    MagicSpells.error("AreaEffectSpell '" + this.internalName + "' attempted to use invalid spell '" + spellName + "'");
                }
                else if (!spell.isTargetedLocationSpell() && !spell.isTargetedEntityFromLocationSpell() && !spell.isTargetedEntitySpell()) {
                    MagicSpells.error("AreaEffectSpell '" + this.internalName + "' attempted to use non-targeted spell '" + spellName + "'");
                }
                else {
                    this.spells.add(spell);
                }
            }
            this.spellNames.clear();
            this.spellNames = null;
        }
        else {
            MagicSpells.error("AreaEffectSpell '" + this.internalName + "' has no spells defined!");
        }
    }

    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            Location loc = null;
            if (this.pointBlank) {
                loc = caster.getLocation();
            } else {
                try {
                    Block block = this.getTargetedBlock(caster, power);
                    if (block != null && !BlockUtils.isAir(block.getType())) {
                        loc = block.getLocation().add(0.5D, 0.0D, 0.5D);
                    }
                } catch (IllegalStateException ignored) {
                }
            }

            if (loc == null) {
                return this.noTarget(caster);
            }

            SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, caster, loc, power);
            EventUtil.call(event);
            if (event.isCancelled()) {
                loc = null;
            } else {
                loc = event.getTargetLocation();
                power = event.getPower();
            }

            if (loc == null) {
                return this.noTarget(caster);
            }

            boolean done = this.doAoe(caster, loc, power);
            if (!done) {
                return this.noTarget(caster);
            }
        }

        return PostCastAction.HANDLE_NORMALLY;
    }

    public boolean castAtLocation(LivingEntity caster, Location target, float power) {
        return this.doAoe(caster, target, power);
    }

    public boolean castAtLocation(Location target, float power) {
        return this.doAoe((LivingEntity)null, target, power);
    }

    private void castSpells(LivingEntity caster, Location location, LivingEntity target, float power) {

        for (Subspell spell : this.spells) {
            if (this.spellSourceInCenter && spell.isTargetedEntityFromLocationSpell()) {
                spell.castAtEntityFromLocation(caster, location, target, power, this.passTargeting);
            }
            else if (caster != null && spell.isTargetedEntityFromLocationSpell()) {
                spell.castAtEntityFromLocation(caster, caster.getLocation(), target, power, this.passTargeting);
            }
            else if (spell.isTargetedEntitySpell()) {
                spell.castAtEntity(caster, target, power, this.passTargeting);
            }
            else if (spell.isTargetedLocationSpell()) {
                spell.castAtLocation(caster, target.getLocation(), power);
            }
        }

    }

    private boolean doAoe(LivingEntity caster, Location location, float basePower) {
        int count = 0;
        Location finalLoc = caster != null ? caster.getLocation() : location;
        location = Util.makeFinite(location);
        if (this.validTargetList.canTargetOnlyCaster()) {
            if (!caster.getWorld().equals(finalLoc.getWorld())) {
                return false;
            }
            else {
                double hDistance = NumberConversions.square(caster.getLocation().getX() - location.getX()) + NumberConversions.square(caster.getLocation().getZ() - location.getZ());
                if (hDistance > this.hRadiusSquared) {
                    return false;
                } else {
                    double vDistance = NumberConversions.square(caster.getLocation().getY() - location.getY());
                    if (vDistance > this.vRadiusSquared) {
                        return false;
                    } else {
                        SpellTargetEvent event = new SpellTargetEvent(this, caster, caster, basePower);
                        EventUtil.call(event);
                        if (event.isCancelled()) {
                            return false;
                        } else {
                            LivingEntity target = event.getTarget();
                            float power = event.getPower();
                            this.castSpells(caster, location, target, power);
                            this.playSpellEffects(EffectPosition.TARGET, target);
                            this.playSpellEffects(EffectPosition.SPECIAL, location);
                            if (this.spellSourceInCenter) {
                                this.playSpellEffectsTrail(location, target.getLocation());
                            } else {
                                this.playSpellEffectsTrail(caster.getLocation(), target.getLocation());
                            }

                            return true;
                        }
                    }
                }
            }
        }
        else {



            float power2 = modifyPower(caster, 1);

            double radiusH = hRadius;
            double radiusV = vRadius;

            if(powerAffectRadius) {
                radiusH *= basePower-1+power2;
                radiusV *= basePower-1+power2;
            }
            else {
                radiusH *= power2;
                radiusV *= power2;
            }

            List<LivingEntity> entities = new ArrayList<>(location.getWorld().getNearbyLivingEntities(location, radiusH, radiusV, radiusH));

            if (this.useProximity) {
                entities.removeIf(ent -> !ent.getWorld().equals(finalLoc.getWorld()));

                Comparator<LivingEntity> comparator = Comparator.comparingDouble((entity) -> entity.getLocation().distanceSquared(finalLoc));
                if (this.reverseProximity) {
                    comparator = comparator.reversed();
                }
                entities.sort(comparator);
            }

            for(LivingEntity e : entities) {
                if (this.circleShape) {
                    double hDistance = NumberConversions.square(e.getLocation().getX() - location.getX()) + NumberConversions.square(e.getLocation().getZ() - location.getZ());
                    if (hDistance > this.hRadiusSquared) {
                        continue;
                    }

                    double vDistance = NumberConversions.square(e.getLocation().getY() - location.getY());
                    if (vDistance > this.vRadiusSquared) {
                        continue;
                    }
                }

                if (this.pointBlank && this.cone > 0.0D) {
                    Vector dir = e.getLocation().toVector().subtract(finalLoc.toVector());
                    if (FastMath.toDegrees(FastMath.abs(dir.angle(finalLoc.getDirection()))) > this.cone) {
                        continue;
                    }
                }

                if (!e.isDead() && (caster != null || this.validTargetList.canTarget(e)) && (caster == null || this.validTargetList.canTarget(caster, e))) {
                    SpellTargetEvent event = new SpellTargetEvent(this, caster, e, basePower);
                    EventUtil.call(event);
                    if (!event.isCancelled()) {
                        LivingEntity target = event.getTarget();
                        float power = event.getPower();
                        this.castSpells(caster, location, target, power);
                        this.playSpellEffects(EffectPosition.TARGET, target);
                        if (this.spellSourceInCenter) {
                            this.playSpellEffectsTrail(location, target.getLocation());
                        } else if (caster != null) {
                            this.playSpellEffectsTrail(caster.getLocation(), target.getLocation());
                        }

                        ++count;
                        if (this.maxTargets > 0 && count >= this.maxTargets) {
                            break;
                        }
                    }
                }
            }


            boolean success = count > 0 || !this.failIfNoTargets;
            if (success) {
                this.playSpellEffects(EffectPosition.SPECIAL, location);
                if (caster != null) {
                    this.playSpellEffects(EffectPosition.CASTER, caster);
                }
            }

            return success;
        }
    }
}
