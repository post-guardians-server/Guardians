package me.rukon0621.guardians.dropItem.window;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.dropItem.DropManager;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.gui.buttons.Icon;
import me.rukon0621.gui.windows.Window;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class DropShowWindow extends Window {
    private static final main plugin = main.getPlugin();


    public DropShowWindow(Player player, String dropName, int dropLevel) {
        super(player, "&f\uF000", 1);
        new BukkitRunnable() {
            @Override
            public void run() {
                List<ItemStack> original = DropManager.getDropList(player, dropName, dropLevel, 10000000, true);
                List<ItemStack> items = new ArrayList<>();
                PlayerData pdc = new PlayerData(player);

                //같은 종류의 아이템 합치기
                for(ItemStack item : original) {
                    boolean notSearched = true;
                    ItemData itemData = new ItemData(item);
                    itemData.setLevel(0);
                    item = itemData.getItemStack();
                    for(ItemStack loopItem : items) {
                        if(!item.isSimilar(loopItem)) continue;
                        notSearched = false;
                        break;
                    }
                    if(notSearched) {
                        item.setAmount(1);
                        items.add(item);
                    }
                }

                for(ItemStack item : items) {
                    if(item.getItemMeta().getDisplayName().contains("청사진: ")) {
                        String bluePrintName = Msg.uncolor(item.getItemMeta().getDisplayName()).split(": ")[1].trim();
                        if(!pdc.hasBlueprint(bluePrintName)) {
                            ItemMeta itemMeta = item.getItemMeta();
                            itemMeta.setDisplayName(Msg.color("&7알 수 없는 청사진"));
                            item.setItemMeta(itemMeta);
                        }
                    }
                }

                inv = new InvClass((items.size() - 1) / 9 + 1, "&f\uF000");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        int slot = 0;
                        for(ItemStack item : items) {
                            map.put(slot, new Icon() {
                                @Override
                                public ItemStack getIcon() {
                                    return item;
                                }
                            });
                            slot++;
                        }
                        reloadGUI();
                        open();
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) player.closeInventory();
    }
}
