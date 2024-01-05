package me.rukon0621.guardians.GUI.item;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.gui.buttons.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class ItemDisassembleWindow extends SingleEquipmentSelectWindow {

    private static final int EQUIPMENT_SLOT = 11;
    private static final int EXP_DIVISION = 150;
    private static final int DIVISION_LEVEL_STAGE = 50;
    private static final int LEVEL = 3; //장비의 영혼 레벨
    private static final int EQUIPMENT_SOUL = 3; //장비의 영혼 개수
    private static final int LEVEL_LIMIT = 10; //레벨 제한

    public static final double MIN_DISASSEMBLE_PROPORTION = 0.35;
    public static final double MAX_DISASSEMBLE_PROPORTION = 0.65;

    private static int getDinar(int level) {
        int stage = (int) ((level / (double) LevelData.maxLevel) * 50);
        return (int) (500 + Math.pow(10 + stage, 2.25));
    }

    public ItemDisassembleWindow(Player player) {
        super(player, "&f\uF000\uF034", 3);

        map.put(15, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(!map.containsKey(EQUIPMENT_SLOT)) {
                    Msg.warn(player, "분해할 장비를 선택해주세요.");
                    return;
                }
                ItemData itemData = new ItemData(inv.getSlot(EQUIPMENT_SLOT));
                PlayerData pdc = new PlayerData(player);

                if(pdc.getMoney() < getDinar(itemData.getLevel())) {
                    Msg.warn(player, "디나르가 부족해 분해를 진행할 수 없습니다.");
                    return;
                }

                int DIVISION = EXP_DIVISION * (1 + (itemData.getLevel() / DIVISION_LEVEL_STAGE));
                int exp = itemData.getDisassembleExp(MAX_DISASSEMBLE_PROPORTION);
                if(exp <= 10) {
                    Msg.warn(player, "장비에서 추출할 수 있는 경험치가 너무 적어 장비를 분해할 수 없습니다.");
                    return;
                }
                exp = itemData.getDisassembleExp();

                if(itemData.getLevel() < LEVEL_LIMIT) {
                    Msg.warn(player, LEVEL_LIMIT + "레벨 이상의 아이템만 분해할 수 있습니다.");
                    return;
                }

                map.remove(EQUIPMENT_SLOT);
                reloadGUI();
                List<ItemStack> items = new ArrayList<>();

                if(!itemData.isDisassemble()) {
                    ItemStack item = ItemSaver.getItemParsed("장비의 영혼", Math.min(itemData.getLevel() + LEVEL, LevelData.maxLevel)).getItem();
                    item.setAmount(EQUIPMENT_SOUL);
                    items.add(item);
                }

                int amount = exp / DIVISION;
                int remain = exp % DIVISION;
                ItemStack book = LevelData.getEquipmentExpBook(DIVISION);
                for(int i = 0; i < amount ; i++) {
                    items.add(book);
                }
                if(remain!=0) {
                    items.add(LevelData.getEquipmentExpBook(remain));
                }
                MailBoxManager.giveAllOrMailAll(player, items);
                pdc.setMoney(pdc.getMoney() - getDinar(itemData.getLevel()));
                if(itemData.isDisassemble()) {
                    Msg.send(player, "장비를 분해했습니다. (장비의 영혼은 추출되지 않았습니다.)", pfix);
                }
                else {
                    Msg.send(player, "장비를 분해했습니다.", pfix);
                }
                player.playSound(player, Sound.ITEM_TOTEM_USE, 0.7f, 2);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass icon = new ItemClass(new ItemStack(Material.SCUTE), "&e아이템 분해하기");
                icon.setCustomModelData(7);
                if(!map.containsKey(EQUIPMENT_SLOT)) {
                    icon.addLore("&c분해할 장비를 선택해주세요.");
                    icon.addLore(" ");
                    icon.addLore("&e10레벨 이상의 장비만 분해할 수 있습니다.");
                }
                else {
                    ItemData id = new ItemData(map.get(EQUIPMENT_SLOT).getIcon());
                    if(id.getLevel() < LEVEL_LIMIT) {
                        icon.addLore("&e10레벨 이상의 장비만 분해할 수 있습니다.");
                        return icon.getItem();
                    }
                    icon.addLore("&f클릭하면 아이템 분해를 진행합니다.");
                    if(id.isDisassemble()) {
                        icon.addLore("&f분해를 진행하면 장비 레벨보다 &e대량의 강화의 서&f를 지급 받을 수 있습니다.");
                    }
                    else {
                        icon.addLore("&f분해를 진행하면 장비 레벨보다 " + LEVEL + "레벨 높은 &b장비의 영혼 " + EQUIPMENT_SOUL + "개&f와");
                        icon.addLore("&e대량의 강화의 서&f를 지급 받을 수 있습니다.");
                    }
                    icon.addLore(" ");
                    icon.addLore(String.format("&b\uE011\uE00C\uE00C분해시 돌려받는 경험치: %d ~ %d", id.getDisassembleExp(MIN_DISASSEMBLE_PROPORTION), id.getDisassembleExp(MAX_DISASSEMBLE_PROPORTION)));
                    icon.addLore("&b\uE015\uE00C\uE00C요구 디나르: " + getDinar(id.getLevel()));
                    if(id.isDisassemble()) {
                        icon.addLore(" ");
                        icon.addLore("&c\uE014\uE00C\uE00C이 장비는 분해시 경험치만 등장합니다.");
                        return icon.getItem();
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
