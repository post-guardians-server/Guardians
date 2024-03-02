package me.rukon0621.guardians.data;

import me.rukon0621.buff.RukonBuff;
import me.rukon0621.buff.data.Buff;
import me.rukon0621.dungeonwave.WaveData;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.craft.craft.WaitingItem;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.events.GuardiansLoginEvent;
import me.rukon0621.guardians.events.WorldPeriodicEvent;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.listeners.ChatEventListener;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.offlineMessage.OfflineMessageManager;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.guardians.skillsystem.skilltree.SkillTreeManager;
import me.rukon0621.guardians.skillsystem.skilltree.elements.SkillTree;
import me.rukon0621.guardians.storage.StorageManager;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.guild.element.Guild;
import me.rukon0621.pay.PaymentData;
import me.rukon0621.ridings.Riding;
import me.rukon0621.ridings.RukonRiding;
import me.rukon0621.rinstance.Instance;
import me.rukon0621.rinstance.RukonInstance;
import me.rukon0621.rpvp.RukonPVP;
import me.rukon0621.rpvp.data.RankData;
import me.rukon0621.teseion.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class PlayerData {
    //플레이어의 행동 불능 상태여부를 저장하는 데이터
    private static final Set<Player> stunnedPlayer = new HashSet<>();
    private static final Set<Player> slowStunnedPlayer = new HashSet<>();
    private static final Map<Player, HashMap<String, Pair>> attributeAbility = new HashMap<>();
    private static final Map<Player, Map<String, Object>> playerDataMap = new HashMap<>();
    public static final int MAX_BACKPACK_SLOT = 300;
    private static final int MAX_FATIGUE = 10;

    public static Map<String, Object> getPlayerDataMap(Player player) {
        return playerDataMap.get(player);
    }

    public static void reloadStatDatabase() {
        DataBase db = new DataBase();
        db.execute("CREATE TABLE IF NOT EXISTS playerData(uuid varchar(36) PRIMARY KEY)");
        db.execute("ALTER TABLE playerData ADD area text;");
        db.execute("ALTER TABLE playerData ADD money bigint;");
        db.execute("ALTER TABLE playerData ADD storyCode int;");
        db.execute("ALTER TABLE playerData ADD level int;");
        db.execute("ALTER TABLE playerData ADD exp bigint;");
        db.execute("ALTER TABLE playerData ADD skillPoint int;");
        db.execute("ALTER TABLE playerData ADD unlearnedChance int;");
        db.execute("ALTER TABLE playerData ADD skillData blob;");
        db.execute("ALTER TABLE playerData ADD equippedSkills blob;");
        db.execute("ALTER TABLE playerData ADD mail mediumblob;"); //메일에서 실시간 담당
        db.execute("ALTER TABLE playerData ADD offlineMessages blob;"); //오프라인 메세지에서 실시간 담당
        db.execute("ALTER TABLE playerData ADD location blob;");
        db.execute("ALTER TABLE playerData ADD inventory mediumblob;");
        db.execute("ALTER TABLE playerData ADD lastLogin datetime;");
        db.execute("ALTER TABLE playerData ADD waitingItems blob;");
        db.execute("ALTER TABLE playerData ADD playingStory text;");
        db.execute("ALTER TABLE playerData ADD blueprints blob;");
        db.execute("ALTER TABLE playerData ADD energyCore int;");
        db.execute("ALTER TABLE playerData ADD maxEnergyCore int;");
        db.execute("ALTER TABLE playerData ADD riding mediumblob;");
        db.execute("ALTER TABLE playerData ADD chatdata mediumblob;");
        db.execute("ALTER TABLE playerData ADD title text;");
        db.execute("ALTER TABLE playerData ADD titles blob;");
        db.execute("ALTER TABLE playerData ADD userRank int;");
        db.execute("ALTER TABLE playerData ADD buff blob;");
        db.execute("ALTER TABLE playerData ADD pvpPoint int;");
        db.execute("ALTER TABLE playerData ADD deathCount int;");
        db.execute("ALTER TABLE playerData ADD spiritOfHero int;");
        db.execute("ALTER TABLE playerData ADD backpackSlot int;");
        db.execute("ALTER TABLE playerData ADD backpack mediumblob;");
        db.execute("ALTER TABLE playerData ADD variable mediumblob;");
        db.execute("ALTER TABLE playerData ADD marketSlot int;");
        db.execute("ALTER TABLE playerData ADD conBlueprint mediumblob;");
        db.execute("ALTER TABLE playerData ADD afkPoint int;");
        db.execute("ALTER TABLE playerData ADD fatigue int;");
        db.execute("ALTER TABLE playerData ADD completedSampling mediumblob;");
        db.execute("ALTER TABLE playerData ADD progressingSampling mediumblob;");
        db.execute("ALTER TABLE playerData ADD cntWeaponSkin int;");
        db.execute("ALTER TABLE playerData ADD weaponSkins mediumblob;");
        db.execute("ALTER TABLE playerData ADD mute bigint;");
        db.execute("ALTER TABLE playerData ADD offHand blob;");
        db.execute("ALTER TABLE playerData ADD guild text;");
        db.execute("ALTER TABLE playerData ADD voteDays int;");
        db.execute("ALTER TABLE playerData ADD transmit mediumblob;");
        db.close();
    }

    public static void removePlayerCache(Player player) {
        playerDataMap.remove(player);
    }

    public static void saveData(Player player, CountDownLatch latch) {
        String playedStory = StoryManager.getPlayingStory(player);
        Location finalLocation;
        Instance instance = RukonInstance.inst().getInstanceManager().getPlayerInstance(player);
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if(instance != null) finalLocation = instance.getPreviousLocation(player);
        else if(playedStory != null) finalLocation = StoryManager.getPreviousLocation(player);
        else finalLocation = player.getLocation();
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Map<Integer, ItemStack> invData = new HashMap<>();
                    Inventory inv = player.getInventory();
                    for(int i = 0; i<=45;i++) {
                        if(inv.getItem(i)==null) continue;
                        invData.put(i, inv.getItem(i));
                    }
                    DataBase db = new DataBase();
                    PlayerData pdc = new PlayerData(player);

                    PreparedStatement statement = db.getConnection().prepareStatement("UPDATE playerData SET area = ?, money = ?, storyCode = ?, level = ?, exp = ?, skillPoint = ?, unlearnedChance = ?, skillData = ? WHERE uuid = '" + player.getUniqueId() + "'");
                    statement.setString(1, pdc.getArea());
                    statement.setLong(2, pdc.getMoney());
                    statement.setInt(3, pdc.getStoryCode());
                    statement.setInt(4, pdc.getLevel());
                    statement.setDouble(5, pdc.getExp());
                    statement.setInt(6, pdc.getSkillPoint());
                    statement.setInt(7, pdc.getUnlearnChance());
                    statement.setBytes(8, Serializer.serialize(pdc.getSkillData()));
                    statement.executeUpdate();
                    statement.close();

                    if(!main.isVoidLandServer()) {
                        statement = db.getConnection().prepareStatement(String.format("UPDATE playerData SET location = ? WHERE uuid = '%s'", player.getUniqueId()));
                        statement.setBytes(1, Serializer.serializeBukkitObject(finalLocation));
                        statement.executeUpdate();
                        statement.close();
                    }

                    statement = db.getConnection().prepareStatement(String.format("UPDATE playerData SET inventory = ?, lastLogin = ?, waitingItems = ?, playingStory = ?, blueprints = ?, riding = ? WHERE uuid = '%s'", player.getUniqueId()));

                    ZonedDateTime date = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                    statement.setBytes(1, Serializer.serializeBukkitObject(invData));
                    statement.setDate(2, Date.valueOf(date.toLocalDate()));
                    statement.setBytes(3, Serializer.serializeBukkitObject(pdc.getWaitingItems()));

                    statement.setString(4, playedStory);
                    statement.setBytes(5, Serializer.serialize(pdc.getBlueprintsData()));
                    statement.setBytes(6, Serializer.serializeBukkitObject(RukonRiding.inst().getRideManager().getRidingCache(player)));

                    statement.executeUpdate();
                    statement.close();

                    statement = db.getConnection().prepareStatement(String.format("UPDATE playerData SET energyCore = ?, maxEnergyCore = ?, chatData = ?, title = ?, titles = ?, userRank = ?, buff = ?, pvpPoint = ?, deathCount = ?, spiritOfHero = ? WHERE uuid = '%s'", player.getUniqueId()));
                    statement.setInt(1, pdc.getEnergyCore());
                    statement.setInt(2, pdc.getMaxEnergyCore(true));
                    statement.setBytes(3, Serializer.serialize(ChatEventListener.getPlayerChatCache(player)));
                    statement.setString(4, pdc.getTitle());
                    statement.setBytes(5, Serializer.serialize(pdc.getTitles()));
                    statement.setInt(6, pdc.getRank());
                    statement.setBytes(7, Serializer.serializeBukkitObject(RukonBuff.inst().getBuffManager().getBuffs(player)));

                    statement.setInt(8, pdc.getPvpPoint());
                    statement.setInt(9, pdc.getDeathCount());
                    statement.setInt(10, pdc.getSpiritOfHero());
                    statement.executeUpdate();
                    statement.close();

                    statement = db.getConnection().prepareStatement(String.format("UPDATE playerData SET backpackSlot = ?, backpack = ?, variable = ?, marketSlot = ?, conBlueprint = ?, afkPoint = ?, fatigue = ? WHERE uuid = '%s'", player.getUniqueId()));
                    statement.setInt(1, pdc.getBackpackSlot());
                    statement.setBytes(2, Serializer.serializeBukkitObject(pdc.getBackpackData()));
                    statement.setBytes(3, Serializer.serialize(pdc.getVariableData()));
                    statement.setInt(4,pdc.getMarketSlot());
                    statement.setBytes(5,Serializer.serialize(pdc.getConsumableBlueprintsData()));
                    statement.setInt(6, pdc.getAfkPoint());
                    statement.setInt(7, pdc.getFatigue());
                    statement.executeUpdate();
                    statement.close();


                    statement = db.getConnection().prepareStatement(String.format("UPDATE playerData SET completedSampling = ?, progressingSampling = ?, cntWeaponSkin = ?, weaponSkins = ?, mute = ?, offHand = ?, voteDays = ? WHERE uuid = '%s'", player.getUniqueId()));
                    statement.setBytes(1, Serializer.serialize(pdc.getCompletedSamplings()));
                    statement.setBytes(2, Serializer.serialize(pdc.getProgressingSamplings()));
                    statement.setInt(3, pdc.getWeaponSkinCmd());
                    statement.setBytes(4, Serializer.serializeBukkitObject(pdc.getWeaponSkins()));
                    statement.setLong(5, pdc.getMuteMillis());
                    statement.setBytes(6, Serializer.serializeBukkitObject(offHandItem));
                    statement.setInt(7, pdc.getVoteDays());
                    /*
                    if(pdc.getGuildID() == null) {
                        statement.setString(7, null);
                    }
                    else statement.setString(7, pdc.getGuildID().toString());
                     */

                    statement.executeUpdate();
                    statement.close();
                    db.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        }.runTaskAsynchronously(main.getPlugin());
    }

    public static void savePlayerInv(Player player) {
        Map<Integer, ItemStack> invData = new HashMap<>();
        Inventory inv = player.getInventory();
        for(int i = 0; i<=45;i++) {
            if(inv.getItem(i)==null) continue;
            invData.put(i, inv.getItem(i));
        }
        DataBase db = new DataBase();
        try {
            PreparedStatement  statement = db.getConnection().prepareStatement(String.format("UPDATE playerData SET inventory = ? WHERE uuid = '%s'", player.getUniqueId()));
            statement.setBytes(1, Serializer.serializeBukkitObject(invData));
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadPlayerStatFromDatabase(Player player, CountDownLatch latch) {
        loadPlayerStatFromDatabase(player, latch, player.getUniqueId().toString());
    }
    public static void loadPlayerStatFromDatabase(Player player, CountDownLatch latch, String uuid) {
        boolean dailyEvent = false;
        try {
            DataBase db = new DataBase();
            String s = String.format("SELECT * FROM playerData WHERE uuid = '%s'", uuid);
            ResultSet resultSet = db.executeQuery(s);
            PlayerData pdc = new PlayerData(player);
            pdc.setEnvironmentResistance(new HashMap<>());
            resultSet.next();
            pdc.setArea(resultSet.getString(2));
            pdc.setMoney(resultSet.getLong(3));
            pdc.setStoryCode(resultSet.getInt(4));
            pdc.setLevel(resultSet.getInt(5));
            pdc.setExp(resultSet.getLong(6));
            pdc.setSkillPoint(resultSet.getInt(7));
            pdc.setUnlearnChance(resultSet.getInt(8));
            pdc.setSkillData((Set<String>) NullManager.defaultNull(Serializer.deserialize(resultSet.getBytes(9)), new HashSet<>()));

            Location location;
            try {
                location = (Location) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(13)), pdc.getStoryCode() >= 10 ? AreaManger.getArea(pdc.getArea()).getLoc() : LocationSaver.getLocation("start"));
            } catch (NullPointerException e) {
                location = LocationSaver.getLocation("start");
            }
            Location finalLocation = location;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Map<String, Couple<String, Boolean>> ptpMap = LogInOutListener.getPtpMap();
                    if(ptpMap.containsKey(player.getName())) {
                        Player target = Bukkit.getPlayer(ptpMap.get(player.getName()).getFirst());
                        if(ptpMap.get(player.getName()).getSecond()) {
                            player.setGameMode(GameMode.SPECTATOR);
                        }
                        player.teleport(target.getLocation());
                        ptpMap.remove(player.getName());
                    }
                    else player.teleport(finalLocation);
                    RukonPVP.inst().getPvpManager().removePvpPotions(player);
                }
            }.runTaskLater(main.getPlugin(), 10);
            HashMap<Integer, ItemStack> invData = (HashMap<Integer, ItemStack>) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(14)), new HashMap<>());
            player.getInventory().clear();
            for(int i : invData.keySet()) {
                player.getInventory().setItem(i, ItemSaver.reloadItem(invData.get(i)));
            }

            pdc.setLastLogin(resultSet.getDate(15).toLocalDate());
            if(ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate().compareTo(pdc.getLastLogin())>=1) {
                dailyEvent = true;
            }
            ArrayList<WaitingItem> list = (ArrayList<WaitingItem>) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(16)), new ArrayList<>());
            pdc.setWaitingItems(list);
            String playingStory = resultSet.getString(17);
            if(playingStory!=null) {
                LogInOutListener.getPreviousStories().put(player, playingStory);
            }
            pdc.setBlueprintsData((Set<String>) NullManager.defaultNull(Serializer.deserialize(resultSet.getBytes(18)), new HashSet<>()));
            pdc.setEnergyCore(resultSet.getInt(19));
            pdc.setMaxEnergyCore(resultSet.getInt(20));
            RukonRiding.inst().getRideManager().setRidingCache(player, (List<Riding>) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(21)), new ArrayList<>()));
            ChatEventListener.setPlayerChatChannel(player, (Set<String>) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(22)), new HashSet<>()));
            pdc.setTitle(resultSet.getString(23));
            pdc.setTitles((List<String>) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(24)), new ArrayList<>()));
            pdc.setRank(resultSet.getInt(25));

            //RukonBuff.inst().getBuffManager().getPlayerBuffDataMap().put(player, (BuffData) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(26)), new BuffData(new HashMap<>())));
            try {
                RukonBuff.inst().getBuffManager().getPlayerBuffDataMap().put(player.getUniqueId(),
                        (List<Buff>) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(26)) , new ArrayList<>()));
            } catch (Exception e) {
                RukonBuff.inst().getBuffManager().getPlayerBuffDataMap().put(player.getUniqueId(), new ArrayList<>());
            }

            pdc.setPvpPoint(resultSet.getInt(27));
            pdc.setDeathCount(resultSet.getInt(28));
            pdc.setSpiritOfHero(resultSet.getInt(29));
            pdc.setBackpackSlot(resultSet.getInt(30));
            pdc.setBackpackData((List<ItemStack>) NullManager.defaultNull(Serializer.deserializeBukkitObject((resultSet.getBytes(31))), new ArrayList<>()));
            pdc.setVariableData((Map<String, Integer>) NullManager.defaultNull(Serializer.deserialize(resultSet.getBytes(32)), new HashMap<>()));
            pdc.setMarketSlot(resultSet.getInt(33));
            pdc.setConsumableBlueprintsData((Map<String, Integer>) NullManager.defaultNull(Serializer.deserialize(resultSet.getBytes(34)), new HashMap<>()));
            pdc.setAfkPoint(resultSet.getInt(35));
            pdc.setFatigue(resultSet.getInt(36));
            pdc.setCompletedSamplings((Set<String>) NullManager.defaultNull(Serializer.deserialize(resultSet.getBytes(37)), new HashSet<>()));
            pdc.setProgressingSamplings((Map<String, Map<String, Integer>>) NullManager.defaultNull(Serializer.deserialize(resultSet.getBytes(38)), new HashMap<>()));
            pdc.setWeaponSkinCmd(resultSet.getInt(39));
            pdc.setWeaponSkins((List<ItemStack>) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(40)), new ArrayList<>()));
            pdc.setMuteMillis(resultSet.getLong(41));
            player.getInventory().setItemInOffHand((ItemStack) Serializer.deserializeBukkitObject(resultSet.getBytes(42)));
            try {
                pdc.setGuildID(UUID.fromString(resultSet.getString(43)));
                //System.out.println("players guild: " + resultSet.getString(43));
                //System.out.println(pdc.getGuildID());
                Guild.GuildBuff.applyAll(player, Guild.loadGuild(pdc.getGuildID()));
                //System.out.println(Guild.loadGuild(pdc.getGuildID()).getName());
            } catch (NullPointerException e) {
                //e.printStackTrace();
                pdc.setGuildID(null);
                Guild.GuildBuff.removeGuildBuff(player);
            }
            pdc.setVoteDays(resultSet.getInt(44));
            resultSet.close();
            db.close();
            if(dailyEvent) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                WorldPeriodicEvent.dailyEvent(player);
                            }
                        }.runTask(main.getPlugin());
                    }
                }.runTaskAsynchronously(main.getPlugin());
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            System.out.println("Database couldn't find data of " + player.getName());
        }
        System.out.println(player.getName() + " - PlayerData Successfully Loaded.");
        latch.countDown();
        EquipmentManager.loadEquipmentsFromDataBase(player, latch, uuid);
    }


    //플레이어의 행동 불능 상태여부를 결정
    public static void setPlayerStun(Player player, boolean bool) {
        if(bool) {
            stunnedPlayer.add(player);
        } else {
            stunnedPlayer.remove(player);
        }
    }
    public static void setPlayerSlowStun(Player player, boolean bool) {
        if(bool) {
            slowStunnedPlayer.add(player);
            PotionManager.effectGive(player, PotionEffectType.SLOW, 100000, 10);
        } else {
            slowStunnedPlayer.remove(player);
            PotionManager.effectRemove(player, PotionEffectType.SLOW);
        }
    }

    //플레이어가 행동 불능 상태인지 파악
    public static boolean isPlayerStunned(Player player) {
        return (stunnedPlayer.contains(player)||slowStunnedPlayer.contains(player));
    }

    //완전 이동 불능 상태인지 파악
    public static boolean isPlayerPerfectlyStunned(Player player) {
        return stunnedPlayer.contains(player);
    }

    public static boolean isPlayerSlowStunned(Player player) {
        return slowStunnedPlayer.contains(player);
    }

    public static void setTransmitData(UUID uuid, byte[] bytes) {
        try {
            DataBase db = new DataBase();
            PreparedStatement statement = db.getConnection().prepareStatement("UPDATE playerData SET transmit = ? WHERE uuid = ?");
            statement.setBytes(1, bytes);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private final Player player;
    private Map<String, Object> data;
    private boolean reset = false;
    private final main plugin = main.getPlugin();

    //플레이어 대한 모든 정보를 저장
    public PlayerData(Player player, boolean checkNoob, CountDownLatch latch) {
        this(player);

        if(!checkNoob) {
            return;
        }
        if(!playerDataMap.containsKey(player)) {
            playerDataMap.put(player, new HashMap<>());
            data = playerDataMap.get(player);
        }
        attributeAbility.put(player, new HashMap<>());

        //Reset Part
        new BukkitRunnable() {
            @Override
            public void run() {
                int storyCode;
                try {
                    DataBase db = new DataBase();
                    ResultSet set = db.executeQuery(String.format("SELECT storyCode FROM playerData WHERE uuid = '%s'", player.getUniqueId()));
                    set.next();
                    storyCode = set.getInt(1);
                } catch (SQLException e) {
                    storyCode = 0;
                }
                if(storyCode<1) {
                    resetPlayerStatusData(latch);
                    reset = true;
                }
                else  {
                    if(storyCode < 10) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                SoloMode.soloMode(player, true);
                            }
                        }.runTask(plugin);
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            latch.countDown();
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public PlayerData(Player player) {
        this.player = player;
        data = playerDataMap.get(player);
    }

    public LocalDate getLastLogin() {
        return (LocalDate) data.getOrDefault("lastLogin", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate());
    }

    public void setLastLogin(LocalDate date) {
        data.put("lastLogin", date);
    }


    public boolean isDataNull() {
        return data == null;
    }

    /*
    PlayerData는 모든 스텟을 수치화시키고 저장을 하고
    실제적으로 스텟이 반영되는 것은 statusControler와
    onDamage Event에서 처리한다.
     */

    public boolean isReset() {
        return reset;
    }

    //해당 플레이어의 모든 스텟을 초기화 (+튜토리얼 시작 storyCode가 0이라면 초기화)
    public void resetPlayerStatusData(CountDownLatch latch) {
        player.getInventory().clear();

        new BukkitRunnable() {
            @Override
            public void run() {
                Main.getPlugin().getTeseionManager().resetPlayerData(player);
                new PaymentData(player).resetPassData();
                DataBase db = new DataBase();
                db.execute(String.format("REPLACE INTO playerData (uuid) VALUES ('%s');", player.getUniqueId()));
                db.execute(String.format("REPLACE INTO fieldWaveData (uuid) VALUES ('%s');", player.getUniqueId()));
                db.execute(String.format("REPLACE INTO paymentData (uuid) VALUES ('%s');", player.getUniqueId()));
                db.execute(String.format("REPLACE INTO teseionData (uuid) VALUES ('%s');", player.getUniqueId()));
                db.close();
                SkillManager.saveEquipSkill(player, new HashMap<>());
                SkillManager.reloadPlayerSkill(player);
                MailBoxManager.setMailData(player, new ArrayList<>());
                OfflineMessageManager.setMessages(player, new ArrayList<>());
                StorageManager.resetPlayerStorage(player);
                DialogQuestManager.resetPlayerDqData(player);
                new WaveData(player, new CountDownLatch(1));
                RankData.resetPvpData(player, new CountDownLatch(1));
                System.out.println(player.getName() + " - Reset PlayerDataBase.");
                new BukkitRunnable() {
                    @Override
                    public void run() {

                        Set<String> set = new HashSet<>();
                        set.add("all");
                        set.add("channel");
                        set.add("party");
                        set.add("whisper");
                        ChatEventListener.setPlayerChatChannel(player, set);

                        setStoryCode(0);
                        setLevel(1);
                        setExp(0);
                        setMoney(0);
                        setSkillData(new HashSet<>());
                        setSkillPoint(0);
                        setUnlearnChance(3);
                        setArea("이니티움");
                        setCraftSpeed(0);
                        setMaxEnergyCore(100);
                        setEnergyCore(100);
                        setBackpackSlot(9);
                        setBackpackData(new ArrayList<>());
                        setVariableData(new HashMap<>());
                        setConsumableBlueprintsData(new HashMap<>());
                        setCompletedSamplings(new HashSet<>());
                        setProgressingSamplings(new HashMap<>());
                        setWeaponSkins(new ArrayList<>());
                        setWeaponSkinCmd(0);

                        //BASIC STATUS SETTING
                        Stat.HEALTH.setBase(player, 20);
                        Stat.CRT_DAMAGE.setBase(player, 100);
                        Stat.MOVE_SPEED.setBase(player, 100);

                        setRank(0);
                        setBlueprintsData(new HashSet<>());
                        setEnvironmentResistance(new HashMap<>());
                        //RukonBuff.inst().getBuffManager().getPlayerBuffDataMap().put(player, new BuffData(new HashMap<>()));
                        RukonBuff.inst().getBuffManager().getPlayerBuffDataMap().put(player.getUniqueId(), new ArrayList<>());
                        EquipmentManager.resetAllEquipment(player);
                        PlayerData.setPlayerStun(player, false);
                        PotionManager.effectRemove(player, PotionEffectType.SLOW);
                        setTitles(new ArrayList<>());
                        System.out.println(player.getName() + " - reset player database record.");
                        latch.countDown();
                        player.teleportAsync(LocationSaver.getLocation("start"));
                        Bukkit.getPluginManager().callEvent(new GuardiansLoginEvent(player));
                        LogInOutListener.getSavingPlayers().remove(player.getName());
                        LogInOutListener.getLoadingPlayers().remove(player.getName());
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    public static void reloadStatus(Player player) {
        PlayerData pdc = new PlayerData(player);

        int line = 1;

        Set<String> skills = pdc.getSkillData();
        double health = 20.0, moveSpeed = 100,
                armor = 0, damage = 0, craftSpeed = 0, finalDamageMultiply = 0, criticalDamage = 0.3, criticalChance = 0, stunningChance = 0,
                armorIgnore = 0, coolDecrease = 0, stunningDuration = 0, armorProportion = 0,
                armorMUL = 0, healthMUL = 0, regen = 0, evade = 0;
        if(pdc.getArea().equals("루테티아")) moveSpeed = 200;

        Stat.MOVE_SPEED.addBase(player, moveSpeed);
        Stat.HEALTH.addBase(player, health);
        Stat.ARMOR.addBase(player, armor);
        Stat.ATTACK_DAMAGE.addBase(player, damage);
        Stat.CRT_CHANCE.addBase(player, criticalChance);
        Stat.CRT_DAMAGE.addBase(player, criticalDamage);
        Stat.STUN_CHANCE.addBase(player, stunningChance);
        Stat.STUN_DUR.addBase(player, stunningDuration);
        Stat.ABSOLUTE_ARMOR.addBase(player, armorProportion);
        Stat.COOL_DEC.addBase(player, coolDecrease);
        Stat.REGEN.addBase(player, regen);
        Stat.EVADE.addBase(player, evade);

        //배율
        Stat.ATTACK_DAMAGE_PER.addBase(player, Stat.ATTACK_DAMAGE_PER.getBase(player) + finalDamageMultiply);
        Stat.ARMOR_PER.addBase(player, Stat.ARMOR_PER.getBase(player) + armorMUL);
        Stat.HEALTH_PER.addBase(player, Stat.HEALTH_PER.getBase(player) + healthMUL);

        SkillTreeManager manager = main.getPlugin().getSkillTreeManager();
        Set<String> beRemoved = new HashSet<>();

        Iterator<String> itr = skills.iterator();

        while(itr.hasNext()) {
            String skill = itr.next();
            SkillTree sk = manager.getSkillTree(skill);
            if(sk == null) {
                itr.remove();
                continue;
            }
            if(sk.hasStat()) sk.applyBaseStat(player);
            else {
                if(skill.startsWith("멀티 태스킹")) {
                    line += 1;
                }
                else if(skill.startsWith("멀티 코어")) {
                    if(skill.endsWith("1 ]")) {
                        craftSpeed += 10;
                    }
                    else if(skill.endsWith("2 ]")) {
                        craftSpeed += 15;
                    }
                }
            }
        }
        pdc.setCraftLineSize(line);
        pdc.setCraftSpeed(craftSpeed);

        LevelData.reloadIndicator(player);
    }

    public double getTotalPowerCached() {
        return EquipmentManager.getCachedTotalPower().getOrDefault(player.getUniqueId(), -1D);
    }

    //********************************************************//
    //                        MODIFIERS                       //
    //********************************************************//
    //플레이어 데이터에 새로운 요소가 생기면 버전을 높여서 해당 데이터를 초기화
    // STATUS
    public void setArea(String value) {
        data.put("area", value);
    }
    public String getArea() {
        return (String) data.get("area");
    }

    public void setVoteDays(int value) {
        data.put("voteDays", value);
    }
    public int getVoteDays() {
        return ((Number) data.getOrDefault("voteDays", 0)).intValue();
    }

    public void setGuildID(UUID value) {
        data.put("guild", value);
    }

    @Nullable
    public UUID getGuildID() {
        return (UUID) data.get("guild");
    }
    public String getGuildName() {
        return Guild.getGuildNameCache().getOrDefault(player.getUniqueId(), "로딩중...");
    }

    public void setStoryCode(int value) {
        data.put("storyCode", value);
    }
    public int getStoryCode() {
        return ((Number) data.get("storyCode")).intValue();
    }

    public void setSpiritOfHero(int value) {
        data.put("spiritOfHero", value);
    }
    public int getSpiritOfHero() {
        return ((Number) data.getOrDefault("spiritOfHero", 0)).intValue();
    }

    public void setMoney(long value) {
        data.put("money", value);
    }
    public long getMoney() {
        return ((Number) data.get("money")).longValue();
    }

    public void setLevel(int value) {
        data.put("level", value);
    }
    public int getLevel() {
        return ((Number) data.get("level")).intValue();
    }

    public void setExp(long value) {
        data.put("exp", value);
    }
    public long getExp() {
        return ((Number) data.get("exp")).longValue();
    }

    public void setRank(int value) {
        data.put("rank", value);
    }
    public int getRank() {
        return ((Number) data.get("rank")).intValue();
    }
    public void setDeathCount(int value) {
        data.put("deathCount", value);
    }
    public int getDeathCount() {
        return ((Number) data.getOrDefault("deathCount", 0)).intValue();
    }

    public void setPvpPoint(int value) {
        data.put("pvpPoint", value);
    }
    public int getPvpPoint() {
        return ((Number) data.getOrDefault("pvpPoint", 0)).intValue();
    }
    public void setAfkPoint(int value) {
        data.put("afkPoint", value);
    }
    public int getAfkPoint() {
        return ((Number) data.getOrDefault("afkPoint", 0)).intValue();
    }

    public void setSkillPoint(int value) {
        data.put("skillPoint", value);
    }
    public int getSkillPoint(int skillTreeIndex) {
        return skillTreeIndex == 7 ? getCraftSkillPoint() : getSkillPoint();
    }
    public int getSkillPoint() {
        return ((Number) data.get("skillPoint")).intValue();
    }
    public void setCraftSkillPoint(int value) {
        data.put("craftSkillPoint", value);
    }
    public int getCraftSkillPoint() {
        return ((Number) data.getOrDefault("craftSkillPoint", 0)).intValue();
    }

    public void setUnlearnChance(int value) {
        data.put("unlearnedChance", value);
    }
    public int getUnlearnChance() {
        return ((Number) data.get("unlearnedChance")).intValue();
    }

    public Set<String> getSkillData() {
        return (Set<String>) data.get("skillData");
    }
    public void setSkillData(Set<String> value) {
        data.put("skillData", value);
    }

    public Set<String> getBannedSkillData() {
        return (Set<String>) data.getOrDefault("bannedSkillData", new HashSet<>());
    }
    public void setBannedSkillData(Set<String> value) {
        data.put("bannedSkillData", value);
    }

    public boolean addSkill(String skill) {
        return getSkillData().add(skill);
    }
    public boolean removeSkill(String skill) {
        return getSkillData().remove(skill);
    }

    public void setBackpackSlot(int value) {
        data.put("backpackSlot", value);
    }
    public int getBackpackSlot() {
        return ((Number) data.get("backpackSlot")).intValue();
    }

    public void setVariableData(Map<String, Integer> value) {
        data.put("variableData", value);
    }
    public Map<String, Integer> getVariableData() {
        return (Map<String, Integer>) data.get("variableData");
    }

    public int getVar(String varName) {
        return ((Map<String, Integer>) data.get("variableData")).getOrDefault(varName, 0);
    }

    public void setVar(String varName, int value) {
        ((Map<String, Integer>) data.get("variableData")).put(varName, value);
    }

    public List<ItemStack> getBackpackData() {
        return (List<ItemStack>) data.getOrDefault("backpackData", new ArrayList<>());
    }
    public void setBackpackData(List<ItemStack> value) {
        data.put("backpackData", value);
    }

    public void resetAllSkills() {
        getSkillData().clear();
    }

    public boolean hasSkill(String skill) {
        return getSkillData().contains(skill);
    }

    public void setWaitingItems(ArrayList<WaitingItem> waitingItems) {
        ArrayList<WaitingItem> list = getWaitingItems();
        if(list==null) {
            list = new ArrayList<>();
        }
        list.clear();
        list.addAll(waitingItems);
    }

    public ArrayList<WaitingItem> getWaitingItems() {
        if(!data.containsKey("waitingItems")) {
            data.put("waitingItems", new ArrayList<>());
        }
        return (ArrayList<WaitingItem>) data.get("waitingItems");
    }

    public void setBlueprintsData(Set<String> value) {
        data.put("blueprint", value);
    }

    public Set<String> getBlueprintsData() {
        return (Set<String>) data.get("blueprint");
    }

    public void setConsumableBlueprintsData(Map<String, Integer> value) {
        data.put("comBlueprint", value);
    }

    public Map<String, Integer> getConsumableBlueprintsData() {
        return (Map<String, Integer>) data.get("comBlueprint");
    }

    public boolean addBlueprint(String name) {
        return getBlueprintsData().add(name);
    }
    public boolean removeBlueprint(String name) {
        return getBlueprintsData().remove(name);
    }
    public boolean hasBlueprint(String name) {
        return getBlueprintsData().contains(name) || getConsumableBlueprintsData().containsKey(name);
    }

    public void addConsumableBlueprint(String name) {
        getConsumableBlueprintsData().put(name, getConsumableBlueprintsData().getOrDefault(name, 0) + 1);
    }

    /**
     * @param name 청사진의 이름
     * @return 청사진을 가지고 있어서 성공적으로 삭제했다면 true 아니면 false
     */
    public boolean removeConsumableBlueprint(String name) {
        if(!getConsumableBlueprintsData().containsKey(name)) return false;
        int remain = getConsumableBlueprintsData().get(name) - 1;
        if(remain <= 0) getConsumableBlueprintsData().remove(name);
        else getConsumableBlueprintsData().put(name, remain);
        return true;
    }

    public void setCraftLineSize(int value) {
        data.put("craftLineSize", value);
    }
    public int getCraftLineSize(boolean original) {
        if(new PaymentData(player).getRemainOfRukonBlessing() > 0) {
            return ((Number) data.getOrDefault("craftLineSize", original)).intValue() + 1;
        }
        else return ((Number) data.getOrDefault("craftLineSize", original)).intValue();
    }

    public void setCraftSpeed(double value) {
        data.put("craftSpeed", value);
    }
    public double getCraftSpeed() {
        return ((Number) data.get("craftSpeed")).doubleValue();
    }

    public void setAttributeAbility(HashMap<String, Pair> data) {
        attributeAbility.put(player, data);
    }
    public Pair getAttributeAbility(String type) {
        return attributeAbility.get(player).get(type);
    }

    //환경 적응력 관련
    public void setEnvironmentResistance(Map<String, Integer> map) {
        data.put("environmentResistance", map);
    }
    public Map<String, Integer> getEnvironmentResistance() {
        return (Map<String, Integer>) data.get("environmentResistance");
    }
    public HashMap<String, Pair> getAttributeAbility() {
        return attributeAbility.get(player);
    }

    public void setEnergyCore(int value) {
        data.put("energyCore", value);
    }
    public int getEnergyCore() {
        return ((Number) data.getOrDefault("energyCore",0)).intValue();
    }


    public void setMuteMillis(long value) {
        data.put("mute", value);
    }
    public long getMuteMillis() {
        return ((Number) data.getOrDefault("mute",0)).longValue();
    }

    public void setFatigue(int value) {
        data.put("fatigue", value);
    }
    public int getFatigue() {
        return ((Number) data.getOrDefault("fatigue",0)).intValue();
    }

    public int getMaxFatigue() {
        if(new PaymentData(player).getRemainOfBertBlessing() > 0) return MAX_FATIGUE + 5;
        else return MAX_FATIGUE;
    }

    public int getMaxEnergyCore() {
        return getMaxEnergyCore(false);
    }
    public int getMaxEnergyCore(boolean original) {
        PaymentData pyd = new PaymentData(player);
        if(pyd.getRemainOfBertBlessing()>0&&!original) return (int) (1.3 * ((Number) data.getOrDefault("maxEnergyCore",100)).intValue());
        else return ((Number) data.getOrDefault("maxEnergyCore",100)).intValue();
    }
    public void setMaxEnergyCore(int value) {
        data.put("maxEnergyCore", value);
    }

    public void setMarketSlot(int value) {
        data.put("marketSlot", value);
    }
    public int getMarketSlot() {
        return ((Number) data.getOrDefault("marketSlot", 0)).intValue();
    }

    public void setTitle(String title) {
        data.put("title", title);
    }

    public String getTitle() {
        return (String) data.get("title");
    }

    public List<String> getTitles() {
        return (List<String>) data.get("titles");
    }

    public void setTitles(List<String> list) {
        data.put("titles", list);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, Integer> getProgressingSampling(String name) {
        return getProgressingSamplings().getOrDefault(name, new HashMap<>());
    }

    public Set<String> getCompletedSamplings() {
        return (Set<String>) data.get("completedSamp");
    }
    public void setCompletedSamplings(Set<String> set) {
        data.put("completedSamp", set);
    }

    public Map<String, Map<String, Integer>> getProgressingSamplings() {
        return (Map<String, Map<String, Integer>>) data.get("progSamp");
    }
    public void setProgressingSamplings(Map<String, Map<String, Integer>> data) {
        this.data.put("progSamp", data);
    }

    public void setWeaponSkinCmd(int cmd) {
        data.put("cntWeaponSkin", cmd);
    }
    public int getWeaponSkinCmd() {
        return ((Number) data.getOrDefault("cntWeaponSkin", 0)).intValue();
    }

    public void setWeaponSkins(List<ItemStack> items) {
        data.put("weaponSkins", items);
    }
    public List<ItemStack> getWeaponSkins() {
        return (List<ItemStack>) data.get("weaponSkins");
    }

    @NotNull
    public Map<Integer, ItemStack> getTransmitData() {
        try {
            DataBase db = new DataBase();
            PreparedStatement statement = db.getConnection().prepareStatement("SELECT transmit FROM playerData WHERE uuid = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            set.next();
            Map<Integer, ItemStack> data = (Map<Integer, ItemStack>) NullManager.defaultNull(Serializer.deserializeBukkitObject(set.getBytes(1)), new HashMap<>());
            statement.close();
            set.close();
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
    public void setTransmitData(Map<Integer, ItemStack> data) {
        setTransmitData(player.getUniqueId(), Serializer.serializeBukkitObject(data));
    }

}
