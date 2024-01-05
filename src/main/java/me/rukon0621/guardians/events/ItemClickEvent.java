package me.rukon0621.guardians.events;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.helper.Msg;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemClickEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled;
    private final ItemData itemData;
    private final ItemStack item;
    private final Player player;

    public ItemClickEvent(Player player, ItemStack item) {
        this.item = item;
        this.itemData = new ItemData(item);
        this.player = player;
    }



    @Deprecated
    public ItemClickEvent(Player player, ItemData itemData) {
        item = player.getInventory().getItemInMainHand();
        this.itemData = itemData;
        this.player = player;
    }

    public ItemData getItemData() {
        return itemData;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean consume() {
        if(!player.getInventory().getItemInMainHand().equals(item)) {
            Msg.warn(player, "손에 아이템을 들어주세요.");
            return false;
        }
        item.setAmount(item.getAmount() - 1);
        return true;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
