package me.rukon0621.guardians.mythicAddon.machanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.PotionManager;
import me.rukon0621.guardians.main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

import static me.rukon0621.guardians.addspells.BlackOutSpell.getBlackOutPlayers;

@MythicMechanic(
        author = "Rukon",
        name = "dam",
        description = "Give Guardians plugin damage."
)
public class FlashbangMechanic extends SkillMechanic implements ITargetedEntitySkill {
    protected final int tick;

    public FlashbangMechanic(SkillExecutor manager, String file, MythicLineConfig mlc) {
        super(manager, new File(file), mlc.getLine(), mlc);
        this.tick = mlc.getInteger(new String[]{"duration", "d"}, 1);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        if (!target.isDead() && !(target.getHealth() <= 0.0D)) {
            if (skillMetadata.getCaster() instanceof ActiveMob am) {
                Entity entity = BukkitAdapter.adapt(target);
                if(entity instanceof Player le) {
                    new Repeater(le, tick);
                }
            }
            return SkillResult.SUCCESS;
        }
        return SkillResult.INVALID_TARGET;
    }
    static class Repeater extends BukkitRunnable {
        private final Player player;
        private int tick;

        public Repeater(Player player, int duration) {
            getBlackOutPlayers().add(player);
            this.player = player;
            this.tick = duration;
            PotionManager.effectGive(player, PotionEffectType.BLINDNESS, duration / 20D, 5);
            runTaskTimerAsynchronously(main.getPlugin(), 0, 1);
        }
        @Override
        public void run() {
            tick--;
            if(tick ==0 || player.isDead()) {
                getBlackOutPlayers().remove(player);
                cancel();
                return;
            }
            player.sendTitle(Msg.color("\uE000"), null, 0, 2, 3);
        }
    }
}
