package me.rukon0621.guardians.GUI.crafting;

import me.rukon0621.guardians.craft.craft.WaitingItem;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.DateUtil;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.Window;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Date;

import static me.rukon0621.guardians.main.pfix;

public class CraftAcceleratingWindow extends Window {

    private final ItemStack usedItem;
    private final int minute;

    public CraftAcceleratingWindow(Player player, ItemStack usedItem, int minute) {
        super(player, "&f\uF000", 1);
        this.usedItem = usedItem;
        this.minute = minute;
        int slot = 0;
        PlayerData pdc = new PlayerData(player);
        for(WaitingItem item : pdc.getWaitingItems()) {
            map.put(slot, new WaitingItemButton(item));
            slot++;
        }
        reloadGUI();
        open();
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) player.closeInventory();
    }

    class WaitingItemButton extends Button {

        private final WaitingItem waitingItem;

        public WaitingItemButton(WaitingItem waitingItem) {
            this.waitingItem = waitingItem;
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            waitingItem.setEndTime(new Date(System.currentTimeMillis() + Math.max(0L, waitingItem.getRemainTime() - (minute * 60000L))));
            usedItem.setAmount(usedItem.getAmount() - 1);
            player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);
            player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, 1, 1.2f);
            player.closeInventory();
            Msg.send(player, "제작 가속기를 사용했습니다!", pfix);
        }

        @Override
        public ItemStack getIcon() {
            ItemClass item = new ItemClass(waitingItem.getResult().clone());
            item.addLore(" ");
            item.addLore("&6\uE004\uE00C\uE00C남은 시간: " + DateUtil.formatDate(waitingItem.getRemainTime()/1000));
            item.addLore(" ");
            item.addLore("&e\uE011\uE00C\uE00C클릭하여 이 아이템에 제작 가속기를 사용합니다.");
            return item.getItem();
        }
    }
}
