package me.rukon0621.guardians.storage;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import me.rukon0621.pay.PaymentData;
import me.rukon0621.pay.blessing.BlessingWindow;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static me.rukon0621.guardians.commands.MoneyCommand.moneyName;
import static me.rukon0621.guardians.main.pfix;

public class StorageManager implements Listener {
    private static final main plugin = main.getPlugin();
    private static HashMap<Player, Storage> usingStorage;
    private static final Set<Player> clickBlocker = new HashSet<>();
    private static final Set<Player> chestSorting = new HashSet<>();

    //모든 인덱스는 원베이스

    public StorageManager() {
        DataBase db = new DataBase();
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS chestData(uuid varchar(36) PRIMARY KEY, bought BLOB");
        for(int i = 1 ; i <= 54 ; i++) {
            sql.append(String.format(", index%d MEDIUMBLOB", i));
        }
        sql.append(")");
        db.executeClose(sql.toString());
        usingStorage = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, main.getPlugin());
    }

    /**
     * 플레이어의 상자 데이터 초기화
     * @param player player
     */
    public static void resetPlayerStorage(Player player) {
        DataBase db = new DataBase();
        String sql = String.format("REPLACE INTO chestData VALUES ('%s'", player.getUniqueId()) + ", NULL".repeat(55) + ")";
        db.execute(sql);
        try {
            PreparedStatement preparedStatement = db.getConnection().prepareStatement(String.format("UPDATE chestData SET bought = ? ,index1 = ? WHERE uuid = '%s'", player.getUniqueId()));
            Map<Integer, String> bought = new HashMap<>();
            bought.put(1, "&f1번 창고");
            Storage storage = new Storage("&f1번 창고", new HashMap<>(), 1);
            preparedStatement.setBytes(1, Serializer.serializeBukkitObject(bought));
            preparedStatement.setBytes(2, Serializer.serializeBukkitObject(storage));
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db.close();
    }

    public static void copyStorageData(String copyUUID, String targetUUID) {
        DataBase db = new DataBase();
        StringBuilder sb = new StringBuilder("REPLACE INTO chestData(uuid, bought");
        for(int i = 1; i <= 54; i++) {
            sb.append(", index").append(i);
        }
        sb.append(")");
        sb.append(" SELECT ").append("'").append(targetUUID).append("'");
        sb.append(", bought");
        for(int i = 1; i <= 54; i++) {
            sb.append(", index").append(i);
        }
        sb.append(" FROM chestData WHERE uuid = '").append(copyUUID).append("'");
        db.execute(sb.toString());
        db.close();
    }

    public static Storage getPlayerStorage(Player player, int index) {
        DataBase db = new DataBase();
        ResultSet set = db.executeQuery(String.format("SELECT index%d FROM chestData WHERE uuid = '%s'", index, player.getUniqueId()));
        try {
            set.next();
            Storage str = (Storage) Serializer.deserializeBukkitObject(set.getBytes(1));
            set.close();
            db.close();
            return str;
        } catch (SQLException e) {
            e.printStackTrace();
            db.close();
            return null;
        }
    }

    public static Map<Integer, String> getBoughtData(Player player) {
        DataBase db = new DataBase();
        ResultSet set = db.executeQuery(String.format("SELECT bought FROM chestData WHERE uuid = '%s'", player.getUniqueId()));
        try {
            set.next();
            Map<Integer, String> map;
            try {
                map = (Map<Integer, String>) Serializer.deserializeBukkitObject(set.getBytes(1));
            } catch (ClassCastException e) {
                map = new HashMap<>();
            }
            set.close();
            db.close();
            return map;
        } catch (SQLException e) {
            resetPlayerStorage(player);
            e.printStackTrace();
            db.close();
            return new HashMap<>();
        }
    }

    public static void setBoughtData(Player player, Map<Integer, String> data) {
        DataBase db = new DataBase();
        try {
            PreparedStatement preparedStatement = db.getConnection().prepareStatement(String.format("UPDATE chestData SET bought = ? WHERE uuid = '%s'",  player.getUniqueId()));
            preparedStatement.setBytes(1, Serializer.serialize(data));
            preparedStatement.executeUpdate();
            preparedStatement.close();
            db.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveStorage(Player player, Storage storage) {
        try {
            DataBase db = new DataBase();
            PreparedStatement preparedStatement = db.getConnection().prepareStatement(String.format("UPDATE chestData SET index%d = ? WHERE uuid = '%s'", storage.getIndex(),  player.getUniqueId()));
            preparedStatement.setBytes(1, Serializer.serializeBukkitObject(storage));
            preparedStatement.executeUpdate();
            preparedStatement.close();
            db.close();
        } catch (SQLException er) {
            er.printStackTrace();
        }
    }
    public static void openChestSelectGUI(Player player) {
        InvClass inv = new InvClass(6, "\uE200\uE200\uE201\uE208");
        if(StorageNameListener.isPlayerDuringSetting(player)) {
            Msg.warn(player, "먼저 상자의 이름을 설정해주세요.");
            player.closeInventory();
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<Integer, String> bought = getBoughtData(player);
                int price = (int) (bought.size()*1500*(bought.size()/5.0));
                for(int i = 1; i <= 54; i++) {
                    ItemClass item;
                    if(bought.containsKey(i)) {
                        item = new ItemClass(new ItemStack(Material.CHEST), bought.get(i));
                        item.addLore("&6쉬프트 우클릭&f으로 창고의 이름을 변경할 수 있습니다.");
                    }
                    else {
                        item = new ItemClass(new ItemStack(Material.IRON_BARS), "&7[ &f창고 추가 구매 &7]");
                        item.addLore(String.format("&e%d%s&f을 지불하여 창고를 하나 더 해금할 수 있습니다.", price, moneyName));
                        item.addLore(" ");
                        item.addLore("&f상자를 더 해금하려면 &6쉬프트+우클릭&f을 눌러주세요.");
                    }
                    inv.setslot(i - 1, item.getItem());
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.openInventory(inv.getInv());
                        clickBlocker.remove(player);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    private static void openChest(Player player, int index) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Storage storage = getPlayerStorage(player, index);
                usingStorage.put(player, storage);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        clickBlocker.remove(player);
                        player.openInventory(storage.getInventory().getInv());
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(e.getView().getTitle().equals("\uE200\uE200\uE201\uE207")) {
            if(e.getClick().equals(ClickType.DOUBLE_CLICK)) {
                e.setCancelled(true);
                if(new PaymentData(player).getRemainOfJackBlessing() == 0) {
                    Msg.warn(player, "창고 정렬 기능을 사용하시려면 작댁신의 가호가 필요합니다.");
                    return;
                }

                if(e.getRawSlot() > 54) {
                    Msg.warn(player, "인벤토리가 아닌 창고를 클릭해주세요.");
                    return;
                }
                if(!InvClass.hasEnoughSpace(player.getInventory(), new ItemStack(Material.BEDROCK))) {
                    Msg.warn(player, "인벤토리를 비워야 창고를 정렬할 수 있습니다.");
                    return;
                }

                chestSorting.add(player);
                player.closeInventory();
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 1);
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);
                return;
            }
            if(main.unusableSlots.contains(e.getRawSlot())) {
                e.setCancelled(true);
                return;
            }
            if(e.getRawSlot()==-999) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.3f);
                openChestSelectGUI(player);
            }
            return;
        }
        if(!e.getView().getTitle().equals("\uE200\uE200\uE201\uE208")) return;
        e.setCancelled(true);
        if(clickBlocker.contains(player)) return;
        if(e.getRawSlot()==-999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.3f);
            player.closeInventory();
            return;
        }
        if(e.getCurrentItem()==null) return;
        if(e.getRawSlot()>53) return;
        if(e.getCurrentItem().getType().equals(Material.CHEST)) {
            //창고 이름 변경
            if(e.getClick().equals(ClickType.SHIFT_RIGHT)) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                PaymentData pdc = new PaymentData(player);
                if(pdc.getRemainOfJackBlessing() == 0) {
                    Msg.warn(player, "작댁신의 가호가 있어야 이용할 수 있는 기능입니다.");
                    new BlessingWindow(player);
                    return;
                }
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Storage storage = getPlayerStorage(player, e.getRawSlot() + 1);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                new StorageNameListener(player, storage);
                            }
                        }.runTask(plugin);
                    }
                }.runTaskAsynchronously(plugin);
                return;
            }

            //창고 열기
            clickBlocker.add(player);
            openChest(player, e.getRawSlot() + 1);
            player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1, 1.3f);
        }
        else {
            if(!e.getClick().equals(ClickType.SHIFT_RIGHT)) return;
            clickBlocker.add(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Map<Integer, String> bought = getBoughtData(player);
                    int price = (int) (bought.size()*1500*(bought.size()/5.0));
                    PlayerData pdc = new PlayerData(player);
                    if(pdc.getMoney() < price) {
                        Msg.send(player, "&c돈이 부족하여 상자를 구매할 수 없습니다.", pfix);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.playSound(player, Sound.BLOCK_CHEST_LOCKED, 1, 0.8f);
                                clickBlocker.remove(player);
                            }
                        }.runTask(plugin);
                        return;
                    }
                    pdc.setMoney(pdc.getMoney()-price);

                    int index = e.getRawSlot() + 1; //구매한 상자의 인덱스 (1 base)
                    Storage storage = new Storage(String.format("&f%d번 창고", index), new HashMap<>(), index);
                    bought.put(index, storage.getName());
                    DataBase db = new DataBase();
                    try {
                        PreparedStatement preparedStatement = db.getConnection().prepareStatement(String.format("UPDATE chestData SET bought = ? ,index%d = ? WHERE uuid = '%s'", index, player.getUniqueId()));
                        preparedStatement.setBytes(1, Serializer.serializeBukkitObject(bought));
                        preparedStatement.setBytes(2, Serializer.serializeBukkitObject(storage));
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    db.close();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.3f);
                            openChestSelectGUI(player);
                        }
                    }.runTaskLater(plugin, 10);
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e) {
        if(!(e.getPlayer() instanceof Player player)) return;
        if(!e.getView().getTitle().equals("\uE200\uE200\uE201\uE207")) return;
        HashMap<Integer, ItemStack> items = new HashMap<>();
        for(int i = 0; i < 54 ; i++) {
            ItemStack item = e.getInventory().getItem(i);
            if(item==null) continue;
            items.put(i, item);
        }
        Storage storage = usingStorage.get(player);
        if(storage==null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if(chestSorting.contains(player)) {
                    Iterator<ItemStack> itr = new ArrayList<>(items.values()).iterator();
                    ArrayList<ItemStack> sorted = new ArrayList<>();
                    while(itr.hasNext()) {
                        ItemStack next = itr.next();
                        boolean found = false;

                        if(next.getType().getMaxStackSize()>1) {
                            for(ItemStack loop : sorted) {
                                if(loop.isSimilar(next)) {
                                    int mergedAmount = loop.getAmount() + next.getAmount();
                                    if(mergedAmount > loop.getType().getMaxStackSize()) {
                                        int left = loop.getType().getMaxStackSize() - loop.getAmount();
                                        loop.setAmount(loop.getType().getMaxStackSize());
                                        next.setAmount(next.getAmount() - left);
                                        continue;
                                    }
                                    loop.setAmount(mergedAmount);
                                    itr.remove();
                                    found = true;
                                    break;
                                }
                            }
                            if(found) continue;
                        }
                        sorted.add(next);
                        itr.remove();
                    }

                    sorted.sort((o1, o2) -> {
                        try {
                            //이름순 정렬
                            int result = Msg.uncolor(o1.getItemMeta().getDisplayName()).compareTo(Msg.uncolor(o2.getItemMeta().getDisplayName()));
                            if(result!=0) return result;

                            //레벨순 정렬
                            ItemData d1 = new ItemData(o1);
                            ItemData d2 = new ItemData(o2);
                            result = d2.getLevel() - d1.getLevel();
                            if(result!=0) return result;

                            //부가속성 품질 정렬
                            return d1.getAttrQuality() - d2.getAttrQuality();
                        } catch (Exception e1) {
                            return 1;
                        }
                    });

                    items.clear();
                    int slot = 0;
                    for(ItemStack item : sorted) {
                        items.put(slot, item);
                        slot++;
                    }
                }
                storage.setItemData(items);
                saveStorage(player, storage);
                PlayerData.savePlayerInv(player);
                usingStorage.remove(player);
                if(chestSorting.remove(player)) {
                    openChest(player, storage.getIndex());
                }
             }
        }.runTaskAsynchronously(plugin);
    }
}
