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

import static java.awt.SystemColor.window;

public class SuccessionGUI extends ItemSelectableWindow {
    private static final int ALLOWED_LEVEL_DIF = 11;

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
        LEVEL_OVER("&c요구 레벨이 " + ALLOWED_LEVEL_DIF + " 이상 차이나면 계승할 수 없습니다.") {
            @Override
            boolean isUnpassed(SuccessionGUI window) {
                ItemData dataA = new ItemData(window.itemA);
                ItemData dataB = new ItemData(window.itemB);
                return Math.abs(dataA.getRequiredLevel() - dataB.getRequiredLevel()) > ALLOWED_LEVEL_DIF;
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
    public SuccessionGUI(Player player, int decreasingQuality, int decreasingEnhance, int decreasingLevel) {
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
                if(!clickType.equals(ClickType.SHIFT_LEFT)) {
                    Msg.warn(player, "계승을 진행하려면 쉬프트 좌클릭 하십시오.");
                    return;
                }
                if(dataA.isImportantItem() || dataA.isQuestItem()) {
                    Msg.warn(player, "중요한 물건이나 퀘스트 아이템은 계승의 재료가 될 수 없습니다.");
                    return;
                }
                //bookItem.setAmount(bookItem.getAmount() - 1);
                dataB.setQuality(Math.max(dataA.getQuality() * (100 - decreasingQuality) / 100, 40));
                dataB.setEnhanceLevel(EnhanceLevel.values()[Math.max(0, dataA.getEnhanceLevel().ordinal() - decreasingEnhance)]);
                dataB.setLevel(dataA.getLevel() - decreasingLevel);
                dataB.setExp((long) (dataA.getExpPercentage() / 100 * ItemData.getMaxExpAtLevel(dataB.getLevel())));
                dataB.setCraftLevel(Math.max(dataA.getCraftLevel(), dataB.getCraftLevel()));
                dataA.setQuality(0);
                dataA.setEnhanceLevel(EnhanceLevel.ZERO);
                itemA = null;
                itemB = null;
                List<ItemStack> list = new ArrayList<>();
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
                it.addLore("&f계승을 받은 장비는 품질이 현재의 90%가 되며 강화 수치가 " + decreasingEnhance + " 감소되어 스펙이 귀속됩니다.");
                it.addLore("&f단 품질은 40이하로 내려가지 않으며 장비 레벨은 원래 장비에서 " + decreasingLevel + "만큼 감소합니다.");
                it.addLore("&7(기존 아이템의 품질이 40보다 낮았다면 품질이 40으로 복구됩니다.)");
                it.addLore("&f두 장비의 요구레벨이 " + ALLOWED_LEVEL_DIF + " 초과로 차이나면 한 번에 계승할 수 없습니다.");
                it.addLore(" ");
                if(failStatus != null) it.addLore("&c" + failStatus.msg);
                else it.addLore("&f\uE011\uE00C\uE00C&e쉬프트 좌클릭&f하여 계승을 진행합니다.");
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
