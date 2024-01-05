package me.rukon0621.guardians.GUI;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.ScrollableWindow;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;

import static me.rukon0621.guardians.main.pfix;

public class WeaponSkinWindow extends ScrollableWindow {
    public WeaponSkinWindow(Player player) {
        super(player, "&f\uF000\uF023", 6, 0, Math.max((new PlayerData(player).getWeaponSkins().size() - 1), 0) / 8);
        map.put(26, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                WeaponSkinWindow.this.y--;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                reloadGUI();
            }

            @Override
            public ItemStack getIcon() {
                ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&6↑");
                it.setCustomModelData(7);
                return it.getItem();
            }
        });
        map.put(35, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                WeaponSkinWindow.this.y++;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                reloadGUI();
            }

            @Override
            public ItemStack getIcon() {
                ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&6↓");
                it.setCustomModelData(7);
                return it.getItem();
            }
        });
        reloadGUI();
        open();
    }

    @Override
    public void reloadGUI() {
        int x = 0, y = 0;

        PlayerData pdc = new PlayerData(player);
        int cntEquipped = pdc.getWeaponSkinCmd();

        pdc.getWeaponSkins().sort(Comparator.comparing(o -> Msg.uncolor(o.getItemMeta().getDisplayName())));

        for(ItemStack item : pdc.getWeaponSkins()) {
            if(x == 8) {
                y++;
                x = 0;
            }
            icons[y][x] = new Button() {
                @Override
                public void execute(Player player, ClickType clickType) {
                    if(getCmd() == cntEquipped) {
                        pdc.setWeaponSkinCmd(0);
                        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
                        Msg.send(player, "장착을 해제했습니다.", pfix);
                    }
                    else {

                        /*
                        if(clickType.equals(ClickType.SHIFT_RIGHT)) {
                            pdc.getWeaponSkins().remove(item);
                            Msg.warn(player, "스킨을 삭제했습니다.");
                            player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1.5f);
                            disable();
                            player.closeInventory();
                            new WeaponSkinWindow(player);
                            return;
                        }
                         */

                        pdc.setWeaponSkinCmd(item.getItemMeta().getCustomModelData());
                        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
                        Msg.send(player, "스킨을 장착했습니다.", pfix);
                    }
                    reloadWeaponSkin(player);
                }

                private int getCmd() {
                    return item.getItemMeta().getCustomModelData();
                }

                @Override
                public ItemStack getIcon() {
                    ItemClass it = new ItemClass(item.clone());
                    it.addLore(" ");
                    if(it.getCustomModelData() == cntEquipped) {
                        it.addLore("&c※ 장착 해제하려면 클릭하십시오.");
                    }
                    else {
                        it.addLore("&f※ 장착하려면 클릭하십시오.");
                        //it.addLore("&f※ &c쉬프트+우클릭&f하면 스킨을 버릴 수 있습니다.");
                    }
                    return it.getItem();
                }
            };
            super.reloadGUI();
            x++;
        }

    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) new MenuWindow(player, 2);
    }

    public static void reloadWeaponSkin(Player player) {
        if(EquipmentManager.getWeapon(player).getType().equals(Material.AIR)) return;
        if(player.getInventory().getItem(0) == null) return;

        ItemStack weapon = player.getInventory().getItem(0);
        int cmd = new PlayerData(player).getWeaponSkinCmd();
        if(cmd == 0) {
            try {
                if(weapon.getItemMeta().hasCustomModelData()) cmd = ItemSaver.getItem(Msg.uncolor(weapon.getItemMeta().getDisplayName())).getCustomModelData();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Msg.warn(player, "현재 무기가 리로드되지 않습니다. 디스코드 티켓을 통해 운영진에게 문의하십시오.");
                return;
            }
        }
        ItemClass it = new ItemClass(weapon.clone());
        it.setCustomModelData(cmd);
        player.getInventory().setItem(0, it.getItem());
    }
}
