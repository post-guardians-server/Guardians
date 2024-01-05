package me.rukon0621.guardians.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GuardiansLoginEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final boolean proxyLogin;

    public GuardiansLoginEvent(Player player) {
        this(player, false);
    }
    public GuardiansLoginEvent(Player player, boolean proxyLogin) {
        this.player = player;
        this.proxyLogin = proxyLogin;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isProxyLogin() {
        return proxyLogin;
    }

    @Override
    public @NotNull
    HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
