package me.rukon0621.guardians.addspells;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PainCauseEvent extends Event implements Cancellable {
    protected static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private double damage;
    private final LivingEntity caster;
    private final LivingEntity target;

    public PainCauseEvent(PainSpell spell, LivingEntity caster, LivingEntity target) {
        this.damage = spell.getDamage();
        this.caster = caster;
        this.target = target;
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = true;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
