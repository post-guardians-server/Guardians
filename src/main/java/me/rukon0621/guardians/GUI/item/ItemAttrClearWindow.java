package me.rukon0621.guardians.GUI.item;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.ItemSelectableWindow;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemAttrClearWindow extends ItemSelectableWindow {
    private static final int[] itemSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8};

    public ItemAttrClearWindow(Player player) {
        super(player, "&f\uF000\uF029", 2);
        Button button = new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if (ItemAttrClearWindow.this.getItems().isEmpty()) {
                    Msg.warn(player, "아이템을 넣어주세요.");
                    return;
                }
                /*
                int price = calculateCost();
                PlayerData pdc = new PlayerData(player);
                if(pdc.getMoney() < price) {
                    Msg.warn(player, "디나르가 부족해 속성 제거를 진행할 수 없습니다.");
                    return;
                }
                pdc.setMoney(pdc.getMoney() - price);
                 */

                List<ItemStack> items = new ArrayList<>();
                for(int i : itemSlots) {
                    if(!map.containsKey(i)) continue;
                    ItemData itemData = new ItemData(map.get(i).getIcon());
                    for(String attr : new ArrayList<>(itemData.getAttrs())) {
                        itemData.setAttr(attr, 0);
                    }
                    items.add(itemData.getItemStack());
                    map.remove(i);
                }
                MailBoxManager.giveAllOrMailAll(player, items);
                reloadGUI();
                player.playSound(player, Sound.BLOCK_SMITHING_TABLE_USE, 1, 1);
                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.5f);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&e[ &6속성 제거 진행하기 &e]");
                item.setCustomModelData(7);

                if (ItemAttrClearWindow.this.getItems().isEmpty()) {
                    item.addLore("&f아이템을 넣어주세요.");
                }
                return item.getItem();
            }
        };
        map.put(12, button);
        map.put(13, button);
        map.put(14, button);
        reloadGUI();
        open();
    }

    private int calculateCost() {
        int price = 0;
        for(ItemStack item : getItems()) {
            ItemData data = new ItemData(item);
            int quality = data.getAttrQuality() % 1000;
            price += item.getAmount() * (quality * 80 + data.getLevel() * 30 + 30);
        }
        return price;
    }

    private List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        for(int slot : itemSlots) {
            if(map.containsKey(slot)) {
                items.add(map.get(slot).getIcon());
            }
        }
        return items;
    }

    @Override
    public void select(int i) {
        int availableSlot = -1;
        for(int l : itemSlots) {
            if(map.containsKey(l)) continue;
            availableSlot = l;
            break;
        }
        if(availableSlot==-1) {
            Msg.warn(player, "더이상 아이템을 넣을 수 없습니다.");
            return;
        }
        inputItem(availableSlot, player.getOpenInventory().getItem(i));
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) player.closeInventory();
        List<ItemStack> items = new ArrayList<>();
        for(int i : itemSlots) {
            if(map.containsKey(i)) {
                items.add(map.get(i).getIcon());
            }
        }
        MailBoxManager.giveAllOrMailAll(player, items);
    }
}
