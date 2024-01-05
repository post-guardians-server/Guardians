package me.rukon0621.guardians;

import com.nisovin.magicspells.MagicSpells;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.rukon0621.guardians.account.AccountCommand;
import me.rukon0621.guardians.account.AccountManager;
import me.rukon0621.guardians.afk.AfkManager;
import me.rukon0621.guardians.areawarp.Area;
import me.rukon0621.guardians.areawarp.AreaCommand;
import me.rukon0621.guardians.areawarp.AreaEnvironment;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.barcast.BarBroadcastCommand;
import me.rukon0621.guardians.blueprint.BluePrintManager;
import me.rukon0621.guardians.blueprint.BlueprintCommand;
import me.rukon0621.guardians.commands.*;
import me.rukon0621.guardians.craft.CrafttableCommand;
import me.rukon0621.guardians.craft.RecipeCommand;
import me.rukon0621.guardians.craft.craft.CraftManager;
import me.rukon0621.guardians.craft.craft.WaitingItem;
import me.rukon0621.guardians.craft.recipes.RecipeManager;
import me.rukon0621.guardians.data.*;
import me.rukon0621.guardians.dialogquest.*;
import me.rukon0621.guardians.dropItem.Drop;
import me.rukon0621.guardians.dropItem.DropAttribute;
import me.rukon0621.guardians.dropItem.DropCommand;
import me.rukon0621.guardians.dropItem.DropManager;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.events.WorldPeriodicEvent;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.listeners.*;
import me.rukon0621.guardians.mailbox.MailBoxCommand;
import me.rukon0621.guardians.mobType.MobTypeCommand;
import me.rukon0621.guardians.mobType.MobTypeManager;
import me.rukon0621.guardians.mythicAddon.MythicListener;
import me.rukon0621.guardians.noticeboard.NoticeBoard;
import me.rukon0621.guardians.noticeboard.NoticeBoardCommand;
import me.rukon0621.guardians.noticeboard.NoticeBoardManager;
import me.rukon0621.guardians.party.*;
import me.rukon0621.guardians.region.Region;
import me.rukon0621.guardians.region.RegionCommands;
import me.rukon0621.guardians.region.RegionManager;
import me.rukon0621.guardians.shop.Shop;
import me.rukon0621.guardians.shop.ShopCommand;
import me.rukon0621.guardians.shop.ShopManager;
import me.rukon0621.guardians.skillsystem.RuneSkillManager;
import me.rukon0621.guardians.skillsystem.SkillCommand;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.guardians.skillsystem.skilltree.SkillTreeCommand;
import me.rukon0621.guardians.skillsystem.skilltree.SkillTreeManager;
import me.rukon0621.guardians.spawnerUtils.SpawnUtilCommand;
import me.rukon0621.guardians.storage.StorageManager;
import me.rukon0621.guardians.story.StoryCommands;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.guardians.story.variable.VariableManager;
import me.ulrich.clans.Clans;
import me.ulrich.clans.api.ClanAPIManager;
import me.ulrich.clans.interfaces.ClanAPI;
import me.ulrich.clans.interfaces.UClans;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class main extends JavaPlugin {
    public static final String pfix = "&7[ &c! &7] ";
    public static final String mainChannel = "postgz:channel";
    public static final String chatChannel = "postgz:chat";
    private static main plugin;
    public static final String levelSplit = "Lv.";
    public static final String typeSplit = "』 ";
    public static Set<Integer> unusableSlots;
    public static Set<Material> notSolidBlock;
    private AccountManager accountManager;
    private MobTypeManager mobTypeManager;
    private BluePrintManager bluePrintManager;
    private SkillTreeManager skillTreeManager;
    private VariableManager variableManager;
    private AfkManager afkManager;
    public static String DB_NAME;
    public static String PLUGIN_FOLDER_NAME;

    private Clans clanPlugin;


    public static main getPlugin() {
        return plugin;
    }

    public static boolean isDevServer() {
        return Bukkit.getPort() >= 56220;
    }

    @Override
    public void onLoad() {
        unusableSlots = new HashSet<>();
        unusableSlots.add(81);
        unusableSlots.add(82);
        unusableSlots.add(83);
        unusableSlots.add(84);
        plugin = this;
        ConfigurationSerialization.registerClass(Region.class);
        ConfigurationSerialization.registerClass(WaitingItem.class);
        ConfigurationSerialization.registerClass(Shop.class);
        ConfigurationSerialization.registerClass(Drop.class);
        ConfigurationSerialization.registerClass(DropAttribute.class);
        ConfigurationSerialization.registerClass(NoticeBoard.class);
        ConfigurationSerialization.registerClass(Area.class);
        ConfigurationSerialization.registerClass(QuestInProgress.class);
        ConfigurationSerialization.registerClass(Couple.class);
        ConfigurationSerialization.registerClass(AreaEnvironment.class);

        //QualityRandomWindow.initialize();

        //File
        reloadConfig();
        FileUtil.getOuterPluginFolder().mkdir();
    }

    @Override
    public void onEnable() {
        Stat.resetKeyMap();
        EnhanceLevel.initialize();

        if (Bukkit.getPluginManager().isPluginEnabled("UltimateClans")) {
            clanPlugin = (Clans) Bukkit.getServer().getPluginManager().getPlugin("UltimateClans");
        }
        else clanPlugin = null;
        //BUNGEE
        getServer().getMessenger().registerOutgoingPluginChannel(this, mainChannel);

        notSolidBlock = new HashSet<>();
        notSolidBlock.add(Material.GRASS);
        notSolidBlock.add(Material.TALL_GRASS);

        DBStatic.getConnection("guardians");

        //Managers
        LevelData.resetLevelData();
        new ItemSaver();
        ItemData.reloadItemData();
        variableManager = new VariableManager();
        afkManager = new AfkManager();
        new DialogQuestManager();
        new RegionManager();
        new StorageManager();
        new SkillManager();
        new RuneSkillManager();
        RecipeManager.reloadRecipes();
        new CraftManager();
        new ShopManager();
        new SoloMode();
        new BarManager();
        new NoticeBoardManager();
        new AreaManger();
        new MythicListener();
        bluePrintManager = new BluePrintManager();
        StoryManager.reloadStory(new CountDownLatch(1));
        DropManager.reloadAllDropData();
        accountManager = new AccountManager();
        new PartyManager();
        mobTypeManager = new MobTypeManager();
        skillTreeManager = new SkillTreeManager();
        skillTreeManager.reload();

        //Commands
        new test();
        new DialogCommands();
        new DQDataCommands();
        new QuestCommands();
        new MailBoxCommand();
        new BuffClearCommand();
        new HealCommand();
        new StoryCommands();
        new RegionCommands();
        new ItemDataCommands();
        new ExpCommand(this);
        new LevelCommand(this);
        new MoneyCommand(this);
        new SkillCommand();
        new DailyEvent();
        new RecipeCommand();
        new CrafttableCommand();
        new ShopCommand();
        new DropCommand();
        new ReportCommand();
        new LocationSaver();
        new PlayerDataCommand();
        new ReloadCommand();
        new BroadcastCommand();
        new ForceCastCommand();
        new NoticeBoardCommand();
        new AreaCommand();
        new PartyInviteCommand();
        new PartyAcceptCommand();
        new RebootCommand();
        new SpawnUtilCommand();
        new AccountCommand();
        new MoneyGetCommand();
        new RespawnCommand();
        new BarBroadcastCommand();
        new MobTypeCommand();
        new BlueprintCommand();
        new TitleCommand();
        new ChannelCommand();
        new WhisperCommand();
        new SkillTreeCommand();
        new PartyJoinCommand();
        new PartyRecruitCommand();
        new GetLocCommand();
        new EntireBroadcastCommand();
        new ChatCommand();
        new CloseAndKickCommand();
        new MuteCommand();
        new TitleControlCommand();

        //Events
        new SystemEventsListener();
        new VoteListener();
        new ProxyListener();
        new EquipmentManager();
        new LogInOutListener();
        new DamagingListener();
        new WorldPeriodicEvent();
        new ItemUseListener();
        new ChatEventListener();
        new MythicListener();
        new TeseionListener();
        new ResourcePackListener();
        new TitleListener();
        new PaymentListener();
        new OpenAudioListener();
        PlayerData.reloadStatDatabase();
        new BukkitRunnable() {
            @Override
            public void run() {
                MagicSpells plg = MagicSpells.getInstance();
                plg.unload();
                plg.load();
                getLogger().info("매직스펠 활성화");
                LogInOutListener.fullyEnableServer();
            }
        }.runTaskLater(this, 20);

    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, mainChannel);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, chatChannel);
        getServer().getMessenger().unregisterIncomingPluginChannel(this, mainChannel);
        getServer().getMessenger().unregisterIncomingPluginChannel(this, chatChannel);
        DBStatic.closeAll();
    }

    public void reloadConfig() {
        Configure config = new Configure("mainConfig.yml", plugin.getDataFolder().getPath());
        /*
        if(config.getConfig().get("database_name")==null) {
            config.getConfig().set("plugin_folder_name", "guardians");
            config.getConfig().set("database_name", "guardians");
            config.saveConfig();
        }
        PLUGIN_FOLDER_NAME = config.getConfig().getString("plugin_folder_name");
        DB_NAME = config.getConfig().getString("database_name");
        DBStatic.setUrl(config.getConfig().getString("dbUrl", "localhost:3306/"));
         */

        if(isDevServer()) PLUGIN_FOLDER_NAME = "devGuardians";
        else PLUGIN_FOLDER_NAME = "guardians";
        DB_NAME = "guardians";
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public MobTypeManager getMobTypeManager() {
        return mobTypeManager;
    }

    public BluePrintManager getBluePrintManager() {
        return bluePrintManager;
    }

    public SkillTreeManager getSkillTreeManager() {
        return skillTreeManager;
    }

    public VariableManager getVariableManager() {
        return variableManager;
    }

    public AfkManager getAfkManager() {
        return afkManager;
    }

    public Clans getClanPlugin() {
        return clanPlugin;
    }
}
