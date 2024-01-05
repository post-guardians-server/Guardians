package me.rukon0621.guardians.account;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.dialogquest.QuestInProgress;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.offlineMessage.OfflineMessageManager;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.guardians.storage.Storage;
import me.rukon0621.guardians.storage.StorageManager;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.pay.PaymentData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

import static me.rukon0621.guardians.main.pfix;

public class   AccountManager {
    private final main plugin = main.getPlugin();
    private Configure getConfig(String accountName) {
        return new Configure(accountName+".yml", FileUtil.getOuterPluginFolder()+"/accounts");
    }
    private ArrayList<String> accountNames;

    public AccountManager() {
        reloadAccounts();
    }

    public ArrayList<String> getAccountNames() {
        return accountNames;
    }

    public void reloadAccounts() {
        accountNames = new ArrayList<>();
        try {
            File parentFile = new File(FileUtil.getOuterPluginFolder()+"/accounts");
            parentFile.mkdir();
            for(File file : parentFile.listFiles()) {
                if(!file.getName().endsWith(".yml")) continue;
                accountNames.add(file.getName().replace(".yml",""));
            }
        } catch (NullPointerException e) {
            System.out.println("파일이 존재하지 않아 계정 리로드에 실패했습니다.");
        }
    }

    public void loadAccount(Player player, String accountName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!accountNames.contains(accountName)) {
                    Msg.warn(player, "&c해당 이름의 계정은 존재하지 않습니다.");
                    return;
                }
                player.getInventory().clear();
                Configure config = getConfig(accountName);
                PlayerData pdc = new PlayerData(player);
                PaymentData pyc = new PaymentData(player);

                pyc.setRunar(config.getConfig().getInt("runar", 0));
                pyc.setBlessOfJack(config.getConfig().getLong("bless1", 0));
                pyc.setBlessOfRukon(config.getConfig().getLong("bless2", 0));
                pyc.setBlessOfBert(config.getConfig().getLong("bless3", 0));
                pyc.setPassLevel(config.getConfig().getInt("passLevel", 1));
                pyc.setPassExp(config.getConfig().getInt("passExp", 0));
                pyc.setPassReward(config.getConfig().getInt("passReward", 0));
                pyc.setPremiumPassReward(config.getConfig().getInt("premiumPassReward", -1));

                pdc.setLevel(config.getConfig().getInt("level"));
                try {
                    pdc.setExp(config.getConfig().getLong("exp"));
                } catch (Exception e) {
                    pdc.setExp((long) config.getConfig().getDouble("exp"));
                }
                pdc.setMoney(config.getConfig().getLong("money"));
                pdc.setStoryCode(config.getConfig().getInt("storyCode"));
                pdc.setSkillPoint(config.getConfig().getInt("skillPoint"));
                pdc.setUnlearnChance(config.getConfig().getInt("unlearnChance"));
                pdc.setEnergyCore(config.getConfig().getInt("energyCore", 100));
                pdc.setMaxEnergyCore(config.getConfig().getInt("maxEnergyCore", 100));
                pdc.resetAllSkills();
                ArrayList<String> learnedSkills = (ArrayList<String>) config.getConfig().getList("learnedSkills");
                for(String skill : learnedSkills) {
                    pdc.addSkill(skill);
                }
                EquipmentManager.resetAllEquipment(player);
                EquipmentManager.setItem(player, "무기", config.getConfig().getItemStack("weapon"));
                EquipmentManager.setItem(player, "투구", config.getConfig().getItemStack("helmet"));
                EquipmentManager.setItem(player, "갑옷", config.getConfig().getItemStack("chest"));
                EquipmentManager.setItem(player, "바지", config.getConfig().getItemStack("leggings"));
                EquipmentManager.setItem(player, "부츠", config.getConfig().getItemStack("boots"));
                EquipmentManager.setItem(player, "목걸이", config.getConfig().getItemStack("necklace"));
                EquipmentManager.setItem(player, "벨트", config.getConfig().getItemStack("belt"));
                EquipmentManager.setItem(player, "반지", config.getConfig().getItemStack("ring"));
                ItemStack riding = config.getConfig().getItemStack("riding");
                if(riding==null) riding = new ItemStack(Material.AIR);
                EquipmentManager.setItem(player, "라이딩", riding);
                ArrayList<ItemStack> runes = new ArrayList<>();
                runes.add(config.getConfig().getItemStack("rune1"));
                runes.add(config.getConfig().getItemStack("rune2"));
                runes.add(config.getConfig().getItemStack("rune3"));
                EquipmentManager.setRunes(player, runes);
                EquipmentManager.reloadEquipment(player, true);

                pdc.setBlueprintsData((Set<String>) NullManager.defaultNull(Serializer.deserializeBukkitObjectFromString(config.getConfig().getString("blueprints")), new HashSet<>()));

                //QUEST DIALOG STORY
                try {
                    ArrayList<String> dialogs = (ArrayList<String>) config.getConfig().getList("dialogs");
                    ArrayList<String> quests = (ArrayList<String>) config.getConfig().getList("quests");
                    ArrayList<String> stories = (ArrayList<String>) config.getConfig().getList("stories");
                    DialogQuestManager.setQuestLog(player, (List<String>) config.getConfig().getList("logs", new ArrayList<>()));
                    DialogQuestManager.setReadDialogs(player, new HashSet<>(dialogs));
                    DialogQuestManager.setCompletedQuests(player, new HashSet<>(quests));
                    StoryManager.setReadStory(player, new HashSet<>(stories));
                    DialogQuestManager.setQuestInProgress(player, (ArrayList<QuestInProgress>) Serializer.deserializeBukkitObjectFromString(config.getConfig().getString("questInProgress")));
                } catch (IllegalArgumentException e) {
                    Msg.warn(player, "&c버전이 다른 계정을 불러와 역직렬화 과정 중 IllegalArgumentException이 발생했습니다. 해당 역직렬화는 자동으로 NULL처리 되었습니다.");
                }

                //SKILL
                HashMap<String, String> skillData = new HashMap<>();
                try {
                    skillData = (HashMap<String, String>) Serializer.deseriallizeFromString(config.getConfig().getString("skillData"));
                    SkillManager.saveEquipSkill(player, skillData);
                } catch (Exception e) {
                    SkillManager.saveEquipSkill(player, skillData);
                    Msg.send(player, "&c스킬 정보를 불러오지 못했습니다.", pfix);
                }
                SkillManager.reloadPlayerSkill(player);

                //INVENTORY
                ConfigurationSection section = config.getConfig().getConfigurationSection("inventory");
                try {
                    for(String s : section.getKeys(false)) {
                        int i = Integer.parseInt(s);
                        player.getInventory().setItem(i, section.getItemStack(s));
                    }
                } catch (Exception e) {
                    player.getInventory().clear();
                    Msg.send(player, "&c인벤토리 정보를 불러오지 못했습니다.", pfix);
                }

                //STORAGE
                try {
                    StorageManager.resetPlayerStorage(player);
                    ArrayList<Storage> storageData = (ArrayList<Storage>) Serializer.deserializeBukkitObjectFromString(config.getConfig().getString("storage"));
                    Map<Integer, String> map = StorageManager.getBoughtData(player);
                    for(Storage storage : storageData) {
                        map.put(storage.getIndex(), storage.getName());
                        StorageManager.saveStorage(player, storage);
                    }
                    StorageManager.setBoughtData(player, map);
                } catch (Exception er) {
                    Msg.send(player, "&c창고 정보를 불러오지 못했습니다.", pfix);
                    StorageManager.resetPlayerStorage(player);
                    er.printStackTrace();
                }
                Msg.send(player, "계정을 불러왔습니다.", pfix);

            }
        }.runTaskAsynchronously(plugin);
    }

    public void deleteAccount(Player player, String accountName) {
        if(!accountNames.contains(accountName)) {
            Msg.warn(player, "&c해당 이름의 계정은 존재하지 않습니다.");
            return;
        }
        Configure config = getConfig(accountName);
        config.delete("accounts");
        reloadAccounts();
        Msg.send(player, "성공적으로 삭제했습니다.", pfix);
    }

    public void saveAccount(Player player, String accountName)  {
        new BukkitRunnable() {
            @Override
            public void run() {
                Configure config = getConfig(accountName);
                PlayerData pdc = new PlayerData(player);
                PaymentData pyc = new PaymentData(player);
                config.getConfig().set("level", pdc.getLevel());
                config.getConfig().set("exp", pdc.getExp());
                config.getConfig().set("money", pdc.getMoney());
                config.getConfig().set("runar", pyc.getRunar());
                config.getConfig().set("bless1", pyc.getBlessOfJack());
                config.getConfig().set("bless2", pyc.getBlessOfRukon());
                config.getConfig().set("bless3", pyc.getBlessOfBert());
                config.getConfig().set("passLevel", pyc.getPassLevel());
                config.getConfig().set("passExp", pyc.getPassExp());
                config.getConfig().set("passReward", pyc.getPassReward());
                config.getConfig().set("premiumPassReward", pyc.getPremiumPassReward());
                config.getConfig().set("storyCode", pdc.getStoryCode());
                config.getConfig().set("skillPoint", pdc.getSkillPoint());
                config.getConfig().set("unlearnChance", pdc.getUnlearnChance());
                config.getConfig().set("energyCore", pdc.getEnergyCore());
                config.getConfig().set("maxEnergyCore", pdc.getMaxEnergyCore(true));
                ArrayList<String> learnedSkills = new ArrayList<>(pdc.getSkillData());
                config.getConfig().set("learnedSkills", learnedSkills);
                config.getConfig().set("weapon", EquipmentManager.getEquipment(player, "무기"));
                config.getConfig().set("helmet", EquipmentManager.getEquipment(player, "투구"));
                config.getConfig().set("chest", EquipmentManager.getEquipment(player, "갑옷"));
                config.getConfig().set("leggings", EquipmentManager.getEquipment(player, "바지"));
                config.getConfig().set("boots", EquipmentManager.getEquipment(player, "부츠"));
                config.getConfig().set("necklace", EquipmentManager.getEquipment(player, "목걸이"));
                config.getConfig().set("belt", EquipmentManager.getEquipment(player, "벨트"));
                config.getConfig().set("ring", EquipmentManager.getEquipment(player, "반지"));
                config.getConfig().set("rune1", EquipmentManager.getEquipment(player, "룬1"));
                config.getConfig().set("rune2", EquipmentManager.getEquipment(player, "룬2"));
                config.getConfig().set("rune3", EquipmentManager.getEquipment(player, "룬3"));
                config.getConfig().set("riding", EquipmentManager.getEquipment(player, "라이딩"));
                config.getConfig().set("dialogs", new ArrayList<>(DialogQuestManager.getReadDialogs(player)));
                config.getConfig().set("quests", new ArrayList<>(DialogQuestManager.getCompletedQuests(player)));
                config.getConfig().set("questInProgress", Serializer.serializeBukkitObjectToString(DialogQuestManager.getQuestsInProgress(player)));
                config.getConfig().set("stories", new ArrayList<>(StoryManager.getReadStory(player)));
                config.getConfig().set("logs", Serializer.serialize(DialogQuestManager.getQuestLog(player)));

                config.getConfig().set("skillData", Serializer.seriallizeToString(SkillManager.loadEquipSkill(player)));
                config.getConfig().set("blueprints", Serializer.seriallizeToString(pdc.getBlueprintsData()));

                Inventory inv = player.getInventory();
                for(int i = 0; i<=45;i++) {
                    config.getConfig().set("inventory."+i, inv.getItem(i));
                }

                //StorageData
                ArrayList<Storage> storageData = new ArrayList<>();
                for(int index : StorageManager.getBoughtData(player).keySet()) {
                    storageData.add(StorageManager.getPlayerStorage(player, index));
                }
                config.getConfig().set("storage", Serializer.serializeBukkitObjectToString(storageData));
                config.getConfig().set("mail", Serializer.seriallizeToString(MailBoxManager.getMailData(player)));

                config.getConfig().set("offlineMessages", Serializer.seriallizeToString(OfflineMessageManager.getMessages(player)));

                config.saveConfig();
                reloadAccounts();
                Msg.send(player, "성공적으로 계정을 저장했습니다.", pfix);
                Msg.send(player, "&c※ 장터의 정보는 저장되지 않습니다.");
            }
        }.runTaskAsynchronously(plugin);
    }
}
