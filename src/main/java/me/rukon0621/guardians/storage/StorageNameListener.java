package me.rukon0621.guardians.storage;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static me.rukon0621.guardians.main.pfix;

public class StorageNameListener implements Listener {
    private static final Set<Player> playersDuringSetting = new HashSet<>();
    private final Player player;
    private final Storage storage;
    private boolean answered = false;
    private final CountDownLatch latch;

    public StorageNameListener(Player player, Storage storage) {
        player.closeInventory();
        this.player = player;
        this.storage = storage;
        latch = new CountDownLatch(1);
        playersDuringSetting.add(player);
        Msg.send(player, " ");
        Msg.send(player, "&e15초 내에 상자의 이름을 입력해주세요.", pfix);
        Msg.send(player, "&7     ※ 색 코드를 사용할 수 있습니다.");

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    latch.await(15 * 1000L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!answered) {
                    playersDuringSetting.remove(player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            StorageManager.openChestSelectGUI(player);
                            Msg.warn(player, "입력 시간이 초과되었습니다.", pfix);
                            HandlerList.unregisterAll(StorageNameListener.this);
                        }
                    }.runTask(main.getPlugin());
                }
            }
        }.runTaskAsynchronously(main.getPlugin());
        Bukkit.getPluginManager().registerEvents(this, main.getPlugin());
    }

    public static boolean isPlayerDuringSetting(Player player) {
        return playersDuringSetting.contains(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(!e.getPlayer().equals(player)) return;
        storage.setName(e.getMessage());
        HandlerList.unregisterAll(this);
        e.setCancelled(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                playersDuringSetting.remove(player);
                answered = true;
                latch.countDown();
                StorageManager.saveStorage(player, storage);
                Map<Integer, String> map = StorageManager.getBoughtData(player);
                map.put(storage.getIndex(), storage.getName());
                StorageManager.setBoughtData(player, map);
                Msg.send(player, "상자의 이름이 성공적으로 설정되었습니다.", pfix);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        StorageManager.openChestSelectGUI(player);
                    }
                }.runTask(main.getPlugin());
            }
        }.runTaskAsynchronously(main.getPlugin());

    }
}
