package me.rukon0621.guardians.GUI.item;

import me.rukon0621.guardians.data.EnhanceLevel;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.ItemSelectableWindow;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SuccessionGUI extends ItemSelectableWindow {
    private static final int EXP_DIVISION = 150;
    private static final int DIVISION_LEVEL_STAGE = 50;

    public static final double MIN_DISASSEMBLE_PROPORTION = 0.35;
    public static final double MAX_DISASSEMBLE_PROPORTION = 0.65;

    private enum FailStatus {
        NOT_FILLED("&c아이템이 모두 채워지지 않았습니다.") {
            @Override
            boolean isUnpassed(SuccessionGUI window) {
                return window.itemA == null || window.itemB == null;
            }
        },
        NOT_MATCHED_TYPE("&c무기는 무기, 방어구는 방어구, 장신구는 장신구끼리 계승할 수 있습니다.") {
            @Override
            boolean isUnpassed(SuccessionGUI window) {
                TypeData typeA = TypeData.getType(window.dataA.getType());
                TypeData category;
                if(typeA.isMaterialOf("무기")) category = TypeData.getType("무기");
                else if(typeA.isMaterialOf("방어구")) category = TypeData.getType("방어구");
                else if(typeA.isMaterialOf("장신구")) category = TypeData.getType("장신구");
                else return true;
                TypeData typeB = TypeData.getType(window.dataB.getType());
                return !typeB.isMaterialOf(category.getName());
            }
        },
        ;

        private final String msg;

        FailStatus(String msg) {
            this.msg = msg;
        }

        abstract boolean isUnpassed(SuccessionGUI window);

    }

    private ItemStack itemA = null;
    private ItemStack itemB = null;
    private ItemData dataA = null;
    private ItemData dataB = null;
    private FailStatus failStatus = FailStatus.NOT_FILLED;
    public SuccessionGUI(Player player, int decreasingQuality, int decreasingEnhance) {
        super(player, "&f\uF000\uF03D", 3);

        map.put(11, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(itemA == null) return;
                MailBoxManager.giveOrMail(player, itemA);
                itemA = null;
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
                reloadGUI();
            }

            @Override
            public ItemStack getIcon() {
                if(itemA == null) return new ItemStack(Material.AIR);
                return itemA;
            }
        });
        map.put(15, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(itemB == null) return;
                MailBoxManager.giveOrMail(player, itemB);
                itemB = null;
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
                reloadGUI();
            }

            @Override
            public ItemStack getIcon() {
                if(itemB == null) return new ItemStack(Material.AIR);
                return itemB;
            }
        });

        map.put(13, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(failStatus != null) {
                    Msg.warn(player, failStatus.msg);
                    return;
                }

                if(dataA.isImportantItem() || dataA.isQuestItem()) {
                    Msg.warn(player, "중요한 물건이나 퀘스트 아이템은 계승의 재료가 될 수 없습니다.");
                    return;
                }

                //bookItem.setAmount(bookItem.getAmount() - 1);
                dataB.setQuality(Math.max(dataA.getQuality() - decreasingQuality, 0));
                dataB.setEnhanceLevel(EnhanceLevel.values()[Math.max(0, dataA.getEnhanceLevel().ordinal() - decreasingEnhance)]);
                dataA.setQuality(0);
                dataA.setEnhanceLevel(EnhanceLevel.ZERO);
                itemA = null;
                itemB = null;

                List<ItemStack> list = new ArrayList<>();
                int DIVISION = EXP_DIVISION * (1 + (dataA.getLevel() / DIVISION_LEVEL_STAGE));
                int exp = dataA.getDisassembleExp(MAX_DISASSEMBLE_PROPORTION);
                int amount = exp / DIVISION;
                int remain = exp % DIVISION;
                ItemStack book = LevelData.getEquipmentExpBook(DIVISION);
                for(int i = 0; i < amount ; i++) {
                    list.add(book);
                }
                if(remain!=0) {
                    list.add(LevelData.getEquipmentExpBook(remain));
                }
                MailBoxManager.giveAllOrMailAll(player, list);

                list.add(dataB.getItemStack());
                MailBoxManager.giveAllOrMailAll(player, list);
                player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1.5f);
                disable();
                player.closeInventory();
            }

            @Override
            public ItemStack getIcon() {
                ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&c【 계승 진행하기 】");
                it.setCustomModelData(7);
                it.addLore("&e\uE011\uE00C\uE00C계승이란?");
                it.addLore("&f계승을 진행하면 &4원래의 무기는 소멸&f되고");
                it.addLore("&f계승을 받은 장비는 품질이 " + decreasingQuality + "% 감소되며 강화 수치가 " + decreasingEnhance + " 감소되어 스펙이 귀속됩니다.");
                it.addLore("&f또한 &b일부 경험치&f를 되돌려 받게 됩니다.");
                it.addLore(" ");
                if(failStatus != null) {
                    it.addLore("&c" + failStatus.msg);
                }
                else {
                    it.addLore("&f\uE011\uE00C\uE00C클릭하여 계승을 진행합니다.");
                }
                return it.getItem();
            }
        });

        reloadGUI();
        open();
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent e) {
        if(!e.getPlayer().equals(player)) return;
        close(false);
    }

    @Override
    protected void reloadGUI() {
        if(itemA == null) dataA = null;
        else dataA = new ItemData(itemA);
        if(itemB == null) dataB = null;
        else dataB = new ItemData(itemB);

        failStatus = null;
        for(FailStatus st : FailStatus.values()) {
            if(st.isUnpassed(this)) {
                failStatus = st;
                break;
            }
        }
        super.reloadGUI();
    }

    @Override
    public void select(int i) {
        ItemData itemData = new ItemData(player.getOpenInventory().getItem(i));
        if(!itemData.isEquipment()||itemData.getType().equals("사증")) {
            return;
        }
        if(itemA == null) {
            itemA = player.getOpenInventory().getItem(i);
            player.getOpenInventory().setItem(i, null);
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
        }
        if(itemB == null) {
            itemB = player.getOpenInventory().getItem(i);
            player.getOpenInventory().setItem(i, null);
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
        }
        reloadGUI();
    }

    @Override
    public void close(boolean b) {
        disable();
        List<ItemStack> items = new ArrayList<>();
        if(itemA != null) items.add(itemA);
        if(itemB != null) items.add(itemB);
        if(!items.isEmpty()) {
            MailBoxManager.giveAllOrMailAll(player, items);
        }
        if(b) player.closeInventory();
    }
}
