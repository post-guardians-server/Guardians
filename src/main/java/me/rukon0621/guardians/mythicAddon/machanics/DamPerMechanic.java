package me.rukon0621.guardians.mythicAddon.machanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import me.rukon0621.guardians.addspells.InvulSpell;
import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.data.Stat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

@MythicMechanic(
        author = "Rukon",
        name = "damPer",
        description = "Give damage ignoring armor(pass damage event) by percentage health of target,"
)
public class DamPerMechanic extends SkillMechanic implements ITargetedEntitySkill {
    protected final double percent;

    public DamPerMechanic(SkillExecutor manager, String file, MythicLineConfig mlc) {
        super(manager, new File(file), mlc.getLine(), mlc);
        this.percent = mlc.getDouble(new String[]{"percent", "p"}, 0.05);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        if (!target.isDead() && !(target.getHealth() <= 0.0D)) {
            Entity entity = BukkitAdapter.adapt(target);
            if(entity instanceof LivingEntity le) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(InvulSpell.isInvincible(le)) return;
                        le.setNoDamageTicks(0);
                        double damage = (le.getMaxHealth() * percent);
                        if(le instanceof Player player) {
                            damage *= (1 - Stat.ABSOLUTE_ARMOR.getTotal(player));
                            BarManager.reloadBar(player, damage);
                        }
                        le.damage(damage);
                    }
                }.runTask(MythicBukkit.inst());
                return SkillResult.SUCCESS;
            }
            return SkillResult.INVALID_TARGET;
        }
        return SkillResult.INVALID_TARGET;
    }
}
