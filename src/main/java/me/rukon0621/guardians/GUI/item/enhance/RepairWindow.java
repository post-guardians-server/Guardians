package me.rukon0621.guardians.GUI.item.enhance;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.Window;
import me.rukon0621.pay.PaymentData;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static me.rukon0621.guardians.main.pfix;

public class RepairWindow extends Window {
    private static final int runar = 49;
    public RepairWindow(Player player, ItemStack item) {
        super(player, "&f\uF000", 1);
        map.put(11, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(player.getInventory().getItemInMainHand().equals(item)) {
                    Msg.warn(player, "손에 아이템을 들고 있어야합니다.");
                    return;
                }
                else if(player.getInventory().getItemInMainHand().getAmount() != 1) {
                    Msg.warn(player, "반드시 1개의 아이템을 들고 있어야합니다.");
                    return;
                }
                if(clickType.equals(ClickType.LEFT)) {
                    ItemStack requiredItem = ItemSaver.getItem("장비 수리키트").getItem().clone();
                    if(!InvClass.hasItem(player, requiredItem)) {
                        Msg.warn(player, "수리 키트를 가지고 있지 않습니다.");
                        return;
                    }
                    InvClass.removeItem(player, item);
                }
                else if (clickType.equals(ClickType.RIGHT)) {
                    PaymentData pdc = new PaymentData(player);
                    if(pdc.getRunar() < runar) {
                        Msg.warn(player, "가진 루나르가 부족합니다.");
                        return;
                    }
                    pdc.setRunar(pdc.getRunar() - 45);
                }
                else {
                    Msg.warn(player, "제대로된 클릭을 진행해주세요.");
                    return;
                }
                ItemData itemData = new ItemData(item);
                itemData.repairDurability();
                player.getInventory().setItemInMainHand(itemData.getItemStack());
                player.playSound(player, Sound.BLOCK_SMITHING_TABLE_USE, 1, 1.5f);
                Msg.send(player, "장비를 수리했습니다.", pfix);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.RED_WOOL), "&a수리 진행하기");
                item.addLore("&f좌클릭하면 장비 수리키트를 사용하여 수리합니다.");
                item.addLore(" ");
                item.addLore("&f우클릭하면 " + runar + " 루나르를 지불하고 수리를 진행합니다.");
                return item.getItem();
            }
        });
        map.put(15, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                player.closeInventory();
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.RED_WOOL), "&c취소");
                item.addLore("&f클릭하여 취소합니다.");
                return item.getItem();
            }
        });
        reloadGUI();
        open();
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) player.closeInventory();
    }
}
