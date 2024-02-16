package me.rukon0621.guardians.GUI;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.helper.InvClass;
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

public class TrashcanGUI extends ItemSelectableWindow {
    List<ItemStack> items = new ArrayList<>();

    public TrashcanGUI(Player player) {
        super(player, "&f\uF000\uF036", 4);

        Button confirm = new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(!clickType.equals(ClickType.SHIFT_LEFT)) {
                    Msg.warn(player, "아이템을 버리려면 쉬프트 좌클릭하십시오.");
                    return;
                }
                items.clear();
                player.closeInventory();
                player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE,  1, .8f);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&c아이템 버리기");
                item.setCustomModelData(7);
                if(items.isEmpty()) {
                    item.addLore("&c버릴 아이템을 넣어주세요.");
                }
                else {
                    item.addLore("&e쉬프트 좌클릭&f하여 쓰레기통에 넣은 아이템을 모두 버립니다.");
                    item.addLore("&c버린 아이템은 되찾을 수 없습니다.");
                    item.addLore(" ");
                    item.addLore("&7이 창을 그냥 닫으면 아이템은 보존됩니다.");
                }
                return item.getItem();
            }
        };

        map.put(12, confirm);
        map.put(13, confirm);
        map.put(14, confirm);
        reloadGUI();
        open();
    }

    @Override
    public void select(int i) {
        int j;
        for(j = 0; j < (rows - 1) * 9; j++) {
            if(!map.containsKey(j)) break;
        }
        if(j == (rows - 1) * 9) {
            Msg.warn(player, "더이상 아이템을 넣을 수 없습니다.");
            return;
        }

        ItemStack item = player.getOpenInventory().getItem(i);
        ItemData itemData = new ItemData(item);
        if(itemData.isImportantItem() || itemData.isQuestItem()||itemData.getType().equals(LevelData.EXP_BOOK_TYPE_NAME)) {
            Msg.warn(player, "이 아이템은 버릴 수 없습니다.");
            return;
        }
        map.put(j, new TrashButton(item, j));
        player.getOpenInventory().setItem(i, new ItemStack(Material.AIR));
        reloadGUI();
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, .8f);
    }

    class TrashButton extends SelectedItemButton {
        protected TrashButton(ItemStack item, int slot) {
            super(item, slot);
            items.add(originalItem);
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            if(!InvClass.hasEnoughSpace(player.getInventory(), originalItem)) {
                Msg.warn(player, "인벤토리에 공간이 부족합니다.");
                return;
            }
            deleteButton();
            player.getInventory().addItem(originalItem);
            items.remove(originalItem);
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, .8f);
        }

        @Override
        public ItemStack getIcon() {
            ItemClass it = new ItemClass(originalItem.clone());
            it.addLore(" ");
            it.addLore("&7클릭하여 쓰레기통에서 다시 아이템을 뺍니다.");
            return it.getItem();
        }
    }

    @Override
    public void close(boolean b) {
        if(b) player.closeInventory();
        disable();
        MailBoxManager.giveAllOrMailAll(player, items);
    }
}
