package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.mailbox.MailBoxManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class InvClass {
    private Inventory inv;
    private static Set<Player> messageBlock = new HashSet<>();

    public InvClass(int rows, String invname) {
        this.inv = Bukkit.createInventory(null, rows*9, Msg.color(invname));
    }
    public InvClass(Inventory inv) {
        this.inv = inv;
    }


    public Inventory getInv() {
        return this.inv;
    }

    public void setInv(Inventory inv) {
        this.inv = inv;
    }

    public ItemStack setslot(int slot, ItemStack item) {
        ItemStack previousItem = inv.getItem(slot);
        inv.setItem(slot, item);
        return previousItem;
    }
    public ItemStack getSlot(int slot) {
        return getInv().getItem(slot);
    }
    public ArrayList<ItemStack> getAllItemsInInventory() {
        ArrayList<ItemStack> items = new ArrayList<>();
        int size = getInv().getSize();
        for (int i = 0; i < size; i++) {
            if (getSlot(i) == null) continue;
            items.add(getSlot(i));
        }
        return items;
    }

    public void fill(ItemStack item) {
        for(int i = 0;i<inv.getSize();i++) {
            inv.setItem(i, item);
        }
    }

    //Check if player has enough space for specific ItemStack
    public static boolean hasEnoughSpace(Inventory inv, ItemStack targetItem) {
        for(ItemStack item : inv.getStorageContents()) {
            if (item==null||item.getType().equals(Material.AIR)) return true;
            if (item.isSimilar(targetItem)) {
                if(item.getType().getMaxStackSize() - item.getAmount() >= targetItem.getAmount()) return true;
            }
        }
        return false;
    }

    public static boolean giveOrDrop(Player player, ItemStack item) {
        return MailBoxManager.giveOrMail(player, item);
    }



    @Deprecated
    //Check if player has specific amount of item.
    public static boolean hasItem(Player player, ItemStack item) {
        int amount = 0;
        for(ItemStack i : player.getInventory().getContents()) {
            if(i==null) continue;
            if(i.getItemMeta().equals(item.getItemMeta())&&i.getType().equals(item.getType())) {
                amount += i.getAmount();
            }
        }
        return amount >= item.getAmount();
    }

    @Deprecated
    public static boolean hasItem(Player player, ItemStack item, ArrayList<ItemStack> items) {
        int amount = 0;
        for(ItemStack i : items) {
            if(i==null) continue;
            if(i.getItemMeta().equals(item.getItemMeta())&&i.getType().equals(item.getType())) {
                amount += i.getAmount();
            }
        }
        return amount >= item.getAmount();
    }

    @Deprecated
    public static boolean removeItem(Player player, ItemStack item) {
        for(ItemStack i : player.getInventory().getContents()) {
            if(i==null) continue;
            if(i.getItemMeta().equals(item.getItemMeta())&&i.getType().equals(item.getType())) {
                if(i.getAmount()<item.getAmount()) continue;
                i.setAmount(i.getAmount()-item.getAmount());
                return true;
            }
        }
        return false;
    }


    @Deprecated
    public static boolean removeItem(Player player, ItemStack item, ArrayList<ItemStack> items) {
        for(ItemStack i : items) {
            if(i==null) continue;
            if(i.getItemMeta().equals(item.getItemMeta())&&i.getType().equals(item.getType())) {
                if(i.getAmount()<item.getAmount()) continue;
                i.setAmount(i.getAmount()-item.getAmount());
                return true;
            }
        }
        return false;
    }
}
