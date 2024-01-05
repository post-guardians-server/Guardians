package me.rukon0621.guardians.mythicAddon.machanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import me.rukon0621.guardians.story.CameraShaking;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;

@MythicMechanic(
        author = "Rukon",
        name = "dam",
        description = "Give Guardians plugin damage."
)
public class ShakeMechanic extends SkillMechanic implements ITargetedEntitySkill {
    protected final double strength;
    protected final int duration;

    public ShakeMechanic(SkillExecutor manager, String file, MythicLineConfig mlc) {
        super(manager, new File(file), mlc.getLine(), mlc);
        this.duration = mlc.getInteger(new String[]{"duration", "d"}, 1);
        this.strength = mlc.getDouble(new String[]{"strength", "s"}, 1);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {

        if (!target.isDead() && !(target.getHealth() <= 0.0D)) {

            Entity entity = BukkitAdapter.adapt(target);
            if(entity instanceof Player player) {
                new CameraShaking(player, duration, strength, false);
                return SkillResult.SUCCESS;
            }
            return SkillResult.INVALID_TARGET;
        }
        return SkillResult.INVALID_TARGET;
    }
}
