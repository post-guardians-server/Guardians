package me.rukon0621.guardians.GUI.item.enhance;

import me.rukon0621.guardians.GUI.item.SingleEquipmentSelectWindow;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.gui.buttons.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static me.rukon0621.guardians.main.isDevServer;
import static me.rukon0621.guardians.main.pfix;

public class FishingRodEnhanceWindow extends SingleEquipmentSelectWindow {



    private enum FAIL_STATUS {

        NO_EQUIPMENT("부여할 낚시대를 넣어주세요.") {
            @Override
            void addLore(ItemClass it) {
                it.addLore("&c주문서를 부여할 낚시대를 선택하십시오.");
            }

            @Override
            boolean isFailed(FishingRodEnhanceWindow window) {
                return window.selectedEquipment == null;
            }
        },
        OVER_ATTR("이미 해당 옵션의 최고 레벨을 달성했습니다.") {
            @Override
            void addLore(ItemClass it) {
                it.addLore("&c이미 해당 옵션이 최고 레벨을 달성했습니다.");
            }

            @Override
            boolean isFailed(FishingRodEnhanceWindow window) {
                return window.attr != null && new ItemData(window.selectedEquipment).getAttrLevel(window.attr) >= window.getMaxLevel();
            }
        },
        ;


        private final String message;
        FAIL_STATUS(String message) {
            this.message = message;
        }


        abstract void addLore(ItemClass it);
        abstract boolean isFailed(FishingRodEnhanceWindow window);
        private void sendMessage(Player player) {
            Msg.warn(player, message);
        }
    }

    private FAIL_STATUS failStatus = FAIL_STATUS.NO_EQUIPMENT;

    private final String attr;

    public FishingRodEnhanceWindow(Player player, String attr, ItemStack bookItem) {
        super(player, "&f\uF000\uF033", 3);
        this.attr = attr;
        map.put(15, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(failStatus != null) {
                    failStatus.sendMessage(player);
                    return;
                }
                if(!player.getInventory().getItemInMainHand().equals(bookItem)) {
                    Msg.warn(player, "손에 아이템을 들어주세요.");
                    player.closeInventory();
                    return;
                }
                bookItem.setAmount(bookItem.getAmount() - 1);
                ItemData itemData = new ItemData(selectedEquipment);
                itemData.setAttr(attr, itemData.getAttrLevel(attr));
                selectedEquipment = itemData.getItemStack();
                player.closeInventory();
                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);
                Msg.send(player, "주문서를 사용했습니다.", pfix);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&e『 주문서 부여 』");
                if(failStatus != null) failStatus.addLore(item);
                else item.addLore("&f클릭하여 주문서를 사용합니다.");
                return item.getItem();
            }
        });
        reloadGUI();
        open();
    }

    public int getMaxLevel() {
        return switch (attr) {
            case "미끼", "내구력" -> 3;
            case "고등급 포획률 증가", "고레벨 포획률 증가", "고품질 포획률 증가" -> 7;
            default -> 1;
        };
    }

    @Override
    protected void reloadGUI() {
        failStatus = null;
        for(FAIL_STATUS st : FAIL_STATUS.values()) {
            if(!st.isFailed(this)) continue;
            failStatus = st;
            break;
        }
        super.reloadGUI();
    }

    @Override
    public void select(int i) {
        ItemStack item = player.getOpenInventory().getItem(i);
        if(item == null) return;
        ItemData itemData = new ItemData(item);
        if(!TypeData.getType(itemData.getType()).isMaterialOf("낚시대")) {
            Msg.warn(player, "낚시대를 넣어주세요.");
            return;
        }
        super.select(i);
    }

    @Override
    public int getItemSlot() {
        return 11;
    }
}
