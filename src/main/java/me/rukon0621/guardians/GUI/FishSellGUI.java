package me.rukon0621.guardians.GUI;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.ItemSelectableWindow;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FishSellGUI extends ItemSelectableWindow {
    List<ItemStack> items = new ArrayList<>();

    private long price = 0;
    public FishSellGUI(Player player) {
        super(player, "&f\uF000\uF045", 4);

        Button confirm = new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(!clickType.equals(ClickType.SHIFT_LEFT)) {
                    Msg.warn(player, "아이템을 판매하려면 쉬프트 좌클릭하십시오.");
                    return;
                }
                items.clear();
                PlayerData pdc = new PlayerData(player);
                pdc.setMoney(pdc.getMoney() + price);
                Msg.send(player, "물고기를 모두 판매하여 " + price + "디나르를 획득하였습니다.");
                player.closeInventory();
                player.playSound(player, Sound.BLOCK_CHAIN_PLACE,  1, .8f);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME,  1, .8f);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&e낚시 포획물 판매하기");
                item.setCustomModelData(7);
                if(items.isEmpty()) {
                    item.addLore("&c판매할 아이템을 넣어주세요.");
                }
                else {
                    item.addLore("&e쉬프트 좌클릭&f하여 아이템을 모두 판매합니다.");
                    item.addLore("&7물고기의 비용은 등급, 품질, 레벨의 영향을 받습니다.");
                    item.addLore(" ");
                    item.addLore("&f판매 가격: &e" + price + " 디나르");
                }
                return item.getItem();
            }
        };

        map.put(30, confirm);
        map.put(31, confirm);
        map.put(32, confirm);
        reloadGUI();
        open();
    }

    @Override
    protected void reloadGUI() {
        price = 0;
        for(ItemStack item : items) {
            if(item.getType().equals(Material.AIR)) continue;
            price += main.getPlugin().getFishingManager().getFishPrice(new ItemData(item)) * item.getAmount();
        }
        super.reloadGUI();
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
        if(!(TypeData.getType(itemData.getType()).isMaterialOf("낚시 포획물") || itemData.getType().equals("쓰레기"))) {
            Msg.warn(player, "이 아이템은 낚시 포획물이 아닙니다.");
            return;
        }
        map.put(j, new FishButton(item, j));
        player.getOpenInventory().setItem(i, new ItemStack(Material.AIR));
        reloadGUI();
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, .8f);
    }

    class FishButton extends SelectedItemButton {
        protected FishButton(ItemStack item, int slot) {
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
            it.addLore("&7클릭하여 아이템을 철회합니다.");
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
