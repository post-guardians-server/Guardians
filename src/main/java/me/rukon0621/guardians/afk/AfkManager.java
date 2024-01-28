package me.rukon0621.guardians.afk;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import me.rukon0621.callback.ProxyCallBack;
import me.rukon0621.guardians.helper.Couple;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Rand;
import me.rukon0621.guardians.main;
import me.rukon0621.rpvp.PvpManager;
import me.rukon0621.rpvp.RukonPVP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static me.rukon0621.guardians.main.getPlugin;

public class AfkManager implements Listener {

    public static void startAFK(Player player) {
        new ProxyCallBack(player, "startAfk") {

            @Override
            protected void constructExtraByteData(ByteArrayDataOutput byteArrayDataOutput) {}

            @Override
            public void done(ByteArrayDataInput in) {
                if(!in.readUTF().equals("fail")) return;
                Msg.warn(player, "잠수 채널에 연결하지 못했습니다. 잠시 후에 다시 시도해주세요.");
            }
        };
    }

    //private static final int POINT_PER_SECOND = 30;

    //private static final int MAX_POINT = 720;

    private final Map<String, Couple<Long, Location>> afkingMap = new HashMap<>();

    private final Map<UUID, Long> afkStart = new HashMap<>();
    private final Map<UUID, Location> afkLoc = new HashMap<>();
    private final Set<UUID> afkWarned = new HashSet<>();


    public void clearAfkData(Player player) {
        UUID uuid = player.getUniqueId();
        afkStart.remove(uuid);
        afkLoc.remove(uuid);
        afkWarned.remove(uuid);
    }

    public AfkManager() {
        main.getPlugin().getServer().getPluginManager().registerEvents(this, main.getPlugin());

        new BukkitRunnable() {
            @Override
            public void run() {
                Set<UUID> kickPlayers = new HashSet<>();
                PvpManager pvpManager = RukonPVP.inst().getPvpManager();
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(player.isOp()) continue;
                    UUID uuid = player.getUniqueId();
                    if(!afkLoc.containsKey(uuid)) {
                        afkStart.put(uuid, System.currentTimeMillis());
                        afkLoc.put(uuid, player.getLocation());
                    }
                    //기존 거리와 5블록 이상 떨어지면 다시 타이머 돌리기
                    Location afkLocation = afkLoc.get(uuid);
                    if(pvpManager.getUuidInMatching().contains(uuid.toString())
                            || afkLocation.getWorld() == null
                            || !afkLocation.getWorld().equals(player.getWorld())
                            || afkLoc.get(uuid).distanceSquared(player.getLocation()) > 9) {
                        afkStart.put(uuid, System.currentTimeMillis());
                        afkLoc.put(uuid, player.getLocation());
                        afkWarned.remove(uuid);
                        continue;
                    }
                    int min = Rand.randInt(15, 20);
                    if(!afkWarned.contains(uuid) && ((System.currentTimeMillis() - getAfkStartMs(uuid)) / 1000L) >= 60L * 14) {
                        afkWarned.add(uuid);
                        Msg.send(player, " ");
                        Msg.warn(player, "이대로 있으시면 잠시후 잠수 서버로 보내질 수 있습니다. 단순 점프, 이리저리 이동하거나 깔짝거리는 움직임은 잠수로 감지될 수 있으니 주의하십시오.");
                        Msg.send(player, " ");
                    }
                    if(((System.currentTimeMillis() - getAfkStartMs(uuid)) / 1000L) >= 60L * min) {
                        kickPlayers.add(uuid);
                    }
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for(UUID uuid : kickPlayers) {
                            Player p = Bukkit.getPlayer(uuid);
                            if(p == null) continue;
                            startAFK(p);
                        }
                    }
                }.runTask(getPlugin());
            }
        }.runTaskTimerAsynchronously(main.getPlugin(), 200, 100);
    }

    public long getAfkStartMs(UUID uuid) {
        return afkStart.getOrDefault(uuid, System.currentTimeMillis());
    }

    @Deprecated
    public Couple<Long, Location> getAfkingData(Player player) {
        return this.afkingMap.get(player.getUniqueId().toString());
    }

    @Deprecated
    public boolean isInAfk(Player player) {
        return isInAfk(player.getUniqueId().toString());
    }

    @Deprecated
    public boolean isInAfk(String uuid) {
        return this.afkingMap.containsKey(uuid);
    }
}
