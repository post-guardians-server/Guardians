package me.rukon0621.guardians.addspells.triggers;

import com.nisovin.magicspells.spells.passive.util.PassiveListener;
import com.nisovin.magicspells.util.OverridePriority;
import me.rukon0621.guardians.events.GuardiansDamageEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;

public class GiveGuardiansDamageListener extends PassiveListener {
    @Override
    public void initialize(String s) {

    }

    @EventHandler
    @OverridePriority
    public void onDamaged(GuardiansDamageEvent e) {
        if (isCancelStateOk(e.isCancelled())) {
            LivingEntity attacker = e.getAttacker();
            if (hasSpell(attacker) && this.canTrigger(attacker)) {
                boolean casted = this.passiveSpell.activate(attacker, e.getVictim());
                if (cancelDefaultAction(casted)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
