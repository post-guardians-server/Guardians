package me.rukon0621.guardians.equipmentLevelup;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.ItemGrade;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * 장비 레벨업 관련 GUI, 이벤트, 메소드 관리
 */
public class RuneLevelUpWindow implements Listener {
    private enum Status {
        AVAILABLE,
        UNAVAILABLE,
        NO_EQUIPMENT,
        NO_EXP,
        OVER_LEVEL
    }
    private static final main plugin = main.getPlugin();
    private static final String guiName = "&f\uF000\uF026";
    private static final int equipmentSlot = 11; //장비 공간 슬롯
    private static final int executeButtonSlot = 15; //강화 버튼 슬롯 위치
    private static final int[] runeSlots = new int[]{28,29,30,31,32,33,34}; //룬 슬롯

    private final Player player;
    private final Inventory inv;
    private Status status = Status.NO_EQUIPMENT;
    private ItemData itemData;

    public RuneLevelUpWindow(Player player) {
        this.player = player;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        InvClass inv = new InvClass(4, guiName);
        this.inv = inv.getInv();
        itemData = null;
        reloadButton();
        player.openInventory(this.inv);
    }

    private double getExpInInv() {

        ItemGrade grade = itemData.getGrade();

        double exp = 0;
        for(int slot : runeSlots) {
            ItemStack item = inv.getItem(slot);
            if(item==null) continue;
            ItemData rune = new ItemData(item);
            int lv = grade.getLevel() - rune.getGrade().getLevel();
            if(lv < 0) {
                return -1;
            }
            if(lv==0) exp += 10;
            else exp += 10.0 / (lv * 4.0);
        }
        return exp;
    }

    /**
     * 버튼 설명 및 STATUS 재설정
     */
    private void reloadButton() {
        ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&c『 &6강화하기 &c』");
        item.setCustomModelData(7);
        if(inv.getItem(equipmentSlot)==null) {
            status = Status.NO_EQUIPMENT;
            item.addLore("&c먼저 강화할 룬을 넣어주세요.");
            inv.setItem(executeButtonSlot, item.getItem());
            return;
        }
        double exp = getExpInInv();
        if(itemData.getLevel() >= LevelData.runeMaxLevel) {
            status = Status.OVER_LEVEL;
            item.addLore("&c이 룬은 이미 최고 레벨에 달성했습니다!");
        }
        else if (exp == -1) {
            status = Status.UNAVAILABLE;
            item.addLore("&c강화 재료로 사용할 수 없는 등급의 룬을 사용하고 있습니다!");
            item.addLore("&c강화하려는 룬보다 높은 등급의 룬을 재료로 쓸 수 없습니다!");
        }
        else if (getExpInInv()==0) {
            status = Status.NO_EXP;
            item.addLore("&f강화 재료로 사용할 룬을 넣어주세요.");
            item.addLore("&f높은 등급의 룬을 쓸수록 더 많은 경험치를 올릴 수 있습니다.");
        }
        else {
            item.addLore("&f현재 선택된 룬: "+itemData.getName());
            item.addLore(" ");
            item.addLore("&7레벨: "+itemData.getLevel());
            item.addLore(String.format("&7경험치: %.2f%%", itemData.getExpPercentage()));
            double percent = (itemData.getExpPercentage() + exp) % 100;
            int level = itemData.getLevel() +  (int) ((itemData.getExpPercentage() + exp) / 100);
            item.addLore(" ");
            item.addLore("&7 ======= ↓ &f강화 결과 &7↓ ======= ");
            item.addLore(" ");
            item.addLore("&7레벨: "+level);
            if(level >= LevelData.runeMaxLevel) {
                item.addLore("&7경험치: 0%");
                item.addLore(" ");
                item.addLore("&c강화시 룬이 최대 레벨을 달성하게 되고 초과되는 경험치는 사라집니다!");
                item.addLore("&c초과되는 경험치: " + percent + "%");
            } else {
                item.addLore(String.format("&7경험치: %.2f%%", percent));
            }
            status = Status.AVAILABLE;
        }
        inv.setItem(executeButtonSlot, item.getItem());
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(!e.getWhoClicked().equals(player)) return;
        e.setCancelled(true);

        if(e.getRawSlot()==-999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
            player.closeInventory();
            return;
        }

        if(e.getCurrentItem()==null) return;
        if(e.getClick().equals(ClickType.DOUBLE_CLICK)) return;
        if(main.unusableSlots.contains(e.getRawSlot()+18)) return;

        if(e.getRawSlot()==executeButtonSlot) {
            if(status.equals(Status.AVAILABLE)) {
                double exp = getExpInInv();
                double percent = (itemData.getExpPercentage() + exp) % 100;
                int level = itemData.getLevel() +  (int) ((itemData.getExpPercentage() + exp) / 100);

                if(level >= LevelData.runeMaxLevel) {
                    itemData.setLevel(LevelData.runeMaxLevel);
                    itemData.setExp(0);
                }
                else {
                    itemData.setLevel(level);
                    itemData.setExp((long) (percent * itemData.getMaxExp() / 100));
                    itemData.setExp(1);
                }
                for(int i : runeSlots) {
                    inv.setItem(i, null);
                }
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                reloadButton();
                inv.setItem(equipmentSlot, itemData.getItemStack());
            }
            else if(status.equals(Status.NO_EQUIPMENT)) {
                Msg.warn(player, "먼저 강화할 룬을 넣어주세요.");
            }
            else if(status.equals(Status.UNAVAILABLE)) {
                Msg.warn(player, "강화에 사용할 수 없는 아이템이 들어가 있습니다.");
            }
            else if(status.equals(Status.NO_EXP)) {
                Msg.warn(player, "강화 재료로 사용할 룬을 넣어주세요.");
            }
            else if(status.equals(Status.OVER_LEVEL)) {
                Msg.warn(player, "이미 최대 레벨에 달성한 장비입니다.");
            }
            return;
        }

        for(int i : runeSlots) {
            if(e.getRawSlot()==i) {
                MailBoxManager.giveOrMail(player, e.getCurrentItem());
                e.setCurrentItem(null);
                reloadButton();
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                return;
            }
        }

        if(e.getRawSlot()==equipmentSlot) {
            MailBoxManager.giveOrMail(player, e.getCurrentItem());
            e.setCurrentItem(null);
            itemData = null;
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
            reloadButton();
            return;
        }

        ItemData item = new ItemData(e.getCurrentItem());
        if(!item.isRune()) return;

        if(e.getRawSlot()>36) {
            if(status.equals(Status.NO_EQUIPMENT)) {
                inv.setItem(equipmentSlot, e.getCurrentItem());
                e.setCurrentItem(null);
                itemData = item;
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                reloadButton();
            }

            else {
                for(int slot : runeSlots) {
                    if(inv.getItem(slot)==null) {
                        inv.setItem(slot, e.getCurrentItem());
                        e.setCurrentItem(null);
                        reloadButton();
                        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        return;
                    }
                }
                Msg.warn(player, "모든 슬롯이 꽉 찼습니다.");
            }
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if(!Msg.recolor(e.getView().getTitle()).equals(guiName)) return;
        if(e.getPlayer().equals(player)) {
            close();
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if(e.getPlayer().equals(player)) close();
    }

    public void close() {
        boolean mailed = true;
        if(!status.equals(Status.NO_EQUIPMENT)) mailed = MailBoxManager.giveOrMail(player, inv.getItem(equipmentSlot));
        for(int slot : runeSlots) {
            if(inv.getItem(slot)!=null) {
                mailed = mailed && MailBoxManager.giveOrMail(player, inv.getItem(slot));
            }
        }
        if(!mailed) {
            Msg.warn(player, "인벤토리가 가득차 일부 아이템이 메일로 전송되었습니다.");
        }
        HandlerList.unregisterAll(this);
    }
}
