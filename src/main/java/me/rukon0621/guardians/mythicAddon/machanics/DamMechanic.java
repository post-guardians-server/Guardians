package me.rukon0621.guardians.mythicAddon.machanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

@MythicMechanic(
        author = "Rukon",
        name = "dam",
        description = "Give Guardians plugin damage."
)
public class DamMechanic extends SkillMechanic implements ITargetedEntitySkill {
    protected final double multiplier;

    public DamMechanic(SkillExecutor manager, String file, MythicLineConfig mlc) {
        super(manager, new File(file), mlc.getLine(), mlc);
        this.multiplier = mlc.getDouble(new String[]{"multiplier", "m"}, 1);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {

        if (!target.isDead() && !(target.getHealth() <= 0.0D)) {

            if (skillMetadata.getCaster() instanceof ActiveMob am) {
                double damage = skillMetadata.getCaster().getEntity().getDamage() * multiplier;
                Entity entity = BukkitAdapter.adapt(target);


                if(entity instanceof LivingEntity le) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            le.setNoDamageTicks(0);
                            le.damage(damage, BukkitAdapter.adapt(am.getEntity()));
                        }
                    }.runTask(MythicBukkit.inst());
                    return SkillResult.SUCCESS;
                }
            }
            return SkillResult.INVALID_TARGET;
        }
        return SkillResult.INVALID_TARGET;
    }
}
