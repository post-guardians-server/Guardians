package me.rukon0621.guardians.GUI.item;

import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.commands.EntireBroadcastCommand;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.gui.buttons.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static me.rukon0621.guardians.main.pfix;

public class QualityRandomWindow extends SingleEquipmentSelectWindow {
    private static final int EQUIPMENT_SLOT = 11;
    private static final double[] pro = new double[101];
    private static boolean initialized = false;

    public static void initialize() {
        initialized = true;
        for(int i = 1; i <= 100; i++) {
            pro[i] += pro[i - 1] + 100 * Math.pow(Math.E, -1 * Math.pow( (i - 65) / 25f , 2));
        }
        double sum = pro[100];
        for(int i = 1; i <= 100; i++) {
            pro[i] *= 100;
            pro[i] /= sum;
        }
    }

    public static double execute() {
        while(true) {
            double seed = new Random().nextDouble() * 100;
            for(int i = 1; i <= 100; i++) {
                if(seed >= pro[i]) continue;
                return i + (new Random().nextDouble() * 2 - 1);
            }
        }
    }

    /**
     *
     * @param player player
     * @param item 강화서 아이템
     * @param keep true면 품질이 더 낮아지지 않도록 유지
     */
    public QualityRandomWindow(Player player, ItemStack item, boolean keep) {
        super(player, "&f\uF000\uF033", 3);
        if(!initialized) initialize();
        map.put(15, new Button() {

            @Override
            public void execute(Player player, ClickType clickType) {
                if(!map.containsKey(EQUIPMENT_SLOT)) {
                    Msg.warn(player, "아이템을 선택해주세요.");
                }

                if(inv.getSlot(EQUIPMENT_SLOT)==null) {
                    Msg.warn(player, "먼저 아이템을 선택해주세요.");
                    return;
                }
                ItemData itemData = new ItemData(inv.getSlot(EQUIPMENT_SLOT));

                if (itemData.getQuality()==100) {
                    Msg.warn(player, "이 아이템은 이미 최고의 품질을 가지고 있습니다.");
                    return;
                }

                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);

                double result = QualityRandomWindow.execute();
                if(result > 100) result = 100;

                item.setAmount(item.getAmount() - 1);
                StringBuilder sb = new StringBuilder("품질");
                if(keep) {
                    if(result < itemData.getQuality()) {
                        Msg.send(player, String.format("&c%.2f%%&e의 품질로 떨어질 뻔 했지만 품질이 유지되었습니다!", result), pfix);
                    }
                    else {
                        if(result > 90) {
                            EntireBroadcastCommand.entireBroadcast(player, String.format(player.getName() + "님이" + result + "%의 품질을 띄웠습니다!!"), false);
                        }
                        Msg.send(player, String.format("&a품질이 증가했습니다! &7( %.2f -> %.2f )", itemData.getQuality(), result), pfix);
                        itemData.setQuality(result);
                    }
                    sb.append("블랙랜덤 (").append(itemData.getQuality()).append("->").append(result).append(") ").append(itemData);
                }
                else {
                    Msg.send(player, String.format("&e아이템을 사용했습니다! &7( %.2f -> %.2f )", itemData.getQuality(), result), pfix);
                    itemData.setQuality(result);
                    if(result > 90) {
                        EntireBroadcastCommand.entireBroadcast(player, String.format("&6%s&e님이 &b%.2f%%&e의 품질을 띄웠습니다!!", player.getName(), result), false);
                    }
                    sb.append("랜덤 (").append(itemData.getQuality()).append("->").append(result).append(") ").append(itemData);
                }
                LogManager.log(player, "itemQuality", sb.toString());

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
                        icon.addLore("&f확률이 &70%&f에서 &b100%&f사이로 랜덤하게 변경됩니다.");
                        if(keep) {
                            icon.addLore("&b고급 품질 강화서&f는 낮은 품질이 떠도 품질이 유지됩니다!");
                        }
                        icon.addLore(" ");
                        icon.addLore("&f높은 품질일 수록 더 낮은 확률로 뜹니다.");
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
