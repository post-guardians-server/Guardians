package me.rukon0621.guardians.story;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class JavaActionEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final String[] actions;

    public JavaActionEvent(Player player, String[] actions) {
        this.player = player;
        this.actions = actions;
    }

    public Player getPlayer() {
        return player;
    }

    public String[] getActions() {
        return actions;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
