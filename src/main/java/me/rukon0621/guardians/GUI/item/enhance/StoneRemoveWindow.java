package me.rukon0621.guardians.GUI.item.enhance;

import me.rukon0621.guardians.GUI.item.SingleEquipmentSelectWindow;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.ItemGrade;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.StoneData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.gui.buttons.Button;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class StoneRemoveWindow extends SingleEquipmentSelectWindow {

    private static final int decreasingProportion = 30;
    private static long getPrice(int level, ItemGrade grade) {
        return StoneData.getGrantPrice(level, grade) * 2;
    }

    private final List<Integer> stoneSlots = new ArrayList<>();

    public StoneRemoveWindow(Player player) {
        super(player, "&f\uF000\uF03E", 5);
        reloadGUI();
        open();
    }

    @Override
    protected void reloadGUI() {
        for(int i : stoneSlots) {
            map.remove(i);
        }
        if(selectedEquipment != null) {
            ItemData itemData = new ItemData(selectedEquipment);
            int size = itemData.getAllStoneData().size();
            int slot = 31 - (size - 1);

            if(size == 6) {
                int[] slots = new int[]{28, 29, 30, 32, 33, 34};
                int index = 0;
                for(StoneData data : itemData.getAllStoneData()) {
                    slot = slots[index];
                    map.put(slot, new StoneIcon(itemData, data, index));
                    stoneSlots.add(slot);
                    index++;
                }
            }
            else {
                int index = 1;
                for(StoneData data : itemData.getAllStoneData()) {
                    map.put(slot, new StoneIcon(itemData, data, index));
                    stoneSlots.add(slot);
                    index++;
                    slot += 2;
                }
            }
        }
        super.reloadGUI();
    }

    private class StoneIcon extends Button {
        private final ItemData equipmentData;
        private final StoneData stoneData;
        private final long price;
        private final int indexBasedOne;

        public StoneIcon(ItemData equipmentData, StoneData stoneData, int indexBasedOne) {
            this.equipmentData = equipmentData;
            this.stoneData = stoneData;
            this.indexBasedOne = indexBasedOne;
            price = getPrice(equipmentData.getLevel(), stoneData.getGrade());
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            PlayerData pdc = new PlayerData(player);
            if(pdc.getMoney() < price) {
                Msg.warn(player, "돈이 부족합니다.");
                return;
            }
            pdc.setMoney(pdc.getMoney());
            StoneData st = equipmentData.getStoneData(indexBasedOne);
            if(st == null) {
                Msg.warn(player, "아다만트석 데이터가 더이상 존재하지 않습니다.");
                return;
            }
            StoneData newStone = new StoneData(st.getGrade(), st.getStat(), st.getValue() * (1 - decreasingProportion * 0.01D));
            equipmentData.removeStoneData(indexBasedOne);
            selectedEquipment = equipmentData.getItemStack();
            player.playSound(player, Sound.ITEM_TOTEM_USE, 0.8f, 1.5f);
            player.closeInventory();
            MailBoxManager.giveOrMail(player, newStone.getStoneItem().getItem());
            Msg.send(player, "아다만트석을 추출합니다.", pfix);
        }

        @Override
        public ItemStack getIcon() {
            ItemClass item = stoneData.getStoneItem();
            item.addLore(" ");
            item.addLore("&c추출 비용: " + price + "디나르");
            item.addLore("&f클릭해서 아다만트석을 추출합니다.");
            item.addLore("&7- 추출된 아다만트석의 레벨은 &b장비의 레벨과 동일&f해집니다.");
            item.addLore("&7- 본래의 힘을 일부 소실하여 아다만트석의 &c스텟 수치가 " + decreasingProportion + "% 감소&ㄹ합니다.");
            return item.getItem();
        }
    }

    @Override
    public int getItemSlot() {
        return 13;
    }
}
