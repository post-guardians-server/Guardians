package me.rukon0621.guardians.GUI.item;

import com.google.common.xml.XmlEscapers;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.StoneData;
import me.rukon0621.guardians.dropItem.DropManager;
import me.rukon0621.guardians.helper.Couple;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.ItemSelectableWindow;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class AdamantStoneRerollWindow extends SingleEquipmentSelectWindow {

    enum FailReason {
        NO_ITEM("&f아이템을 넣어주세요.") {
            @Override
            boolean isFailed(AdamantStoneRerollWindow window) {
                return window.selectedEquipment == null;
            }

            @Override
            void addLore(AdamantStoneRerollWindow window, ItemClass it) {
                it.addLore(NO_ITEM.message);
            }
        },
        MISSING_HAND("&f손에 주문서를 들어주세요.") {
            @Override
            boolean isFailed(AdamantStoneRerollWindow window) {
                return !window.player.getInventory().getItemInMainHand().equals(window.book);
            }

            @Override
            void addLore(AdamantStoneRerollWindow window, ItemClass it) {
                it.addLore(MISSING_HAND.message);
            }
        },
        MISSING_STONE_DATA("&f아다만트석의 정보를 탐색할 수 없습니다.") {
            @Override
            boolean isFailed(AdamantStoneRerollWindow window) {
                return new ItemData(window.selectedEquipment).getAllStoneData().isEmpty();
            }

            @Override
            void addLore(AdamantStoneRerollWindow window, ItemClass it) {
                it.addLore(MISSING_STONE_DATA.message);
            }
        },
        ;
        private final String message;

        FailReason(String message) {
            this.message = message;
        }

        abstract boolean isFailed(AdamantStoneRerollWindow window);
        abstract void addLore(AdamantStoneRerollWindow window, ItemClass it);

        public void warn(Player player) {
            Msg.warn(player, message);
        }
    }

    private FailReason failReason = FailReason.NO_ITEM;
    private final ItemStack book;

    public AdamantStoneRerollWindow(Player player, ItemStack book) {
        super(player, "&f\uF000\uF033", 3);
        this.book = book;
        map.put(15, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(failReason != null) {
                    failReason.warn(player);
                    return;
                }
                double cnt = getCntQualityProportion();
                if(cnt >= 100) {
                    Msg.warn(player, "이미 최고 수치의 아다만트석입니다.");
                    return;
                }
                ItemData itemData = new ItemData(selectedEquipment);
                StoneData newData = DropManager.rerollStoneData(itemData.getStoneData(1), itemData.getLevel());
                if(newData == null) {
                    Msg.warn(player, "리롤할 수 있는 아다만트 데이터를 탐색하지 못했습니다.");
                    return;
                }
                itemData.removeStoneData(1);
                itemData.addStoneData(newData);
                selectedEquipment = itemData.getItemStack();
                book.setAmount(book.getAmount() - 1);
                cnt = getCntQualityProportion();
                if(cnt >= 100) player.playSound(player, Sound.ITEM_TOTEM_USE, 1, 0.8f);
                else if(cnt >= 90) player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, 0.8f);
                else if(cnt >= 80) player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);
                else player.playSound(player, Sound.BLOCK_CHAIN_PLACE, 1, 0.8f);
                if(book.getAmount() == 0) player.closeInventory();
                reloadGUI();
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&e『 옵션 재설정 』");
                item.setCustomModelData(7);
                if(failReason != null) {
                    failReason.addLore(AdamantStoneRerollWindow.this, item);
                }
                else {
                    ItemData itemData = new ItemData(selectedEquipment);
                    Couple<Double, Double> v = DropManager.getAvailableValue(itemData.getStoneData(1), itemData.getLevel());
                    if(v == null) {
                        item.addLore("&c아다만트 데이터가 검색되지 않습니다.");
                    }
                    else {
                        item.addLore("&f클릭하여 아다만트석의 스텟을 재설정합니다.");
                        item.addLore(" ");
                        double cnt = getCntQualityProportion();
                        if(cnt >= 100) item.addLore(String.format("&f현재 수치: #ff5555%.2f", itemData.getStoneData(1).getValue()));
                        else if(cnt >= 90) item.addLore(String.format("&f현재 수치: #ffaaaa%.2f", itemData.getStoneData(1).getValue()));
                        else if(cnt >= 80) item.addLore(String.format("&f현재 수치: #ffeeaa%.2f", itemData.getStoneData(1).getValue()));
                        else item.addLore(String.format("&f현재 수치: %.2f", itemData.getStoneData(1).getValue()));
                        item.addLore(String.format("&f변동 범위: &7%.2f~%.2f", v.getFirst(), v.getSecond()));
                    }
                }
                return item.getItem();
            }

            /**
             *
             * @return 현재 수치가 몇 % 인지 반환 (0~100%) 5 ~ 7 범위에서 6이면 50%
             */
            private double getCntQualityProportion() {
                ItemData itemData = new ItemData(selectedEquipment);
                Couple<Double, Double> c = DropManager.getAvailableValue(itemData.getStoneData(1), itemData.getLevel());
                double v = Objects.requireNonNull(itemData.getStoneData(1)).getValue();
                assert c != null;
                return (v - c.getFirst()) / (c.getSecond() - c.getFirst()) * 100;
            }
        });
        reloadGUI();
        open();
    }

    @Override
    protected void reloadGUI() {
        failReason = null;
        for(FailReason reason : FailReason.values()) {
            if(reason.isFailed(this)) {
                failReason = reason;
                break;
            }
        }
        super.reloadGUI();
    }

    @Override
    public void select(int i) {
        ItemData itemData = new ItemData(player.getOpenInventory().getItem(i));
        if(!itemData.getType().equals("아다만트석")) {
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

    @Override
    public int getItemSlot() {
        return 11;
    }
}
