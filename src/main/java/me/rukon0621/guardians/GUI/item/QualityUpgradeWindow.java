package me.rukon0621.guardians.GUI.item;

import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.GUI.item.SingleEquipmentSelectWindow;
import me.rukon0621.guardians.commands.EntireBroadcastCommand;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.helper.Couple;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Rand;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.gui.buttons.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static me.rukon0621.guardians.main.pfix;

public class QualityUpgradeWindow extends SingleEquipmentSelectWindow {
    private static final int EQUIPMENT_SLOT = 11;
    private static final double[] pro = new double[101];

    public QualityUpgradeWindow(Player player, ItemStack item, Couple<Double, Double> range) {
        super(player, "&f\uF000\uF033", 3);

        ItemData bookData = new ItemData(item);
        map.put(15, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(!map.containsKey(EQUIPMENT_SLOT)) {
                    Msg.warn(player, "아이템을 선택해주세요.");
                }
                if(!player.getInventory().getItemInMainHand().equals(item)) {
                    Msg.warn(player, "품질 강화서를 들어주세요.");
                    return;
                }
                if(inv.getSlot(EQUIPMENT_SLOT)==null) {
                    Msg.warn(player, "먼저 아이템을 선택해주세요.");
                    return;
                }
                ItemData itemData = new ItemData(inv.getSlot(EQUIPMENT_SLOT));
                if(bookData.getLevel() < itemData.getRequiredLevel()) {
                    Msg.warn(player, "품질 강화서보다 장비의 요구 레벨이 더 높아 품질 강화를 진행할 수 없습니다.");
                    return;
                }
                else if (itemData.getQuality()==100) {
                    Msg.warn(player, "이 아이템은 이미 최고의 품질을 가지고 있습니다.");
                    return;
                }
                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);
                double result = Rand.randDouble(range.getFirst(), range.getSecond());
                Msg.send(player, String.format("&e품질이 변동되었습니다! &7( %.2f -> %.2f )", itemData.getQuality(), itemData.getQuality() + result), pfix);
                StringBuilder sb = new StringBuilder("품질");
                sb.append("(").append(itemData.getQuality()).append("->").append(itemData.getQuality() + result).append(") ").append(itemData);
                LogManager.log(player, "itemQuality", sb.toString());
                itemData.setQuality(itemData.getQuality() + result);
                /*
                if(itemData.getQuality() >= 100) {
                    EntireBroadcastCommand.entireBroadcast(player, String.format("&6%s&e님이 &b100%%&e의 품질을 달성했습니다!!", player.getName()), false);
                }
                 */
                item.setAmount(item.getAmount() - 1);
                selectedEquipment = itemData.getItemStack();
                ((EquipmentButton) map.get(11)).setOriginalItem(selectedEquipment);
                if(item.getAmount() == 0) {
                    MailBoxManager.giveOrMail(player, selectedEquipment);
                    disable();
                    player.closeInventory();
                }
                else {
                    reloadGUI();
                }
            }

            @Override
            public ItemStack getIcon() {
                ItemClass icon = new ItemClass(new ItemStack(Material.SCUTE), "&6아이템 사용");
                icon.setCustomModelData(7);

                ItemStack selected = inv.getSlot(EQUIPMENT_SLOT);
                if(selected==null) {
                    icon.addLore("&c품질 강화를 진행할 아이템을 넣어주세요.");
                }
                else {
                    ItemData selectedData = new ItemData(selected);

                    if(selectedData.getQuality()==100) {
                        icon.addLore("&c이 아이템은 이미 최고의 품질을 가지고 있습니다.");
                    }
                    else {
                        icon.addLore(String.format("&7장비 품질 변동 범위: %.2f ~ %.2f", range.getFirst(), range.getSecond()));
                        icon.addLore(String.format("&e요구 레벨 %d레벨&f 이하의 장비를 대상으로 진행할 수 있습니다.", bookData.getLevel()));
                        icon.addLore(" ");
                        icon.addLore("&f클릭하면 아이템을 사용합니다.");
                    }
                }

                return icon.getItem();
            }
        });
        reloadGUI();
        open();
    }

    @Override
    public int getItemSlot() {
        return EQUIPMENT_SLOT;
    }
}
