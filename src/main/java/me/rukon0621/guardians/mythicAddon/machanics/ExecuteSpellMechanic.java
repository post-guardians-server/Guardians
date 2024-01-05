package me.rukon0621.guardians.mythicAddon.machanics;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import me.rukon0621.guardians.main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class ExecuteSpellMechanic implements ITargetedEntitySkill {
    protected final float power;
    protected final float powerPerLevel;
    protected final String spellName;
    protected final boolean facing;
    protected final boolean castAtTarget;

    public ExecuteSpellMechanic(MythicLineConfig config) {
        this.spellName = config.getString("spell");
        this.power = config.getFloat(new String[]{"power", "p"}, 1);
        this.powerPerLevel = config.getFloat(new String[]{"powerperlevel", "pp"}, 0f);
        this.facing = config.getBoolean(new String[]{"facing", "fc"}, true);
        castAtTarget = config.getBoolean(new String[]{"cat", "castAtTarget"}, false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        Spell spell = MagicSpells.getSpellByInGameName(spellName);
        if(spell==null) return SkillResult.ERROR;

        if(castAtTarget) {
            Entity e = BukkitAdapter.adapt(abstractEntity);
            if(e instanceof LivingEntity le) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        spell.cast(le, (float) (power * (1+powerPerLevel*(skillMetadata.getCaster().getLevel()-1))), new String[]{});
                    }
                }.runTask(main.getPlugin());
                return SkillResult.SUCCESS;
            }
            return SkillResult.INVALID_TARGET;
        }

        if(BukkitAdapter.adapt(skillMetadata.getCaster().getEntity()) instanceof LivingEntity le) {

            new BukkitRunnable() {
                public void run() {
                    if(facing) {
                        if(abstractEntity.getTarget()!=null) {
                            AbstractVector vector = abstractEntity.getLocation().toVector().subtract(abstractEntity.getTarget().getLocation().toVector()).multiply(-1);
                            AbstractLocation location = abstractEntity.getLocation();
                            location.setDirection(vector);
                            abstractEntity.teleport(location);
                        }
                    }
                    if(spell instanceof TargetedEntitySpell targetedEntitySpell) {
                        if(BukkitAdapter.adapt(abstractEntity.getTarget()) instanceof LivingEntity target) {
                            targetedEntitySpell.castAtEntity(le, target, (float) (power * (1+powerPerLevel*(skillMetadata.getCaster().getLevel()-1))));
                        }
                    }
                    else {
                        spell.cast(le, (float) (power * (1+powerPerLevel*(skillMetadata.getCaster().getLevel()-1))), new String[]{});
                    }
                }
            }.runTask(main.getPlugin());
            return SkillResult.SUCCESS;
        }
        return SkillResult.INVALID_TARGET;
    }
}
