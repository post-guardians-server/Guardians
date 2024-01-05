package me.rukon0621.guardians.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GuardiansDamageEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final LivingEntity attacker, victim;
    private boolean isCancelled;

    public GuardiansDamageEvent(LivingEntity attacker, LivingEntity victim) {
        this.attacker = attacker;
        this.victim = victim;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }
    public LivingEntity getVictim() {
        return victim;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
