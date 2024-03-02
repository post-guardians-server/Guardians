package me.rukon0621.guardians.helper;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.RukonUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.main.mainChannel;

public class ChannelUtil {

    private ChannelUtil() {
    }

    /**
     * 이 서버의 데이터를 완전히 저장한 이후에 서버를 이동시킴
     * @param player player
     * @param proxyServerInternalName channel name
     * @return 서버를 이동하지 못했으면 false 이동하려고 시도했으면 true
     */
    public static boolean moveChannel(Player player, String proxyServerInternalName) {
        if(LogInOutListener.getLoadingPlayers().contains(player.getName())||LogInOutListener.getSavingPlayers().contains(player.getName())) {
            return false;
        }
        LogInOutListener.getSavingPlayers().add(player.getName());
        CountDownLatch latch = new CountDownLatch(LogInOutListener.dataCategories);
        LogInOutListener.saveAllDataToDB(player, latch);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                LogInOutListener.clearAllCacheOfPlayer(player);
                LogInOutListener.getSavingPlayers().remove(player.getName());
                LogInOutListener.addLogoutBlock(player);
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("connect");
                out.writeUTF(player.getName());
                out.writeUTF(proxyServerInternalName);
                player.sendPluginMessage(main.getPlugin(), mainChannel, out.toByteArray());
            }
        }.runTaskAsynchronously(main.getPlugin());
        return true;
    }


}
