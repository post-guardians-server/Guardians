package me.rukon0621.guardians.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rukon0621.buff.RukonBuff;
import me.rukon0621.callback.ProxyCallBack;
import me.rukon0621.dungeonwave.WaveData;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.events.GuardiansLoginEvent;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.offlineMessage.OfflineMessageManager;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.pay.PaymentData;
import me.rukon0621.ridings.RukonRiding;
import me.rukon0621.rpvp.RukonPVP;
import me.rukon0621.rpvp.data.RankData;
import me.rukon0621.teseion.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class LogInOutListener implements Listener, PluginMessageListener {
    private static boolean isServerFullyEnabled = false;
    private static final Set<Player> unloadedPlayers = new HashSet<>(); //서버가 완전히 켜지기 전에 들어온 플레이어들.
    private static final main plugin = main.getPlugin();
    private static final Set<String> loadingPlayers = new HashSet<>();
    private static final Set<String> savingPlayers = new HashSet<>();
    public static final int dataCategories = 7;
    private static final Set<Player> logoutEventBlocked = new HashSet<>();
    private static final Set<String> proxyPlayerNames = new HashSet<>();
    private static final Set<String> proxyLogin = new HashSet<>();
    private static final Map<Player, String> previousStories = new HashMap<>();

    private static final Map<String, Couple<String, Boolean>> ptpMap = new HashMap<>();
    private boolean proxyPlayerLoaded = false;

    public LogInOutListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, main.mainChannel, this);
    }

    /**
     * Should be executed in sync
     * @param player player
     */
    public static void saveAllDataAndLockSaving(Player player) {
        CountDownLatch latch = new CountDownLatch(LogInOutListener.dataCategories);
        logoutEventBlocked.add(player);
        PlayerData.saveData(player, latch);
        DialogQuestManager.savePlayerDqData(player, latch);
        EquipmentManager.saveEquipmentsToDataBase(player, latch);
        PaymentData.saveDataToDatabase(player, latch);
        WaveData.getWaveData(player).saveAllDataToDatabase(latch);
        RankData.saveDataToDatabase(player, latch);
        Main.getPlugin().getTeseionManager().savePlayerData(player, latch);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logoutEventBlocked.remove(player);
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Should be executed in sync
     * @param player player
     * @param latch latch
     */
    public static void saveAllDataToDB(Player player, CountDownLatch latch) {
        PlayerData.saveData(player, latch);
        DialogQuestManager.savePlayerDqData(player, latch);
        EquipmentManager.saveEquipmentsToDataBase(player, latch);
        PaymentData.saveDataToDatabase(player, latch);
        WaveData.getWaveData(player).saveAllDataToDatabase(latch);
        RankData.saveDataToDatabase(player, latch);
        Main.getPlugin().getTeseionManager().savePlayerData(player, latch);
    }

    public static void fullyEnableServer() {
        isServerFullyEnabled = true;
        for(Player player : unloadedPlayers) {
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, (Component) null));
        }
        unloadedPlayers.clear();
    }

    public static Set<Player> getUnloadedPlayers() {
        return unloadedPlayers;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!proxyPlayerLoaded) {
                    proxyPlayerLoaded = true;
                    new ProxyCallBack(e.getPlayer(), "getProxyPlayers") {
                        @Override
                        protected void constructExtraByteData(ByteArrayDataOutput byteArrayDataOutput) {}

                        @Override
                        public void done(ByteArrayDataInput in) {
                            String v;
                            while(!(v = in.readUTF()).equals("end")) proxyPlayerNames.add(v);
                            System.out.println("proxied player name is fully loaded.");
                        }
                    };
                }
            }
        }.runTaskLater(main.getPlugin(), 40);
        Player player = e.getPlayer();
        player.teleportAsync(LocationSaver.getLocation("tutoblack"));
        if(!isServerFullyEnabled) {
            e.setJoinMessage(Msg.color("&7[ &9! &7] &f" + player.getName()));
            unloadedPlayers.add(player);
            return;
        }
        e.setJoinMessage(Msg.color("&7[ &e+ &7] &f" + player.getName()));
        if(savingPlayers.contains(player.getName())) {
            player.kick(Component.text("5초 정도 후 다시 접속해주시기 바랍니다."));
            return;
        }

        player.getInventory().clear();
        //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 99999999, 120, false, false, false));
        player.setNoDamageTicks(0);
        loadingPlayers.add(player.getName());
        player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1, 0.8f);
        SoloMode.soloMode(player, true);
        PlayerData.setPlayerStun(player, true);
        Msg.sendTitle(player, "&e서버에 로그인하는 중입니다...", "&f데이터를 불러오고 모드 및 클라이언트를 감지하고 있습니다.", 60, 10, 20);
        int delay = 120;

        //OP거나 PVP 매칭이라면
        if(player.isOp() || RukonPVP.inst().getPvpManager().getMatchedPlayers().contains(player.getUniqueId().toString())) {
            delay = 1;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                //DataBase
                final long time = System.currentTimeMillis();
                CountDownLatch latch = new CountDownLatch(1);
                PlayerData pdc = new PlayerData(player, true, latch);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            latch.await();
                        } catch (InterruptedException ex) {
                            System.out.println("플레이어의 정보가 초기화되었습니다. : " + player.getName() + " | 소요시간(ms) : " + (System.currentTimeMillis() - time));
                            ex.printStackTrace();
                        }
                        if(pdc.isReset()) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    SoloMode.soloMode(player, true);
                                }
                            }.runTask(plugin);
                            return;
                        }
                        CountDownLatch latch = new CountDownLatch(dataCategories);
                        PlayerData.loadPlayerStatFromDatabase(player, latch); //2
                        DialogQuestManager.loadPlayerDqData(player, latch);
                        PaymentData.loadDataFromDataBase(player, latch);
                        RankData.loadDataFromDataBase(player, latch);
                        new WaveData(player, latch);
                        Main.getPlugin().getTeseionManager().loadPlayerData(player, latch);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                System.out.println("플레이어가 로그인하고 데이터베이스에서 모든 정보를 불러왔습니다. : " + player.getName() + " | 소요시간(ms) : " + (System.currentTimeMillis() - time));
                                login(player);
                                plugin.getServer().getPluginManager().callEvent(new GuardiansLoginEvent(player, proxyLogin.remove(player.getName())));
                            }
                        }.runTask(plugin);
                    }
                }.runTaskAsynchronously(plugin);
            }
        }.runTaskLater(plugin, delay);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        previousStories.remove(player);
        main.getPlugin().getAfkManager().clearAfkData(player);
        e.setQuitMessage(Msg.color("&7[ &c- &7] &f" + player.getName()));
        if(unloadedPlayers.contains(player)) {
            unloadedPlayers.remove(player);
            return;
        }

        ChatEventListener.getBlockedPlayers().remove(player);
        if(logoutEventBlocked.remove(player)) {
            System.out.println("BLOCKED - " + player.getName());
            return;
        }
        else {
            System.out.println("PASS - " + player.getName());
        }

        if(savingPlayers.contains(player.getName())) return;
        if(loadingPlayers.contains(player.getName())) return;

        player.closeInventory();

        PlayerData pdc = new PlayerData(player);
        /*
        if(AreaManger.getArea(pdc.getArea()).pvpEnabled()) {
            if(DamagingListener.getRemainCombatTime(player)!=-1) {
                DamagingListener.stealItem(player);
                DamagingListener.deathPenalty(player);
                OfflineMessageManager.sendOfflineMessage(player, "PVP 허용 섬에서 전투중 접속을 종료하여 일부 아이템을 잃고 사망 처리 되었습니다.");
                AreaManger.getArea(pdc.getArea()).warp(player);
            }
        }
         */
        //DataBase
        logout(player, new CountDownLatch(1));
    }

    public static void logout(Player player, CountDownLatch lat) {
        if(new PlayerData(player).getStoryCode()==0) return;
        savingPlayers.add(player.getName());
        CountDownLatch latch = new CountDownLatch(dataCategories);
        saveAllDataToDB(player, latch);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lat.countDown();
                clearAllCacheOfPlayer(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        savingPlayers.remove(player.getName());
                    }
                }.runTaskLater(plugin, 60);

            }
        }.runTaskAsynchronously(plugin);
    }

    public static void login(Player player) {
        RukonRiding.inst().getRideManager().optimizeRidingCache(player);
        PlayerData pdc = new PlayerData(player);
        SoloMode.soloMode(player, false);
        if(pdc.getStoryCode()>=10) SoloMode.soloMode(player, false);
        PlayerData.setPlayerStun(player, false);
        SkillManager.reloadPlayerSkill(player);

        if(!player.isOp()) player.setGameMode(GameMode.SURVIVAL);

        if(previousStories.containsKey(player)) {
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1.0F, 0.8F);
            PlayerData.setPlayerSlowStun(player, true);
            new BukkitRunnable() {
                public void run() {
                    StoryManager.readStory(player, LogInOutListener.previousStories.get(player));
                    LogInOutListener.previousStories.remove(player);
                }
            }.runTaskLater(plugin, 60L);
        }

        if(pdc.getArea().equals("메리스")) {
            AreaManger.getArea("루테티아").warp(player, true);
            Msg.send(player, "크리스마스 이벤트가 끝나 메리스에서 루테티아로 귀환했습니다.");
        }

        /*
        //신규 튜토리얼 관련 초기화
        if (pdc.getLevel() < 5 && !StoryManager.getReadStory(player).contains("flag_tutorial"))
            new BukkitRunnable() {
                public void run() {
                    pdc.setLevel(1);
                    pdc.setExp(0L);
                    player.getInventory().remove(Material.ENCHANTED_BOOK);
                    DialogQuestManager.resetPlayerDqData(player);
                    StoryManager.addStory(player, "flag_tutorial");
                    Msg.sendTitle(player, "&e중요 알림", "초반 튜토리얼이 변경되어 5레벨 미만 플레이어들의 튜토리얼 진행 현황이 초기화되었습니다.", 100, 20, 20);
                    Msg.send(player, "초반 튜토리얼이 변경되어 5레벨 미만 플레이어들의 튜토리얼 진행 현황이 초기화되었습니다.", pfix);
                    player.teleport(LocationSaver.getLocation("start"));
                }
            }.runTaskLater(plugin, 20L);
         */



        //Offline Message
        new BukkitRunnable() {
            @Override
            public void run() {
                loadingPlayers.remove(player.getName());
                OfflineMessageManager.readOfflineMessage(player);
                MailBoxManager.warningMailStacks(player);
                PotionManager.effectRemove(player, PotionEffectType.SLOW);
            }
        }.runTaskLater(plugin, 60);
    }

    public static void addLogoutBlock(Player player) {
        logoutEventBlocked.add(player);
    }

    public static Set<String> getProxyPlayerNames() {
        return proxyPlayerNames;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if(!channel.equals(main.mainChannel)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if(subChannel.equals("join")) {
            proxyPlayerNames.add(in.readUTF());
        }
        else if (subChannel.equals("firstLogin")) {
            proxyLogin.add(in.readUTF());
        }
        else if (subChannel.equals("quit")) proxyPlayerNames.remove(in.readUTF());
    }

    public static Set<String> getLoadingPlayers() {
        return loadingPlayers;
    }

    public static Set<String> getSavingPlayers() {
        return savingPlayers;
    }

    public static Set<Player> getLogoutEventBlocked() {
        return logoutEventBlocked;
    }

    public static void clearAllCacheOfPlayer(Player player) {
        PlayerData.removePlayerCache(player);
        PaymentData.removePlayerCache(player);
        ChatEventListener.clearPlayerCache(player);
        WaveData.removePlayerCache(player);
        RankData.clearRankData(player);
        RukonBuff.inst().getBuffManager().getPlayerBuffDataMap().remove(player);
        System.out.println(player.getName() + "님의 캐시 데이터가 삭제되었습니다.");
    }

    public static Map<Player, String> getPreviousStories() {
        return previousStories;
    }

    public static Map<String, Couple<String, Boolean>> getPtpMap() {
        return ptpMap;
    }
}
