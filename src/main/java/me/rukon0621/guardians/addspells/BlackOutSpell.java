package me.rukon0621.guardians.addspells;
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.PotionManager;
import me.rukon0621.guardians.main;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BlackOutSpell extends TargetedSpell implements TargetedEntitySpell {
    private final int duration;
    private final String title;
    private static final List<Player> blackOut = new ArrayList<>();

    public static List<Player> getBlackOutPlayers() {
        return blackOut;
    }

    public BlackOutSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        title = StringEscapeUtils.unescapeJava(config.getString("title", "\uE000"));
        duration = config.getInt("duration", 20);
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState spellCastState, float power, String[] args) {
        if(!spellCastState.equals(SpellCastState.NORMAL)) return PostCastAction.NO_MESSAGES;
        TargetInfo<LivingEntity> target = this.getTargetedEntity(caster, power);
        if(target==null) return this.noTarget(caster);
        blackOut(target.getTarget(), power);
        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        return this.validTargetList.canTarget(caster, target) && blackOut(target, power);
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float v) {
        return this.validTargetList.canTarget(target) && blackOut(target, v);
    }

    private boolean blackOut(LivingEntity target, float power) {
        if(target==null||target.isDead()||!(target instanceof Player player)) return true;
        new Repeater(player, (int) (duration * power));
        return true;
    }

    class Repeater extends BukkitRunnable {
        private final Player player;
        private int duration;

        public Repeater(Player player, int duration) {
            blackOut.add(player);
            this.player = player;
            this.duration = duration;
            PotionManager.effectGive(player, PotionEffectType.BLINDNESS, duration / 20D, 5);
            runTaskTimer(main.getPlugin(), 0, 1);
        }
        @Override
        public void run() {
            duration--;
            if(duration==0 || player.isDead()) {
                blackOut.remove(player);
                cancel();
                return;
            }
            player.sendTitle(Msg.color(title), null, 0, 2, 3);
        }
    }

}
