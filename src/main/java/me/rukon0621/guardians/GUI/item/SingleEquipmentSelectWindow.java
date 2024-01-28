package me.rukon0621.guardians.GUI.item;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.gui.windows.ItemSelectableWindow;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public abstract class   SingleEquipmentSelectWindow extends ItemSelectableWindow {
    protected ItemStack selectedEquipment = null;

    public SingleEquipmentSelectWindow(Player player, String title, int rows) {
        super(player, title, rows);
    }

    public abstract int getItemSlot();
    @Override
    public void close(boolean b) {
        disable();
        if(inv.getSlot(getItemSlot())!=null) {
            MailBoxManager.giveOrMail(player, inv.getSlot(getItemSlot()));
        }
        if(b) player.closeInventory();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent e) {
        if(!e.getPlayer().equals(player)) return;
        close(false);
    }

    @Override
    public void select(int i) {
        ItemData itemData = new ItemData(player.getOpenInventory().getItem(i));
        if(!itemData.isEquipment()||itemData.getType().equals("사증")||itemData.getType().equals("룬")) {
            return;
        }
        if(inv.getSlot(getItemSlot())!=null) {
            Msg.warn(player, "기존에 장착된 아이템을 제거해주세요.");
            return;
        }
        if(player.getOpenInventory().getItem(i) != null && player.getOpenInventory().getItem(i).getAmount() != 1) {
            Msg.warn(player, "한 번에 여러개의 아이템을 넣을 수 없습니다.");
            return;
        }
        map.put(getItemSlot(), new EquipmentButton(player.getOpenInventory().getItem(i), getItemSlot()) {
        });
        player.getOpenInventory().setItem(i, null);
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
        reloadGUI();
    }

    protected class EquipmentButton extends SelectedItemButton {

        protected EquipmentButton(ItemStack item, int slot) {
            super(item, slot);
            selectedEquipment = originalItem;
        }

        @Override
        public void setOriginalItem(ItemStack originalItem) {
            selectedEquipment = originalItem;
            super.setOriginalItem(originalItem);
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
            MailBoxManager.giveOrMail(player, originalItem);
            deleteButton();
        }

        @Override
        protected void deleteButton() {
            super.deleteButton();
            selectedEquipment = null;
        }

        @Override
        public ItemStack getIcon() {
            return originalItem;
        }
    }
}
