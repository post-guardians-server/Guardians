package me.rukon0621.guardians.dialogquest;

import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.Scene;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.pay.PaymentData;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.main.pfix;

public class DialogQuestManager implements Listener {
    public enum QuestType {
        MAIN,
        REPEATABLE,
        SUB_MAIN,
        GUARDIAN_QUEST,
        HIDDEN
    }

    private static final main plugin = main.getPlugin();

    private static final Map<Player, Map<String, Object>> playerDqData = new HashMap<>();
    private static final Map<String, Dialog> dialogData = new HashMap<>();
    private static final Map<String, Quest> questData = new HashMap<>();
    private static final Map<String, ArrayList<String>> registeredData = new HashMap<>();
    private static final Map<Player, Dialog> readingDialog = new HashMap<>();
    private static final Map<Player, Integer> readingPage = new HashMap<>();
    private static final Map<Player, String> settingQuest = new HashMap<>();
    private static final Set<Player> noClick = new HashSet<>();
    private static final Map<Player, Entity> interactingEntity = new HashMap<>();
    private static final Map<Player, QuestInProgress> completingQuest = new HashMap<>();
    private static final Map<Player, Quest> playerNavigating = new HashMap<>();
    private static final Set<Player> skipDialog = new HashSet<>();
    private static final Set<Player> isSkipable = new HashSet<>();
    public static int[] completingSlots = new int[]{9,10,11,18,19,20,27,28,29};
    private static final String dialogGuiName = "&f\uF000\uF011";
    private static final String answerGuiName = "&f\uF000\uF012";
    private static final List<String> dialogFileNames = new ArrayList<>();
    private static final List<String> questFileNames = new ArrayList<>();

    private static final Set<String> dailyResetQuests = new HashSet<>();

    public static final String questItemGuiName = "&f\uF000\uF01B";
    private static boolean isReloading = false;

    public static Map<String, Object> getPlayerDqData(Player player) {
        return playerDqData.get(player);
    }

    public static Map<String, Dialog> getDialogData() {
        return dialogData;
    }

    public static Map<String, Quest> getQuestData() {
        return questData;
    }

    //패스에는 반드시 .yml 을 포함해서 저장
    public static Configure getQuestPathConfig() {
        return new Configure("questPath.yml", FileUtil.getOuterPluginFolder()+"/dialogQuests");
    }

    public static Configure getDialogPathConfig() {
        return new Configure("dialogPath.yml", FileUtil.getOuterPluginFolder()+"/dialogQuests");
    }

    private static Configure getDialogConfig(String name) {
        return new Configure(FileUtil.getOuterPluginFolder()+"/dialogQuests/dialogs/"+getDialogPathConfig().getConfig().getString(name));
    }

    private static Configure getQuestConfig(String name) {
        return new Configure(FileUtil.getOuterPluginFolder()+"/dialogQuests/quests/"+getQuestPathConfig().getConfig().getString(name));
    }

    private static Configure getRegisteredDataConfig() {
        return new Configure("registeredData.yml", FileUtil.getOuterPluginFolder()+"/dialogQuests");
    }

    public static List<String> getDialogFileNames() {
        return dialogFileNames;
    }

    public static List<String> getQuestFileNames() {
        return questFileNames;
    }

    public DialogQuestManager() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reloadAll(new CountDownLatch(2));
        DataBase db = new DataBase();
        db.execute("CREATE TABLE IF NOT EXISTS dialogQuestData(uuid varchar(36) PRIMARY KEY, dialogs blob, quests blob, qip blob, cooltime blob, story blob)");
        db.execute("ALTER TABLE dialogQuestData ADD logs blob;");
        db.close();
    }
    /**
     * 데이터 베이스에 있는 플레이어의 데이터를 초기화하고 인게임에 반영 (Story도 포함)
     * @param player player
     */
    public static void resetPlayerDqData(Player player) {
        playerDqData.put(player, new HashMap<>());
        setReadDialogs(player, new HashSet<>());
        setCompletedQuests(player, new HashSet<>());
        setQuestInProgress(player, new ArrayList<>());
        setQuestCooltime(player, new HashMap<>());
        setQuestLog(player, new ArrayList<>());
        StoryManager.setReadStory(player, new HashSet<>());
        savePlayerDqData(player, new CountDownLatch(1));
    }


    /**
     * 데이터 베이스에 플레이어의 Dialog Quests 정보를 저장 (Story도 포함)
     * @param player player
     */
    public static void savePlayerDqData(Player player, CountDownLatch latch) {
        Set<String> dialogs = DialogQuestManager.getReadDialogs(player);
        Set<String> quests = DialogQuestManager.getCompletedQuests(player);
        ArrayList<QuestInProgress> qips = DialogQuestManager.getQuestsInProgress(player);
        Map<String, Long> questCooltime = DialogQuestManager.getQuestCooltime(player);
        Set<String> stories = StoryManager.getReadStory(player);
        List<String> logs = DialogQuestManager.getQuestLog(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    DataBase db = new DataBase();
                    db.execute(String.format("REPLACE INTO dialogQuestData (uuid) VALUES ('%s');", player.getUniqueId()));
                    PreparedStatement statement = db.getConnection().prepareStatement(String.format("UPDATE dialogQuestData SET dialogs = ?, quests = ?, qip = ?, cooltime = ?, story = ?, logs = ? WHERE uuid = '%s'", player.getUniqueId()));
                    statement.setBytes(1, Serializer.serialize(dialogs));
                    statement.setBytes(2, Serializer.serialize(quests));
                    statement.setBytes(3, Serializer.serializeBukkitObject(qips));
                    statement.setBytes(4, Serializer.serialize(questCooltime));
                    statement.setBytes(5, Serializer.serialize(stories));
                    statement.setBytes(6, Serializer.serialize(logs));
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("DQS 데이터 저장 도중 오류가 발생했습니다. - " + player.getName());
                }
                latch.countDown();
            }
        }.runTaskAsynchronously(main.getPlugin());
    }

    /**
     * 데이터 베이스에서 플레이어의 Dialog Quests 정보를 로드 (Story도 포함)
     * @param player player
     */
    public static void loadPlayerDqData(Player player, CountDownLatch latch) {
        loadPlayerDqData(player, latch, player.getUniqueId().toString());
    }
    public static void loadPlayerDqData(Player player, CountDownLatch latch, String uuid) {
        try {
            DataBase db = new DataBase();
            ResultSet resultSet = db.executeQuery(String.format("SELECT * FROM dialogQuestData WHERE uuid = '%s'", uuid));
            resultSet.next();
            playerDqData.put(player, new HashMap<>());

            Set<String> dialogs = (Set<String>) Serializer.deserialize(resultSet.getBytes(2));
            Set<String> quests = (Set<String>) Serializer.deserialize(resultSet.getBytes(3));
            ArrayList<QuestInProgress> qips = (ArrayList<QuestInProgress>) Serializer.deserializeBukkitObject(resultSet.getBytes(4));
            Map<String, Long> cooltime = (Map<String, Long>) Serializer.deserialize(resultSet.getBytes(5));
            Set<String> stories = (Set<String>) Serializer.deserialize(resultSet.getBytes(6));
            List<String> logs = (List<String>) Serializer.deserialize(resultSet.getBytes(7));

            if(dialogs==null) {
                System.out.println(player.getName() + " - Dialogs NULL");
                setReadDialogs(player, new HashSet<>());
            }
            else setReadDialogs(player, dialogs);
            if(quests==null) {
                System.out.println(player.getName() + " - Quests NULL");
                setCompletedQuests(player, new HashSet<>());
            }
            else setCompletedQuests(player, quests);
            if(qips==null) {
                System.out.println(player.getName() + " - QIPS NULL");
                setQuestInProgress(player, new ArrayList<>());
            }
            else {
                setQuestInProgress(player, qips);
            }
            if(cooltime == null) {
                System.out.println(player.getName() + " - QuestCoolMap NULL");
                setQuestCooltime(player, new HashMap<>());
            }
            else setQuestCooltime(player, cooltime);
            if(stories==null) {
                System.out.println(player.getName() + " - Story NULL");
                StoryManager.setReadStory(player, new HashSet<>());
            }
            else StoryManager.setReadStory(player, stories);
            if(logs==null) {
                System.out.println(player.getName() + " - Logs NULL");
                setQuestLog(player, new ArrayList<>());
            }
            else setQuestLog(player, logs);

            resultSet.close();
            db.close();

            reloadQIP(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(player.getName() + " - DQS Data Successfully Loaded.");
        latch.countDown();
    }

    public static void reloadAll(CountDownLatch latch) {
        readingDialog.clear();
        settingQuest.clear();
        readingPage.clear();
        registeredData.clear();
        interactingEntity.clear();
        completingQuest.clear();
        playerNavigating.clear();
        skipDialog.clear();
        noClick.clear();
        reloadDialogs(latch);
        reloadQuests(latch);
        reloadRegisteredData();
    }

    public static void reloadRegisteredData() {
        Configure config = getRegisteredDataConfig();
        config.saveConfig();
        for(String uuid : config.getConfig().getKeys(false)) {
            registeredData.put(uuid, (ArrayList<String>) (ArrayList<String>) config.getConfig().getList(uuid));
        }
    }

    public static void reloadDialogs(CountDownLatch latch) {
        new BukkitRunnable() {
            @Override
            public void run() {
                isReloading = true;
                dialogData.clear();
                Configure config = getDialogPathConfig();

                if(config.getFile().length()==0L) {
                    Bukkit.getLogger().severe(config.getFile().getName() + " - 파일이 비어있습니다. 작댁 바보 멍청이");
                }

                for(String name : config.getConfig().getKeys(false)) {
                    dialogData.put(name, new Dialog(name, getDialogConfig(name)));
                }
                dialogFileNames.clear();
                for(String path : DialogQuestManager.getDialogPathConfig().getConfig().getKeys(false)) {
                    String value = DialogQuestManager.getDialogPathConfig().getConfig().getString(path);
                    dialogFileNames.add(value);
                }
                latch.countDown();
                isReloading = false;
            }
        }.runTaskAsynchronously(plugin);
    }

    public static void reloadQuests(CountDownLatch latch){
        new BukkitRunnable() {
            @Override
            public void run() {
                questData.clear();
                dailyResetQuests.clear();
                Configure config = getQuestPathConfig();
                if(config.getFile().length()==0L) {
                    Bukkit.getLogger().severe(config.getFile().getName() + " - 파일이 비어있습니다. 작댁 바보 멍청이");
                }
                for(String name : config.getConfig().getKeys(false)) {
                    Quest quest = new Quest(name, getQuestConfig(name));
                    if(quest.isDailyReset()) dailyResetQuests.add(quest.getName());
                    questData.put(name, quest);
                }
                questFileNames.clear();
                for(String path : DialogQuestManager.getQuestPathConfig().getConfig().getKeys(false)) {
                    String value = DialogQuestManager.getQuestPathConfig().getConfig().getString(path);
                    questFileNames.add(value);
                }
                latch.countDown();
            }
        }.runTaskAsynchronously(plugin);
    }

    public static Set<String> getDailyResetQuests() {
        return dailyResetQuests;
    }

    public static void reloadQIP(Player player) {
        ArrayList<QuestInProgress> qips = getQuestsInProgress(player);
        qips.removeIf(qip -> !questData.containsKey(qip.getName()));
        setQuestInProgress(player, qips);
    }

    //플레이어가 추적하는 퀘스트를 재설정
    //자석석 나침반을 사용
    public static void reloadNavigate(Player player) {
        ItemStack leftHand = player.getInventory().getItemInOffHand();
        if(playerNavigating.containsKey(player)) {
            if(leftHand.getType().equals(Material.COMPASS)) {
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                playerNavigating.remove(player);
                return;
            }
        }
        Quest quest = playerNavigating.get(player);
        if(quest==null) {
            if(leftHand.getType().equals(Material.COMPASS)) {
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                playerNavigating.remove(player);
            }
            playerNavigating.remove(player);
            return;
        }
        if(!quest.isNavigatable()) return;
        Location loc = quest.getNavigatingTarget();
        ItemClass it = new ItemClass(new ItemStack(Material.COMPASS), "&6퀘스트 추적 : "+quest.getName());
        it.addLore("#aaccff나침반의 방향을 따라가면 퀘스트 위치에 도달할 수 있습니다.");
        it.addLore("&f");
        it.addLore(String.format("&7X: %.0f | &7Y: %.0f | &7Z: %.0f", loc.getX(), loc.getY(), loc.getZ()));
        it.addLore("&f");
        it.addLore("&c위치 안내를 종료하려면 클릭하십시오.");
        player.setCompassTarget(loc);
        player.getInventory().setItemInOffHand(it.getItem());
    }

    //##################################
    //Dialog
    //##################################
    public static boolean createNewDialog(String path, String name) {
        if(dialogData.containsKey(name)) return false;
        if(!path.endsWith(".yml")) path += ".yml";
        if(!dialogFileNames.contains(path)) dialogFileNames.add(path);
        Configure config = getDialogPathConfig();
        config.getConfig().set(name, path);
        config.saveConfig();
        config = getDialogConfig(name);
        config.getConfig().set(name+".dialogConditions", new ArrayList<>());
        config.getConfig().set(name+".questConditions", new ArrayList<>());
        config.getConfig().set(name+".specialConditions", new ArrayList<>());
        config.getConfig().set(name+".priority", 10);
        config.getConfig().set(name+".dialogs", new ArrayList<>());
        config.saveConfig();
        dialogData.put(name, new Dialog(name, config));
        return true;
        /*
            name:
                dialogConditions:
                - list of dialog name
                questConditions:
                - list of quests name
                priority: 10
                dialogs:
                    - ...
         */
    }

    public static boolean deleteDialog(String name) {
        if(!dialogData.containsKey(name)) return false;
        //config에서 name제거
        Configure config = getDialogConfig(name);
        config.getConfig().set(name, null);
        config.saveConfig();
        if(config.getFile().length()==0L) {
            config.delete("dialogs");
        }
        //경로 제거
        config = getDialogPathConfig();
        config.getConfig().set(name, null);
        config.saveConfig();
        dialogData.remove(name);
        return true;
    }

    public static boolean registerDialog(String name, Entity entity) {
        ArrayList<String> dialogs = getRegisteredDialog(entity);
        if(dialogs.contains(name)) {
            dialogs.remove(name);
            setRegisteredDialog(entity, dialogs);
            return false;
        }
        else {
            dialogs.add(name);
            setRegisteredDialog(entity, dialogs);
            return true;
        }
    }

    private static void interpretDialog(Player player) {
        interpretDialog(player, true);
    }

    /**
     * 대화문을 분석합니다.
     * @param player player
     * @param readComplete 대화문이 다 읽은채로 종료됐는가, false면 break등을 이용해 끊긴걸로 처리 (readDialog에 저장되지 않음)
     */
    private static void interpretDialog(Player player, boolean readComplete) {
        Dialog dialog = readingDialog.get(player);
        int page = readingPage.get(player);
        InvClass inv = getBaseGUI();
        //대화문 시작
        if(page==-1) {
            isSkipable.remove(player);
            ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&e[ &6대화문 시작 &e]");
            it.setCustomModelData(43);
            it.addLore("&f클릭하여 대화문 읽기를 시작합니다.");
            if(new PaymentData(player).getRemainOfJackBlessing() > 0 && getReadDialogs(player).contains(readingDialog.get(player).getName())) {
                it.addLore(" ");
                it.addLore("&e한 번 읽은 대화문은 쉬프트 클릭하면 생략할 수 있습니다.");
                it.addLore("&7(작댁신의 축복 적용중)");
                isSkipable.add(player);
            }
            inv.setslot(13, it.getItem());
            player.openInventory(inv.getInv());
            return;
        }
        //대화문 종료
        else if (page==dialog.getPageSize()) {
            if(readComplete&&dialog.getName()!=null) {
                getReadDialogs(player).add(dialog.getName());
            }
            ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&e[ &6대화문 종료 &e]");
            it.setCustomModelData(43);
            it.addLore("&f클릭하여 대화문을 종료합니다.");
            inv.setslot(13, it.getItem());
            player.openInventory(inv.getInv());
            return;
        }
        String str = dialog.getPage(page);
        String stl = str.toUpperCase(Locale.ROOT);
        //답변하기
        if(stl.startsWith("!ANSWER")) {
            /*
            !ANSWER
            |sting:!CONTUNUE
             */
            inv = getAnswerGUI();

            str = str.replaceAll("!ANSWER\\||!ANSWER \\|", "");
            str = str.replaceAll("<player>", player.getName());
            noClick.add(player);
            String[] answers = str.split("\\|");
            int slot = 13 - answers.length + 1;
            int index = 0;
            for(String ans : answers) {
                ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&e[ &6"+ (index+1) +" &e]");
                it.setCustomModelData(45);
                ans = ans.split(":")[0].trim();
                String[] lines = ans.split("//");
                for(String line : lines) {
                    it.addLore("&e" + line);
                }
                inv.setslot(slot + (index*2), it.getItem());
                index++;
            }

            player.openInventory(inv.getInv());
            new BukkitRunnable() {
                @Override
                public void run() {
                    noClick.remove(player);
                }
            }.runTaskLater(plugin, 5);
        }
        else if(stl.equalsIgnoreCase("!GUARDIANS_QUESTS")) {
            int size = dialog.getPageSize() - page  - 1;
            ZonedDateTime date = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            long serial = date.getYear() + date.getDayOfYear();
            Random rand = new Random(serial*serial);

            List<Integer> indexes = new ArrayList<>();
            for(int i = 0; i < Math.min(4, size); i++) {
                int t = rand.nextInt(1, size + 1);
                if(indexes.contains(t)) {
                    i--;
                    continue;
                }
                indexes.add(t);
            }

            //t = 1~개수

            int slot = 14 - Math.min(4, size);
            int index = 0;
            for(int t : indexes) {
                ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&e[ &6"+ (index+1) +" &e]");
                it.setCustomModelData(45);
                it.addLore(dialog.getPage(page + t));
                inv.setslot(slot + (index*2), it.getItem());
                index++;
            }

            player.openInventory(inv.getInv());
            new BukkitRunnable() {
                @Override
                public void run() {
                    noClick.remove(player);
                }
            }.runTaskLater(plugin, 2);
        }
        //퀘스트 받기
        else if(stl.startsWith("!PS")) {
            String[] datas = str.split(":");
            String sound = datas[1];
            float volume = Float.parseFloat(datas[2]);
            float pitch = Float.parseFloat(datas[3]);
            player.playSound(player.getLocation(), sound, volume, pitch);
            readingPage.put(player, readingPage.get(player)+1);
            interpretDialog(player);
        }
        else if(stl.startsWith("!CMD")) {
            String cmd = str.split(":")[1].trim();
            cmd = cmd.replaceAll("<player>", player.getName());
            Opcmd.opCmd(player, cmd);
            readingPage.put(player, readingPage.get(player)+1);
            interpretDialog(player);
        }
        else if(stl.startsWith("!COMPLETE_OBJECT")) {
            String objName = str.split(":")[1].trim();
            readingPage.put(player, readingPage.get(player)+1);
            interpretDialog(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    completeCustomObject(player, objName);
                }
            }.runTaskLater(plugin, 2);
        }
        else if(stl.startsWith("!QUEST")) {
            Quest quest = questData.get(str.split(":")[1].trim());
            inv.setslot(13, quest.getIcon(player));
            player.openInventory(inv.getInv());
        }
        else if(stl.startsWith("!TP:")) {
            String saver = str.split(":")[1].trim();
            LocationSaver.tpToLoc(player, saver);
            readingPage.put(player, readingPage.get(player)+1);
            interpretDialog(player);
        }
        else if(stl.startsWith("!GIVE:")) {
            if(!DialogQuestManager.getReadDialogs(player).contains(str.split(":")[2].trim())) {
                String saver = str.split(":")[1].trim();
                MailBoxManager.giveOrMail(player, ItemSaver.getItem(saver).getItem(), true);
            }
            readingPage.put(player, readingPage.get(player)+1);
            interpretDialog(player);
        }
        else if(stl.startsWith("!GOTO:")) {
            readingPage.put(player, Integer.valueOf(str.split(":")[1].trim()));
            interpretDialog(player);
        }
        else if(stl.startsWith("!GO:")) {
            readingPage.put(player, readingPage.get(player) + Integer.parseInt(str.split(":")[1].trim()));
            interpretDialog(player);
        }
        else if(stl.equals("!FINISHCLOSE")||str.equals("!CLOSEFINISH")) {
            readingPage.put(player, dialog.getPageSize());
            interpretDialog(player);
            player.closeInventory();
        }
        else if(stl.startsWith("!BREAK")) {
            readingPage.put(player, dialog.getPageSize());
            interpretDialog(player, false);
        }
        else if(stl.startsWith("!READ")) {
            getReadDialogs(player).add(dialog.getName());
            readingPage.put(player, readingPage.get(player) + 1);
            interpretDialog(player);
        }
        else if(stl.startsWith("!CLOSE")) {
            player.closeInventory();
        }
        else if(stl.startsWith("!DIALOG")) {
            String target = str.split(":")[1];
            if(!dialogData.containsKey(target)) {
                Msg.send(player, "&c이동할 대화문이 존재하지 않습니다.", "&4[ ERROR &4] ");
                player.closeInventory();
                return;
            }
            readingDialog.put(player, dialogData.get(target));
            readingPage.put(player, 0);
            interpretDialog(player);
        }
        else if(stl.startsWith("!STORYEX")) {
            String ex = str.replaceFirst("!STORYEX:", "");
            if(ex.trim().startsWith("|")) {
                ex = ex.replaceFirst("\\|", "");
                List<String> list = new ArrayList<>();
                for(String s : ex.split("\\|")) {
                    list.add(s.trim());
                }
                new Scene(player, list, null, false);
            }
            else StoryManager.executeLine(player, ex);
            readingPage.put(player, readingPage.get(player) + 1);
            interpretDialog(player);
        }
        else if(stl.startsWith("!STORYCODE")) {
            int code = Integer.parseInt(str.split(":")[1]);
            PlayerData pdc = new PlayerData(player);
            pdc.setStoryCode(code);
            readingPage.put(player, readingPage.get(player) + 1);
            interpretDialog(player);
        }
        else if(stl.startsWith("!STORY")) {
            String storyName = str.split(":")[1].trim();
            StoryManager.readStory(player, storyName);
            readingPage.put(player, readingPage.get(player)+1);
            interpretDialog(player);
        }
        else if(stl.startsWith("!JAVAACTION")) {
            String action = str.split(":")[1].trim();
            StoryManager.javaAction(player, action);
            readingPage.put(player, readingPage.get(player)+1);
            interpretDialog(player);
        }
        //대화 읽기
        else {
            if(skipDialog.contains(player)) {
                skipDialog.remove(player);
                ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&e[ &6대화문 &e]");
                str = str.replaceAll("<player>", player.getName());
                String[] lines = str.split("//");
                for(String s : lines) {
                    it.addLore("&f"+s);
                }
                it.setCustomModelData(44);
                inv.setslot(13, it.getItem());
                player.openInventory(inv.getInv());
                return;
            }
            noClick.add(player);
            new readString(player, str).runTaskTimer(plugin, 0,1);
        }
    }

    //대화문을 한 글자씩 읽음
    private static class readString extends BukkitRunnable {
        private final Player player;
        private final char[] completedText;
        private int index = 0;
        private String text;
        private InvClass inv;

        public readString(Player player, String str) {
            str = str.replaceAll("<player>", player.getName());
            completedText = str.toCharArray();
            this.player = player;
            text = "";
        }

        @Override
        public void run() {
            if(skipDialog.contains(player)) {
                skipDialog.remove(player);
                player.closeInventory();
                cancel();
                return;
            }
            if(index==completedText.length) {
                cancel();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        noClick.remove(player);
                    }
                }.runTaskLater(plugin, 5);
                return;
            }
            text += completedText[index];
            if(index!=completedText.length-1) {
                if(completedText[index]=='/'&&completedText[index+1]=='/') {
                    index++;
                    text += completedText[index];
                }
            }
            if(completedText[index]=='&') {
                index++;
                text += completedText[index];
            }
            index++;
            inv = getBaseGUI();
            ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&e[ &6대화문 &e]");
            it.setCustomModelData(44);
            String[] lines = text.split("//");
            for(String s : lines) {
                it.addLore("&f"+s);
            }
            inv.setslot(13, it.getItem());
            if(index%2==0) player.playSound(player.getLocation(), "a.type", 1, 1);
            player.openInventory(inv.getInv());
        }
    }

    private static InvClass getBaseGUI() {
        return new InvClass(3, dialogGuiName);
    }
    private static InvClass getAnswerGUI() {
        return new InvClass(3, answerGuiName);
    }

    public static ArrayList<String> getRegisteredDialog(Entity entity) {
        if(!registeredData.containsKey(entity.getUniqueId().toString())) return new ArrayList<>();
        return registeredData.get(entity.getUniqueId().toString());
    }

    private static void setRegisteredDialog(Entity entity, ArrayList<String> dialogs) {
        String uuid = entity.getUniqueId().toString();
        Configure config = getRegisteredDataConfig();
        config.getConfig().set(uuid, dialogs);
        config.saveConfig();
        registeredData.put(uuid, dialogs);
    }

    //##################################
    //QUESTS
    //##################################
    public static boolean createNewQuest(String path, String name, int sort, @Nullable Entity target, QuestType questType) {
        if (questData.containsKey(name)) return false;
        if(!path.endsWith(".yml")) path += ".yml";
        if(!questFileNames.contains(path)) questFileNames.add(path);
        Configure config = getQuestPathConfig();
        config.getConfig().set(name, path);
        config.saveConfig();
        config = getQuestConfig(name);
        config.getConfig().set(name + ".sort", sort);
        if (target == null) config.getConfig().set(name + ".completeNpc", "완료 NPC를 작성해주세요.");
        else config.getConfig().set(name + ".completeNpc", Msg.recolor(target.getName()));
        if(sort!=2&&sort!=3) config.getConfig().set(name + ".chainQuest", "null");
        if(questType.equals(QuestType.GUARDIAN_QUEST)) config.getConfig().set(name + ".isGuardianQuest", true);
        if(questType.equals(QuestType.REPEATABLE)) config.getConfig().set(name + ".canGiveUp", true);
        else config.getConfig().set(name + ".canGiveUp", false);
        if(sort==2) config.getConfig().set(name + ".saveItem", false);
        if(sort==1) config.getConfig().set(name + ".leastContribution", 30.0);
        if(questType.equals(QuestType.REPEATABLE)) config.getConfig().set(name + ".repeatable", true);
        else config.getConfig().set(name + ".repeatable", false);
        config.getConfig().set(name + ".repeatTimer", 600);
        config.getConfig().set(name + ".lore", new ArrayList<>());
        config.getConfig().set(name + ".logs", new ArrayList<>());

        ItemClass icon = new ItemClass(new ItemStack(Material.SCUTE));
        if(questType.equals(QuestType.MAIN)) icon.setCustomModelData(100);
        else if(questType.equals(QuestType.SUB_MAIN)) icon.setCustomModelData(101);
        else if(questType.equals(QuestType.REPEATABLE)) icon.setCustomModelData(102);
        else if(questType.equals(QuestType.HIDDEN)) icon.setCustomModelData(104);
        else if(questType.equals(QuestType.GUARDIAN_QUEST)) icon.setCustomModelData(103);

        config.getConfig().set(name + ".icon", icon.getItem());
        config.getConfig().set(name + ".endMessage", "null");
        ArrayList<String> conditions = new ArrayList<>();
        if(sort==2) {
            conditions.add("useItemData");
            config.getConfig().set(name + ".conditions", conditions);
        } else {
            config.getConfig().set(name + ".conditions", conditions);
        }
        config.getConfig().set(name + ".rewards", new ArrayList<>());
        if (sort != 3) {
            ArrayList<String> list = new ArrayList<>();
            if (sort == 1) list.add("MobName, 0");
            if (sort == 4) list.add("customObjectName");
            config.getConfig().set(name + ".details", list);
        }
        config.saveConfig();
        questData.put(name, new Quest(name, config));
        return true;
    }

    public static boolean deleteQuest(String name) {
        if(!questData.containsKey(name)) return false;
        Configure config = getQuestConfig(name);
        config.getConfig().set(name, null);
        config.saveConfig();
        if(config.getFile().length()==0L) {
            config.delete("quests");
        }
        config = getQuestPathConfig();
        config.getConfig().set(name, null);
        config.saveConfig();
        questData.remove(name);

        for(Player player : plugin.getServer().getOnlinePlayers()) {
            reloadQIP(player);
        }

        return true;
    }

    public static void setQuestIcon(String name, ItemStack item) {
        Configure config = getQuestConfig(name);
        config.getConfig().set(name+".icon", item);
        config.saveConfig();
    }

    public static boolean setQuestRepeatTimer(String name, int second) {
        Configure config = getQuestConfig(name);
        if(!config.getConfig().getBoolean(name+".repeatable")) return false;
        config.getConfig().set(name+".repeatTimer", second);
        config.saveConfig();
        questData.put(name, new Quest(name, config));
        return true;
    }

    public static void setQuestNavigatable(String name, boolean bool) {
        Configure config = getQuestConfig(name);
        if(!bool) config.getConfig().set(name+".navigatable", null);
        else config.getConfig().set(name+".navigatable", bool);
        config.saveConfig();
    }
    public static void setQuestNavigatingTarget(String name, @Nullable Location location) {
        Configure config = getQuestConfig(name);
        config.getConfig().set(name+".navigatingTarget", location);
        config.saveConfig();
    }

    /**
     * 퀘스트 리스트에서 퀘스트의 세부 정보를 확인하는 창을 띄운다.
     *
     * @param player player
     * @param questName 퀘스트의 이름
     * @param isReward true면 보상 창 , false면 아이템 보는 창
     * @param onlyView reward가 true일때는 onlyView가 true일시 보상을 받고 false면 보상을 수정, reward가 false일때는 onlyView가 true면 가져올 아이템을 보는 창 false면 아이템 수정
     */
    public static void openQuestDataList(Player player, String questName, boolean isReward, boolean onlyView) {
        InvClass inv;
        Quest quest = questData.get(questName);
        settingQuest.put(player, questName);
        if(isReward) {
            ArrayList<ItemStack> rewards = new ArrayList<>();
            if(onlyView) {
                inv = new InvClass(3, "&f\uF000\uF01A\uE203");

                int level = new PlayerData(player).getLevel();

                for(ItemStack item :quest.getConvertedRewards()) {
                    ItemData itemData = new ItemData(item.clone());
                    if(itemData.getLevel() < 0) {
                        itemData.setLevel(Math.max(1, itemData.getLevel() + level));
                    }
                    rewards.add(itemData.getItemStack());
                }
            } else {
                inv = new InvClass(3, "&f\uF000\uF01A\uE200");
                rewards = quest.getRewards();
            }
            int slot = 9;
            for(ItemStack item : rewards) {
                inv.setslot(slot, item);
                slot++;
            }
        }
        else {
            ArrayList<ItemStack> items;
            if(onlyView) {
                items = quest.getConvertedItems();
                inv = new InvClass(3, "&f\uF000\uF01A\uE202");
            } else {
                inv = new InvClass(3, "&f\uF000\uF01A\uE201");
                items = quest.getItems();
            }
            int slot = 9;
            for(ItemStack item : items) {
                inv.setslot(slot, item);
                slot++;
            }
        }
        player.openInventory(inv.getInv());
    }

    public static void openQuestList(Player player) {
        InvClass inv = new InvClass(3, "&f\uF000\uF01A");

        int slot = 9;
        for(QuestInProgress quest : getQuestsInProgress(player)) {
            inv.setslot(slot, quest.getIcon());
            slot++;
        }

        ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&e퀘스트 로그 확인하기");
        item.addLore("&f\uE011\uE00C\uE00C자신이 지금까지 어떤 퀘스트를 해왔는지 확인합니다.");
        item.addLore("&c\uE011\uE00C\uE00C어떤 퀘스트를 해야할 지 모르겠으면 확인해보세요!");
        item.addLore(" ");
        item.addLore("&7퀘스트 로그는 최근에 클리어한 54개의 퀘스트만 저장됩니다.");
        item.setCustomModelData(42);
        inv.setslot(22, item.getItem());
        player.openInventory(inv.getInv());
    }

    public static boolean completeCustomObject(Player player, String goal) {
        ArrayList<QuestInProgress> qips = getQuestsInProgress(player);
        boolean clear = false;
        for(QuestInProgress qip : qips) {
            if(questData.get(qip.getName()).getSort()!=4) continue;
            if(!questData.get(qip.getName()).getCustomObjects().contains(goal)) continue;

            Set<String> completedObj = qip.getCompletedCustomObject();
            if(!completedObj.contains(goal)) {
                clear = true;
                completedObj.add(goal);
            }
            qip.setCompletedCustomObject(completedObj);
        }
        setQuestInProgress(player, qips);
        List<QuestInProgress> comp = new ArrayList<>();
        for (QuestInProgress qip : getQuestsInProgress(player)) {
            Quest quest = getQuestData().get(qip.getName());
            if (quest.getSort() == 2) continue;
            if(!quest.getCompleteNpc().replaceAll(" ", "").equals("즉시완료")) continue;
            comp.add(qip);
        }
        for(QuestInProgress qip : comp) {
            qip.completeQuest(player, null);
        }
        return clear;
    }

    /**
     * 해당 플레이어가 해당 목표를 수행하고 있는지 반환
     * @param player player
     * @param object 커스텀 목표의 정확한 이름
     * @return 해당 플레이어가 해당 목표를 수행하고 있는가
     */
    public static boolean hasCustomObject(Player player, String object) {
        ArrayList<QuestInProgress> qips = getQuestsInProgress(player);
        for(QuestInProgress qip : qips) {
            Quest quest = questData.get(qip.getName());
            if(quest.getSort()!=4) continue;
            if(quest.getCustomObjects().contains(object)) return true;
        }
        return false;
    }

    public static ArrayList<String> getUncompletedCustomObject(Player player) {
        ArrayList<String> cob = new ArrayList<>();
        ArrayList<QuestInProgress> qips = getQuestsInProgress(player);
        for(QuestInProgress qip : qips) {
            Quest quest = questData.get(qip.getName());
            if(quest.getSort()!=4) continue;
            cob.addAll(quest.getCustomObjects());
            cob.removeAll(qip.getCompletedCustomObject());
        }
        return cob;
    }

    //Manipulating Player Data
    public static Set<String> getReadDialogs(Player player) {
        return (Set<String>) playerDqData.get(player).get("readDialogs");
    }

    public static void setReadDialogs(Player player, Set<String> dialogs) {
        playerDqData.get(player).put("readDialogs", dialogs);
    }

    public static Set<String> getCompletedQuests(Player player) {
        return (Set<String>) playerDqData.get(player).get("completedQuests");
    }

    public static void setCompletedQuests(Player player, Set<String> quests) {
        playerDqData.get(player).put("completedQuests", quests);
    }

    public static void setQuestLog(Player player, List<String> questLog) {
        playerDqData.get(player).put("questLogs", questLog);
    }

    public static List<String> getQuestLog(Player player) {
        return (List<String>) playerDqData.get(player).get("questLogs");
    }

    public static ArrayList<QuestInProgress> getQuestsInProgress(Player player) {
        return (ArrayList<QuestInProgress>) playerDqData.get(player).get("questsInProgress");
    }

    public static void setQuestInProgress(Player player, ArrayList<QuestInProgress> quests) {
        playerDqData.get(player).put("questsInProgress", quests);
    }

    public static Map<String, Long> getQuestCooltime(Player player) {
        return (Map<String, Long>) playerDqData.get(player).get("questCooltime");
    }

    public static void setQuestCooltime(Player player, Map<String, Long> data) {
        playerDqData.get(player).put("questCooltime", data);
    }

    @Nullable
    public static Quest getNavigatingQuest(Player player) {
        if(!playerNavigating.containsKey(player)) return null;
        return playerNavigating.get(player);
    }

    public static boolean setNavigatingQuest(Player player, @Nullable Quest quest) {
        if(quest==null) {
            playerNavigating.remove(player);
            reloadNavigate(player);
            return true;
        }
        if(player.getLocation().distanceSquared(quest.getNavigatingTarget())>1500*1500) {
            Msg.warn(player, "추적 대상이 다른 섬에 있거나 너무 멀리 떨어져 있습니다.");
            return false;
        }

        ItemStack left = player.getInventory().getItemInOffHand();
        if(!left.getType().equals(Material.AIR)) {
            if(!InvClass.hasEnoughSpace(player.getInventory(), left)) {
                Msg.warn(player, "인벤토리에 나침반을 장착할 공간이 부족합니다.");
                return false;
            }
            player.getInventory().addItem(left);
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        }


        playerNavigating.put(player, quest);
        reloadNavigate(player);
        return true;
    }

    private static String prefix(String name) {
        return "&7[ &e"+name+" &7] ";
    }

    //EVENTS

    //재접속시 위치 추적 초기화
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        playerNavigating.remove(player);
        if(player.getInventory().getItemInOffHand().getType().equals(Material.COMPASS)) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        }
    }

    //위치 추적 도중 나침반 클릭 비활성화
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(!e.getView().getTitle().equals("Crafting")) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(e.getCurrentItem()==null) return;
        if(!e.getCurrentItem().getType().equals(Material.COMPASS)) return;
        e.setCurrentItem(new ItemStack(Material.AIR));
        setNavigatingQuest(player, null);
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, Rand.randFloat(0.8, 1.3));
        player.closeInventory();
        Msg.send(player, "&c위치 추적을 취소합니다.", pfix);
        e.setCancelled(true);
    }

    //아이템 퀘스트 완료
    @EventHandler
    public void onClickCompletingGUI(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals(questItemGuiName)) return;
        e.setCancelled(true);
        if(e.getCurrentItem()==null) return;
        if(e.getCurrentItem().getType()==Material.NETHERITE_SHOVEL) return;

        if(main.unusableSlots.contains(e.getRawSlot()+9))  {
            if(e.getRawSlot()+9!=81||!EquipmentManager.getEquipment(player, "무기").getType().equals(Material.AIR)) {
                return;
            }
        }

        //아이템 넣기
        if(e.getRawSlot()>44) {
            if(e.getCurrentItem()==null) return;
            if(e.getCurrentItem().getType().equals(Material.ENCHANTED_BOOK)) {
                Msg.warn(player, "이 아이템은 넣으실 수 없습니다.");
                return;
            }
            for(int i : completingSlots) {
                if(e.getInventory().getItem(i)!=null) continue;
                for(int j : completingSlots) {
                    ItemStack exist = e.getInventory().getItem(j);
                    if(exist==null) continue;
                    if(!exist.getItemMeta().equals(e.getCurrentItem().getItemMeta())) continue;
                    exist.setAmount(exist.getAmount()+e.getCurrentItem().getAmount());
                    e.setCurrentItem(new ItemStack(Material.AIR));
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.2f);
                    return;
                }
                e.getInventory().setItem(i, e.getCurrentItem());
                e.setCurrentItem(new ItemStack(Material.AIR));
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.2f);
                return;
            }
            Msg.warn(player, "아이템을 넣을 공간이 부족한가요? &e대장간에서 아이템 레벨 다운을 통해 재료들을 합칠 수 있습니다!");
            return;
        }

        //퀘스트 완료
        if(e.getRawSlot()==35) {
            if(!completingQuest.get(player).completeQuest(player, interactingEntity.get(player), e.getInventory(), false)) {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                Msg.send(player, "&c퀘스트 완료에 필요한 아이템을 정확히 넣어주세요. 퀘스트에 따라 특정 속성이 있는 아이템이나 특정 레벨 이상의 아이템등을 요구할 수 있습니다.", pfix);
            }
            return;
        }
        //대화문 읽기
        if(e.getRawSlot()==17) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            if(readRegistered(player, interactingEntity.get(player))) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            }
            else {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                Msg.send(player, "&c읽을 수 있는 대화문이 존재하지 않습니다.", pfix);
            }
            return;
        }

        //아이템 뺴기
        boolean pass = false;
        for(int i : completingSlots) {
            if(e.getRawSlot()==i) {
                pass = true;
                break;
            }
        }
        if(!pass) return;
        InvClass.giveOrDrop(player, e.getCurrentItem());
        e.setCurrentItem(new ItemStack(Material.AIR));
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 0.8f);
    }

    //아이템 퀘스트 완료창 닫기
    @EventHandler
    public void onCloseCompletingGUI(InventoryCloseEvent e) {
        if(!(e.getPlayer() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals(questItemGuiName)) return;
        boolean mail = false;
        List<ItemStack> items = new ArrayList<>();
        for(int i : completingSlots) {
            if(e.getInventory().getItem(i)==null) continue;
            items.add(e.getInventory().getItem(i));
        }
        MailBoxManager.giveAllOrMailAll(player, items);
    }

    //처치 기여도를 이용하여 계산
    public static void onKillMob(Player player, String pureName, double contribution) {
        ArrayList<QuestInProgress> qips = getQuestsInProgress(player);
        for(QuestInProgress qip : qips) {
            Quest quest = getQuestData().get(qip.getName());
            if(quest.getSort()!=1) continue;

            if(!quest.getMobData().containsKey(pureName)) continue;
            double least = quest.getLeastContribution();

            if(contribution<least) {
                Msg.send(player, String.format("&c최소 처치 기여도인 %.0f보다 기여도가 낮아 퀘스트가 갱신되지 않았습니다.", least), prefix(quest.getName()));
                continue;
            }
            qip.killMob(player, pureName);
        }
        setQuestInProgress(player, qips);

        for (QuestInProgress qip : getQuestsInProgress(player)) {
            Quest quest = getQuestData().get(qip.getName());
            if (quest.getSort() == 2) continue;
            if(!quest.getCompleteNpc().equals("즉시 완료")) continue;
        }
    }

    //퀘스트 목록/보상 클릭 (위치 추적, 아이템 확인, 포기)
    @EventHandler
    public void onClickQuestList(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF01A\uE203")) {
            e.setCancelled(true);
            if(e.getRawSlot()>8&&e.getRawSlot()<18) {
                if(e.getCurrentItem()==null) return;
                if(!MailBoxManager.giveOrMail(player, e.getCurrentItem())) {
                    Msg.send(player, "&c인벤토리 공간이 부족하여 수령하지 못한 아이템이 &4메일함으로 전송&c되었습니다.", pfix);
                    Msg.send(player, "메뉴에서 메일함을 확인해보세요.", pfix);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                }
                else player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, (float) Rand.randDouble(0.8, 1.5));
                e.setCurrentItem(new ItemStack(Material.AIR));
            }
            return;
        }

        if(!Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF01A")) return;
        e.setCancelled(true);
        if(e.getRawSlot()==-999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            new MenuWindow(player);
            return;
        }
        if(e.getCurrentItem()==null) return;

        if(e.getRawSlot()==22) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            new QuestLogWindow(player);
            return;
        }

        if(e.getRawSlot()<9||e.getRawSlot()>=18) return;
        String name = Msg.recolor(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName());
        if(name.endsWith("[ 반복 ]")) {
            name = name.replaceAll(" &7\\[ 반복 ]", "");
        }
        Quest quest = questData.get(name);

        if(e.getClick().equals(ClickType.SHIFT_LEFT)) {
            if(!quest.isNavigatable()) return;

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.closeInventory();
                }
            }.runTaskLater(plugin, 1);



            if(!player.getInventory().getItemInOffHand().getType().equals(Material.AIR)) {
                Msg.warn(player, "&c퀘스트를 추적하려면 왼손을 비워주세요.");
                return;
            }

            if(getNavigatingQuest(player)!=null&&getNavigatingQuest(player).getName().equals(quest.getName())) {
                setNavigatingQuest(player, null);
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, Rand.randFloat(0.8, 1.3));
                Msg.send(player, "&c위치 추적을 취소합니다.", String.format("&7[ %s &7] &f", quest.getName()));
                return;
            }

            if(!setNavigatingQuest(player, quest)) return;
            player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 1, Rand.randFloat(0.8, 1.3));
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, Rand.randFloat(0.8, 1.3));
            Msg.send(player, "&6퀘스트의 위치 추적을 시작합니다.", String.format("&7[ %s &7] &f", quest.getName()));

            return;
        }
        else if (quest.getCompleteNpc().equalsIgnoreCase("click") && e.getClick().equals(ClickType.LEFT)) {
            QuestInProgress qip = null;
            for (QuestInProgress lip : getQuestsInProgress(player)) {
                if(!lip.getName().equals(quest.getName())) continue;
                qip = lip;
            }
            if(qip == null) return;
            if (quest.getSort() == 2) {
                completingQuest.put(player, qip);
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                quest.openQuestCompletingMenu(player);
            }
            else {
                if(!qip.completeQuest(player, null)) {
                    Msg.warn(player, "퀘스트를 클리어하기 위한 조건을 달성하지 못했습니다.");
                }
            }
            return;
        }

        if(quest.canGiveUp()) {
            if(e.getClick()==ClickType.SHIFT_RIGHT) {
                ArrayList<QuestInProgress> qips = getQuestsInProgress(player);
                Quest navigated = DialogQuestManager.getNavigatingQuest(player);
                for(QuestInProgress qip : qips) {
                    if(qip.getName().equals(name)) {
                        if(navigated!=null&&navigated.getName().equals(name)) {
                            DialogQuestManager.setNavigatingQuest(player, null);
                        }
                        qips.remove(qip);
                        break;
                    }
                }
                setQuestInProgress(player, qips);
                Msg.send(player, "&6퀘스트를 중단하였습니다.", pfix);
                player.playSound(player, Sound.BLOCK_FIRE_EXTINGUISH, 1, 1.5f);
                openQuestList(player);
                return;
            }
        }
        if(quest.getSort()==2) {
            if(e.getClick()!=ClickType.RIGHT) return;
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
            openQuestDataList(player, name, false, true);
        }
    }

    //대화 진행 및 답변
    @EventHandler
    public void onClickDialog(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        String title = Msg.recolor(e.getView().getTitle());
        if(!(title.equals(dialogGuiName)||title.equals(answerGuiName))) return;
        e.setCancelled(true);
        if(e.getClick().equals(ClickType.DOUBLE_CLICK)) return;

        if(e.getRawSlot()==-999) {
            skipDialog.add(player);
            noClick.remove(player);
            player.closeInventory();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        if(noClick.contains(player)) return;
        skipDialog.remove(player);
        if(e.getClick().equals(ClickType.SHIFT_LEFT)&&(player.isOp()||isSkipable.contains(player))) {
            skipDialog.add(player);
        }
        Dialog dialog = readingDialog.get(player);
        int page = readingPage.getOrDefault(player, -1);
        if(page==-1) {
            if(e.getRawSlot()!=13) return;
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, (float) Rand.randDouble(1, 1.5));
            page++;
            readingPage.put(player, page);
            interpretDialog(player);
            return;
        }
        if (page==dialog.getPageSize()) {
            if(e.getRawSlot()!=13) return;
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, (float) Rand.randDouble(1, 1.5));
            player.closeInventory();
            return;
        }
        String str = dialog.getPage(page);
        //답변
        if(str.startsWith("!ANSWER")) {
            str = str.replaceAll("!ANSWER\\||!ANSWER \\|", "");
            String[] answers = str.split("\\|");
            int index = (e.getRawSlot()-(13- answers.length+1)) / 2;
            if(e.getCurrentItem()==null||!e.getCurrentItem().getType().equals(Material.SCUTE)) return;
            ArrayList<String> actions = new ArrayList<>();
            for(String ans : answers) {
                actions.add(ans.split(":")[1].trim());
            }
            //답변 만들기
            String action;
            try {
                action = actions.get(index);
            } catch (IndexOutOfBoundsException er) {
                return;
            }
            player.playSound(player, Sound.UI_BUTTON_CLICK, 0.7f, (float) Rand.randDouble(1,1.5));
            if(action.equalsIgnoreCase("!CONTINUE")) {
                page++;
                readingPage.put(player, page);
                interpretDialog(player);
            }
            else if (action.equalsIgnoreCase("!BREAK")) {
                page = dialog.getPageSize();
                readingPage.put(player, page);
                interpretDialog(player, false);
            }
            else if (action.equalsIgnoreCase("!STAY")) {
                interpretDialog(player, false);
            }
            else if (action.toUpperCase().startsWith("!GOTO")) {
                try {
                    page = Integer.parseInt(answers[index].split(":")[2].trim());
                } catch (NumberFormatException er) {
                    Msg.send(player, "&cGOTO 구문에서 오류가 발생했습니다.", "&4[ ERROR ] ");
                }
                readingPage.put(player, page);
                interpretDialog(player, false);
            }
            else if (action.toUpperCase().startsWith("!GO")) {
                try {
                    page = Integer.parseInt(answers[index].split(":")[2].trim());
                } catch (NumberFormatException er) {
                    Msg.send(player, "&cGOTO 구문에서 오류가 발생했습니다.", "&4[ ERROR ] ");
                }
                readingPage.put(player, readingPage.get(player) + page);
                interpretDialog(player, false);
            }
            else {
                if(!dialogData.containsKey(action)) {
                    Msg.send(player, "&c이동할 대화문이 존재하지 않습니다.", "&4[ ERROR &4] ");
                    player.closeInventory();
                    return;
                }
                openDialogInstantly(player, action);
            }
        }
        else if (str.equalsIgnoreCase("!GUARDIANS_QUESTS")) {
            List<String> strings = new ArrayList<>();
            strings.add("!QUEST:"+Msg.recolor(Objects.requireNonNull(Objects.requireNonNull(e.getCurrentItem()).getItemMeta().getLore()).get(0)));
            strings.add("!CLOSEFINISH");
            readingDialog.put(player, new Dialog(strings));
            readingPage.put(player, 0);
            interpretDialog(player);
        }
        else if (str.startsWith("!QUEST")) {
            if(e.getRawSlot()!=13) return;
            String questName = str.split(":")[1].trim();
            questData.get(questName).startQuest(player);
            readingPage.put(player, readingPage.get(player)+1);
            interpretDialog(player);
        }
        //대화문 읽기
        else {
            if(e.getRawSlot()!=13) return;
            page++;
            player.playSound(player, Sound.UI_BUTTON_CLICK, 0.7f, (float) Rand.randDouble(1, 1.5));
            readingPage.put(player, page);
            interpretDialog(player);
        }
    }

    public static void openDialogInstantly(Player player, String dialogName) {
        readingDialog.put(player, dialogData.get(dialogName));
        readingPage.put(player, 0);
        interpretDialog(player);
    }

    //리스트 클릭
    @EventHandler
    public void onClickListGUI(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF01A\uE202")) {
            e.setCancelled(true);
            if(e.getRawSlot()==-999) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, (float) Rand.randDouble(1, 1.5));
                openQuestList(player);
            }
        }
    }

    //보상 및 아이템 설정 저장
    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if(!(e.getPlayer() instanceof Player player)) return;
        //보상설정
        if(Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF01A\uE200")) {
            String name = settingQuest.get(player);
            ArrayList<ItemStack> items = new ArrayList<>();
            for(int slot = 9; slot < 18; slot++) {
                ItemStack item = e.getInventory().getItem(slot);
                if(item==null) continue;
                items.add(item);
            }
            Configure config = getQuestConfig(name);
            config.getConfig().set(name+".rewards", items);
            config.saveConfig();
            questData.put(name, new Quest(name, config));
            Msg.send(player, "성공적으로 보상을 설정했습니다.", pfix);
        }
        //보상 받기
        else if (Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF01A\uE203")) {
            List<ItemStack> items = new ArrayList<>();
            for(int slot = 9; slot < 18; slot++) {
                if(e.getInventory().getItem(slot)==null) continue;
                items.add(e.getInventory().getItem(slot));
            }
            MailBoxManager.giveAllOrMailAll(player, items);
        }
        //아이템 설정
        else if (Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF01A\uE201")) {
            String name = settingQuest.get(player);
            ArrayList<ItemStack> items = new ArrayList<>();
            for(int slot = 9; slot < 18; slot++) {
                ItemStack item = e.getInventory().getItem(slot);
                if(item==null) continue;
                items.add(item);
            }
            Configure config = getQuestConfig(name);
            config.getConfig().set(name+".details", items);
            config.saveConfig();
            questData.put(name, new Quest(name, config));
            Msg.send(player, "성공적으로 아이템을 설정했습니다.", pfix);
        }
    }

    //NPC 상호작용 (퀘스트 완료 + 대화문 읽기)
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if(isReloading) return;
        Player player = e.getPlayer();
        Entity entity = e.getRightClicked();
        if (noClick.contains(player)) return;
        if(plugin.getServer().getPlayer(entity.getName())!=null) return;
        noClick.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                noClick.remove(player);
            }
        }.runTaskLater(plugin, 5);

        if(PlayerData.isPlayerStunned(player)) {
            Msg.send(player, "&c지금은 아무 행동도 할 수 없습니다.", pfix);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
            return;
        }

        //퀘스트 완료

        for (QuestInProgress qip : getQuestsInProgress(player)) {
            Quest quest = getQuestData().get(qip.getName());
            if (quest.getSort() == 2) {
                if(!Msg.color(quest.getCompleteNpc()).equals(entity.getName()) || quest.getCompleteNpc().equalsIgnoreCase("click")) continue;
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_GOLD, 1, (float) Rand.randDouble(0.8, 1.3));
                interactingEntity.put(player, e.getRightClicked());
                completingQuest.put(player, qip);
                quest.openQuestCompletingMenu(player);
                return;
            } else if (qip.completeQuest(player, entity)) return;
        }
        readRegistered(player, entity);
    }

    //대화문 읽기
    public boolean readRegistered(Player player, Entity entity) {
        ArrayList<String> dialogs = getRegisteredDialog(entity);
        Set<String> toDelete = new HashSet<>();
        ArrayList<Dialog> availableDialogs = new ArrayList<>();
        for (String name : dialogs) {
            if (!dialogData.containsKey(name)) {
                toDelete.add(name);
                continue;
            }
            Dialog dialog = dialogData.get(name);
            if (dialog.isReadable(player)) {
                availableDialogs.add(dialog);
            }
        }
        if (!toDelete.isEmpty()) {
            for (String name : toDelete) {
                dialogs.remove(name);
            }
            setRegisteredDialog(entity, dialogs);
        }
        if (availableDialogs.isEmpty()) return false;
        availableDialogs.sort((o1, o2) -> o2.getPriority() - o1.getPriority());
        Dialog dialog = availableDialogs.get(0);
        readingDialog.put(player, dialog);
        readingPage.put(player, -1);
        interpretDialog(player);
        return true;
    }
}
