package me.rukon0621.guardians.GUI.item;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.main.getPlugin;
import static me.rukon0621.guardians.main.pfix;

public class ItemLevelDownWindow implements Listener {

    private enum Result {
        NO_ITEMS,
        NEED_SAME_ITEM,
        NO_COST,
        POSSIBLE,
        ALL_SAME_LEVEL
    }

    private final Player player;
    private static final int[] slots = new int[]{0,1,2,3,4,5,6,7,8};
    private final Inventory inv;
    private Result result;
    private int cost = 0;
    private int targetLevel = -1;

    public ItemLevelDownWindow(Player player) {
        this.player = player;
        InvClass inv = new InvClass(2, "&f\uF000\uF029");
        this.inv = inv.getInv();
        reloadWindow();
        player.openInventory(inv.getInv());
        main.getPlugin().getServer().getPluginManager().registerEvents(this, main.getPlugin());
    }

    private void reloadWindow() {
        String name = null;
        int size = 0;
        cost = 0;

        for(int slot : slots) {
            if(inv.getItem(slot)==null) continue;
            size++;

            ItemData itemData = new ItemData(inv.getItem(slot));

            if(targetLevel==-1) targetLevel = itemData.getLevel();
            else targetLevel = Math.min(itemData.getLevel(), targetLevel);

            if(name==null) name = itemData.getName();

            if(!name.equals(itemData.getName())) {
                name = "!";
                break;
            }
        }

        for(int slot : slots) {
            if(inv.getItem(slot)==null) continue;
            ItemData itemData = new ItemData(inv.getItem(slot));

            if(itemData.getLevel()==targetLevel) continue;
            cost += itemData.getLevel() * 5 * inv.getItem(slot).getAmount();
        }

        PlayerData pdc = new PlayerData(player);
        if(size<2) result = Result.NO_ITEMS;
        else if (cost==0) result = Result.ALL_SAME_LEVEL;
        else if (name.equals("!")) result = Result.NEED_SAME_ITEM;
        else if(pdc.getMoney() < cost) result = Result.NO_COST;
        else result = Result.POSSIBLE;

        ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&6레벨 다운 진행하기");
        item.setCustomModelData(7);

        if(result==Result.NO_ITEMS) {
            item.addLore("&c레벨 다운을 진행할 아이템을 넣어주세요.");
            item.addLore("&c최소 2개의 아이템이 필요합니다.");
            item.addLore(" ");
            item.addLore("&e\uE011\uE00C\uE00C아이템 레벨 다운이란?");
            item.addLore("&f아이템의 레벨이 달라 합쳐지지 않는 현상을 방지하기 위해");
            item.addLore("&f아이템의 레벨을 줄이는 과정입니다.");
            item.addLore("&6넣은 아이템 중 가장 낮은 레벨&f의 아이템으로 아이템 레벨이 통일됩니다.");
        }
        else if (result==Result.ALL_SAME_LEVEL) {
            item.addLore("&6모든 아이템의 레벨이 동일합니다.");
        }
        else if (result==Result.NO_COST) {
            item.addLore("&c레벨 다운을 진행할 비용이 부족합니다.");
            item.addLore(" ");
            item.addLore(String.format("&a\uE015\uE00C\uE00C필요 비용: %d 디나르", cost));
        }
        else if (result==Result.NEED_SAME_ITEM) {
            item.addLore("&c같은 종류의 아이템만 레벨을 다운 할 수 있습니다.");
        }
        else if (result==Result.POSSIBLE) {
            item.addLore("&f클릭하여 레벨 다운을 진행합니다.");
            item.addLore(" ");
            item.addLore(String.format("&e\uE011\uE00C\uE00C레벨 다운 결과: %d 레벨", targetLevel));
            item.addLore(String.format("&a\uE015\uE00C\uE00C필요 비용: %d 디나르", cost));
        }
        inv.setItem(12, item.getItem());
        inv.setItem(13, item.getItem());
        inv.setItem(14, item.getItem());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if(!e.getWhoClicked().equals(player)) return;
        e.setCancelled(true);
        if(e.getRawSlot()==-999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
            player.closeInventory();
        }
        if(e.getClick().equals(ClickType.DOUBLE_CLICK)) return;
        if(e.getCurrentItem()==null) return;

        //아이템 넣기
        if(e.getRawSlot()>=18) {
            if(main.unusableSlots.contains(e.getRawSlot()+36)) return;
            int slot = -1;
            for(int s : slots) {
                if(inv.getItem(s)==null) {
                    slot = s;
                    break;
                }
            }
            if(slot==-1) {
                Msg.warn(player, "더이상 아이템을 넣을 수 없습니다.");
                return;
            }
            ItemData id = new ItemData(e.getCurrentItem());
            if(id.isQuestItem()) {
                Msg.warn(player, "퀘스트 아이템을 레벨 다운할 수 없습니다.");
                return;
            }
            if(id.isEquipment()) {
                Msg.warn(player, "장비는 레벨을 낮출 수 없습니다. &7&m굳이 힘들게 올린 스텟을 왜...ㅠㅠ");
                return;
            }
            if(id.getLevel()<=0) {
                Msg.warn(player, "이 아이템은 레벨 다운을 진행할 수 없습니다.");
                return;
            }

            inv.setItem(slot, e.getCurrentItem());
            e.setCurrentItem(null);
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
            reloadWindow();
            return;
        }

        //아이템 빼기
        else if(e.getRawSlot()>=0&&e.getRawSlot()<=8) {
            MailBoxManager.giveOrMail(player, e.getCurrentItem(), true);
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1.5f);
            e.setCurrentItem(null);
            reloadWindow();
            return;
        }

        //레벨 다운 진행
        if(e.getRawSlot()==12||e.getRawSlot()==13||e.getRawSlot()==14) {
            if(result.equals(Result.POSSIBLE)) {
                PlayerData pdc = new PlayerData(player);
                pdc.setMoney(pdc.getMoney() - cost);

                for(int slot : slots) {
                    if(inv.getItem(slot)==null) continue;
                    ItemData item = new ItemData(inv.getItem(slot));
                    item.setLevel(targetLevel);
                    inv.setItem(slot, item.getItemStack());
                }
                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1, 0.8f);
                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);
                Msg.send(player, "성공적으로 아이템 레벨을 다운시켰습니다.", pfix);
                reloadWindow();
            }
            else {
                Msg.warn(player, "레벨 다운을 진행할 수 없습니다.");
            }
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent e) {
        if(!e.getPlayer().equals(player)) return;
        List<ItemStack> items = new ArrayList<>();
        for(int slot : slots) {
            if(inv.getItem(slot)==null) continue;
            items.add(inv.getItem(slot));
        }
        MailBoxManager.giveAllOrMailAll(player, items);

        new BukkitRunnable() {
            @Override
            public void run() {
                HandlerList.unregisterAll(ItemLevelDownWindow.this);
            }
        }.runTaskLater(getPlugin(), 1);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if(!e.getPlayer().equals(player)) return;
        HandlerList.unregisterAll(this);
    }
}
