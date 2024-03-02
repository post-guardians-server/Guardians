package me.rukon0621.guardians.GUI.item.enhance;

import me.rukon0621.guardians.GUI.item.SingleEquipmentSelectWindow;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.StoneData;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.gui.buttons.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static me.rukon0621.guardians.main.pfix;

public class StoneUseWindow extends SingleEquipmentSelectWindow {

    private enum FAIL_STATUS {
        NULL("장비를 넣어주세요.") {
            @Override
            public void addLore(ItemClass item, StoneUseWindow window) {
                item.addLore("&c장비를 넣어주세요.");
                item.addLore(" ");
                item.addLore("&f아다만트석은 등급에 따라 각각 부여할 수 있는 최대 수가 다르며");
                item.addLore("&f부여 비용은 아다만트석의 레벨과 등급에 따라 달라집니다.");
            }

            @Override
            public boolean isFailed(StoneUseWindow window) {
                return window.selectedEquipment == null;
            }
        },
        NO_MONEY("돈이 부족합니다.") {
            @Override
            public void addLore(ItemClass item, StoneUseWindow window) {
                item.addLore("&c디나르가 부족합니다.");
                item.addLore(" ");
                item.addLore("&7비용: &f" + StoneData.getGrantPrice(window.itemData.getLevel(), window.itemData.getGrade()) + "디나르");
            }

            @Override
            public boolean isFailed(StoneUseWindow window) {
                Player player = window.player;
                return new PlayerData(player).getMoney() < StoneData.getGrantPrice(window.itemData.getLevel(), window.itemData.getGrade());
            }
        },
        RIDING("라이딩에는 부여할 수 없습니다..") {
            @Override
            public void addLore(ItemClass item, StoneUseWindow window) {
                item.addLore("&c라이딩에는 사용할 수 없습니다.");
            }

            @Override
            public boolean isFailed(StoneUseWindow window) {
                return window.itemData.isRiding();
            }
        },
        LOW_EQUIPMENT_LEVEL("장비의 레벨이 아다만트석보다 낮을 수 없습니다.") {
            @Override
            public void addLore(ItemClass item, StoneUseWindow window) {
                item.addLore("&c장비의 레벨이 아다만트석보다 낮을 수 없습니다.");
            }

            @Override
            public boolean isFailed(StoneUseWindow window) {
                return new ItemData(window.selectedEquipment).getLevel() < window.itemData.getLevel();
            }
        },
        MAX_SLOT("이미 이 장비에 부여할 수 있는 모든 아다만트석을 부여했습니다.") {
            @Override
            public void addLore(ItemClass item, StoneUseWindow window) {
                ItemData equipmentData = new ItemData(window.selectedEquipment);
                item.addLore("&f이 장비에는 최대 " + equipmentData.getGrade().getStoneMaxSlots() + "개까지 부여할 수 있습니다.");
            }

            @Override
            public boolean isFailed(StoneUseWindow window) {
                ItemData equipmentData = new ItemData(window.selectedEquipment);
                return equipmentData.getAllStoneData().size() >= equipmentData.getGrade().getStoneMaxSlots();
            }
        },
        NO_STONE("아다만트석을 부여하려면 결속석이 필요합니다.") {
            @Override
            public void addLore(ItemClass item, StoneUseWindow window) {
                item.addLore("&f아다만트를 부여하려면 &b" + window.getEquipmentType() + " 결속석&f이 필요합니다.");
            }

            @Override
            public boolean isFailed(StoneUseWindow window) {
                return !ItemData.hasItemOnlyName(window.player, window.getStone());
            }
        };

        private final String message;
        FAIL_STATUS(String message) {
            this.message = message;
        }

        public void sendMessage(Player player) {
            Msg.warn(player, message);
        }
        public abstract void addLore(ItemClass item, StoneUseWindow window);
        public abstract boolean isFailed(StoneUseWindow window);
    }

    private final StoneData stoneData;
    private final ItemData itemData;
    private FAIL_STATUS status = FAIL_STATUS.NULL;

    public String getEquipmentType() {
        ItemData equipmentData = new ItemData(selectedEquipment);
        if(equipmentData.isWeapon()) return "무기";
        else if (equipmentData.isArmor()) return "방어구";
        else return "장신구";
    }
    public ItemStack getStone() {
        return ItemSaver.getItem(getEquipmentType() + " 결속석").getItem();
    }

    public StoneUseWindow(Player player, ItemStack stone) {
        super(player, "&f\uF000\uF033", 3);
        this.itemData = new ItemData(stone);
        this.stoneData = itemData.getStoneData(1);
        if(new PlayerData(player).getLevel() < itemData.getLevel()) {
            Msg.warn(player, "자신보다 높은 레벨의 아다만트석을 사용할 수 없습니다.");
            return;
        }
        if(stoneData == null) {
            Msg.warn(player, "이 아이템은 저장된 아다만트 데이터가 존재하지 않습니다.");
            return;
        }
        map.put(15, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(!player.getInventory().getItemInMainHand().equals(stone)) {
                    player.closeInventory();
                    Msg.warn(player, "손에 아다만트석을 들고 있어야합니다.");
                    return;
                }
                if(status != null) {
                    status.sendMessage(player);
                    return;
                }

                if(FAIL_STATUS.NO_STONE.isFailed(StoneUseWindow.this)) {
                    FAIL_STATUS.NO_STONE.sendMessage(player);
                    return;
                }
                PlayerData pdc = new PlayerData(player);
                pdc.setMoney(pdc.getMoney() - StoneData.getGrantPrice(itemData.getLevel(), itemData.getGrade()));
                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.3f);
                ItemData equipmentData = new ItemData(selectedEquipment);
                equipmentData.addStoneData(stoneData);
                stone.setAmount(stone.getAmount() - 1);
                selectedEquipment = equipmentData.getItemStack();
                player.closeInventory();
                ItemData.removeItem(player, new ItemData(getStone()), false);
                Msg.send(player, "장비에 성공적으로 아다만트의 힘을 부여했습니다.", pfix);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), StoneData.stoneColor + "『 아다만트석 부여 』");
                it.setCustomModelData(7);
                if(status != null) {
                    status.addLore(it, StoneUseWindow.this);
                }
                else {
                    it.addLore("&7부여 비용: &f" + StoneData.getGrantPrice(itemData.getLevel(), itemData.getGrade()) + "디나르");
                    it.addLore("&7부여 재료: &f" + getEquipmentType() + " 결속성 1개");
                    it.addLore(" ");
                    it.addLore("&f클릭하여 아다만트석을 사용합니다.");
                    it.addLore("&c주의: 부여를 해제해도 다시 돌려받을 수 없습니다.");
                }
                return it.getItem();
            }
        });
        reloadGUI();
        open();
    }

    @Override
    protected void reloadGUI() {
        status = null;
        for(FAIL_STATUS st : FAIL_STATUS.values()) {
            if(!st.isFailed(this)) continue;
            status = st;
            break;
        }
        super.reloadGUI();
    }

    @Override
    public int getItemSlot() {
        return 11;
    }
}
