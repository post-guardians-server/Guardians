package me.rukon0621.guardians.mailbox;

import me.rukon0621.callback.ProxySender;
import me.rukon0621.guardians.helper.DataBase;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Serializer;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.offlineMessage.OfflineMessageManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MailBoxManager {
    private static final main plugin = main.getPlugin();
    private static final int maxStacks = 100;
    private static final Map<OfflinePlayer, List<List<ItemStack>>> waitingItems = new HashMap<>();
    private static final Set<OfflinePlayer> isOnSending = new HashSet<>();

    public static List<ItemStack> getMailData(OfflinePlayer player) {
        List<ItemStack> items;
        DataBase db = new DataBase();
        ResultSet set = db.executeQuery(String.format("SELECT mail FROM playerData WHERE uuid = '%s'", player.getUniqueId()));
        try {
            set.next();
            items = (List<ItemStack>) Serializer.deserializeBukkitObject(set.getBytes(1));
            set.close();
        } catch (SQLException e) {
            items = new ArrayList<>();
            e.printStackTrace();
        }
        db.close();
        return items;
    }


    public static void setMailData(OfflinePlayer player, List<ItemStack> items) {
        DataBase db = new DataBase();
        try {
            PreparedStatement statement = db.getConnection().prepareStatement(String.format("UPDATE playerData SET mail = ? WHERE uuid = '%s'", player.getUniqueId()));
            statement.setBytes(1, Serializer.serializeBukkitObject(items));
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db.close();
        if(waitingItems.getOrDefault(player, new ArrayList<>()).size()>0) {
            List<ItemStack> list = getMailData(player);
            list.addAll(waitingItems.get(player).get(0));
            waitingItems.get(player).remove(0);
            setMailData(player, list);
            return;
        }
        isOnSending.remove(player);
        waitingItems.remove(player);
    }

    public static void openMail(Player player) {
        if(isOnSending.contains(player)) {
            Msg.warn(player, "메일함을 다시 열어주세요.");
            return;
        }
        new MailBoxWindow(player);
    }

    public static void sendMail(OfflinePlayer player, ItemStack item) {
        ArrayList<ItemStack> items = new ArrayList<>();
        items.add(item);
        sendAll(player, items);
    }

    /**
     * 아이템을 주거나 인벤에 공간이 부족하면 메일로 전송
     *
     * @param player 아이템을 지급 받을 플레이어
     * @param item   지급할 아이템
     * @return 인벤토리에 공간이 없어서 메일이 전송되면 false, 인벤토리에 성공적으로 들어가면 true 반환
     */
    public static boolean giveOrMail(Player player, ItemStack item) {
        return giveOrMail(player, item, true);
    }

    public static boolean giveOrMail(Player player, ItemStack item, boolean sendMessage) {
        if(InvClass.hasEnoughSpace(player.getInventory(), item)) {
            player.getInventory().addItem(item);
            return true;
        }
        else {
            sendMail(player, item);
            if(sendMessage) Msg.warn(player, "인벤토리에 공간이 부족하여 아이템이 메일로 전송되었습니다.");
            return false;
        }
    }

    public static void giveAllOrMailAll(Player player, List<ItemStack> items) {
        Iterator<ItemStack> itr = items.iterator();
        while(itr.hasNext()) {
            ItemStack item = itr.next();
            if(InvClass.hasEnoughSpace(player.getInventory(), item)) {
                player.getInventory().addItem(item);
                itr.remove();
            }
            else break;
        }
        if(items.size() > 0) {
            sendAll(player, items);
            Msg.warn(player, "인벤토리에 공간이 부족하여 아이템이 메일로 전송되었습니다.");
        }
    }

    public static void sendAll(OfflinePlayer player, List<ItemStack> items) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!isOnSending.add(player)) {
                    if(!waitingItems.containsKey(player)) waitingItems.put(player, new ArrayList<>());
                    List<List<ItemStack>> list = waitingItems.get(player);
                    list.add(items);
                    return;
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<ItemStack> list = getMailData(player);
                        list.addAll(0, items);

                        if(list.size()>=maxStacks / 2) {
                            int left = maxStacks - list.size();

                            if(left<0) {
                                for(int i = 0; i < -left; i++) {
                                    list.remove(list.size() - 1);
                                }
                                String str = String.format("&4메일함에 공간이 부족해 가장 오래된 %d칸의 일부 아이템 영구 소멸되었습니다.", -left);
                                if(player.isOnline()) {
                                    Player onp = player.getPlayer();
                                    Msg.send(onp, str);
                                }
                                else OfflineMessageManager.sendOfflineMessage(player, str);
                            }
                            else {
                                String str = String.format("&c메일함에 여유공간이 &4%d칸 &c남았습니다.", left);
                                if(player.isOnline()) {
                                    Player onp = player.getPlayer();
                                    Msg.send(onp, str);
                                }
                                else OfflineMessageManager.sendOfflineMessage(player, str);
                            }
                        }
                        setMailData(player, list);
                    }
                }.runTaskLaterAsynchronously(plugin, 5);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(player == null) return;
                        new ProxySender(null, "mailSend", player.getName());
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    public static void warningMailStacks(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<ItemStack> mails = getMailData(player);
                if(mails.size() < maxStacks/2) return;
                int left = maxStacks - mails.size();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(left<=0) Msg.warn(player, "&4메일함이 가득찼습니다! 여기서 메일을 더 받으면 오래된 아이템이 완전히 소멸됩니다.");
                        else Msg.warn(player, String.format("&c메일함에 여유공간이 &4%d칸 &c남았습니다.", left));
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }
}