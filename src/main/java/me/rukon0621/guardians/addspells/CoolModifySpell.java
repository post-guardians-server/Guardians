package me.rukon0621.guardians.addspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import me.rukon0621.guardians.helper.Msg;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CoolModifySpell extends InstantSpell {
    final String targetSpell;
    final float cooltime;
    final boolean usingProportion;
    final String msg;

    public CoolModifySpell(MagicConfig config, String spellName) {
        super(config, spellName);
        targetSpell = getConfigString("targetSpell", "anvil");
        cooltime = getConfigFloat("cool", 1);
        msg = getConfigString("msg", null);
        usingProportion = getConfigBoolean("usingProportion", false);
    }

    @Override
    public PostCastAction castSpell(LivingEntity livingEntity, SpellCastState spellCastState, float v, String[] strings) {
        Spell spell = MagicSpells.getSpellByInGameName(targetSpell);
        new BukkitRunnable() {
            @Override
            public void run() {
                if(msg != null) {
                    if(livingEntity instanceof Player player) {
                        Msg.send(player, msg);
                    }
                }
                if(usingProportion) spell.setCooldown(livingEntity, spell.getCooldown() * cooltime);
                else spell.setCooldown(livingEntity, cooltime);
            }
        }.runTaskLater(MagicSpells.plugin, 1);
        this.playSpellEffects(EffectPosition.CASTER, livingEntity);
        return PostCastAction.HANDLE_NORMALLY;
    }
}
