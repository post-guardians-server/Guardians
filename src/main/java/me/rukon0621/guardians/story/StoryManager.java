package me.rukon0621.guardians.story;

import me.rukon0621.callback.LogManager;
import me.rukon0621.callback.ProxyCallBack;
import me.rukon0621.callback.RukonCallback;
import me.rukon0621.dungeonwave.RukonWave;
import me.rukon0621.dungeonwave.fieldwave.FieldWave;
import me.rukon0621.guardians.GUI.ChannelWindow;
import me.rukon0621.guardians.GUI.item.*;
import me.rukon0621.guardians.GUI.item.enhance.EnhanceGUI;
import me.rukon0621.guardians.account.AccountManager;
import me.rukon0621.guardians.afk.AfkConfirmWindow;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.craft.craft.CraftManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.dialogquest.QuestInProgress;
import me.rukon0621.guardians.dropItem.DropManager;
import me.rukon0621.guardians.equipmentLevelup.RuneLevelUpWindow;
import me.rukon0621.guardians.events.WorldPeriodicEvent;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.listeners.OpenAudioListener;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.party.PartyManager;
import me.rukon0621.guardians.region.RegionManager;
import me.rukon0621.guardians.shop.ShopManager;
import me.rukon0621.guardians.storage.StorageManager;
import me.rukon0621.guardians.story.variable.VariableManager;
import me.rukon0621.gui.RukonGUI;
import me.rukon0621.pay.runartrade.ExchangeWindow;
import me.rukon0621.pay.shop.MONEY;
import me.rukon0621.pay.shop.ShopWindow;
import me.rukon0621.ridings.RideManager;
import me.rukon0621.ridings.RidingMenu;
import me.rukon0621.ridings.RukonRiding;
import me.rukon0621.rinstance.RukonInstance;
import me.rukon0621.rpvp.RukonPVP;
import me.rukon0621.guardians.story.Scene;
import me.rukon0621.teseion.Main;
import me.rukon0621.teseion.TeseionInstance;
import me.rukon0621.teseion.event.ReceiveSignalEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.playavalon.avnparty.AvNParty;
import net.playavalon.avnparty.party.Party;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.internal.LogManagerStatus;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static io.lumine.mythic.bukkit.utils.text.Text.DefaultFontInfo.e;
import static me.rukon0621.guardians.main.pfix;

public class StoryManager {
    private static final main plugin = main.getPlugin();
    private static HashMap<String, List<String>> storyData;
    private static final HashMap<Player, String> playingStory = new HashMap<>(); //플레이어가 실행중인 스토리
    private static final ArrayList<String> pathList = new ArrayList<>();
    private static final HashMap<Player, Location> previousLocation = new HashMap<>();
    private static final Map<Player, Scene> playerScene = new HashMap<>();
    private static final VariableManager variableManager = main.getPlugin().getVariableManager();

    private static Configure getPathConfig() {
        return new Configure("path.yml", FileUtil.getOuterPluginFolder()+"/story");
    }

    private static Configure getStoryConfig(String name) {
        return new Configure(FileUtil.getOuterPluginFolder() + "/story/stories/" + getPathConfig().getConfig().getString(name));
    }

    public static HashMap<String, List<String>> getStoryData() {
        return storyData;
    }

    public static ArrayList<String> getPathList() {
        return pathList;
    }

    public static void reloadStory(CountDownLatch latch) {
        new BukkitRunnable() {
            @Override
            public void run() {
                storyData = new HashMap<>();
                Configure paths = getPathConfig();
                pathList.clear();
                for(String name : paths.getConfig().getKeys(false)) {
                    pathList.add(paths.getConfig().getString(name));
                    storyData.put(name, (ArrayList<String>) getStoryConfig(name).getConfig().getList(name));
                }
                latch.countDown();
            }
        }.runTaskAsynchronously(plugin);
    }

    @Nullable
    public static Location getPreviousLocation(Player player) {
        return previousLocation.getOrDefault(player, null);
    }

    public static Location setPreviousLocation(Player player, Location loc) {
        return previousLocation.put(player, loc);
    }

    @Nullable
    public static String getPlayingStory(Player player) {
        if(!playingStory.containsKey(player)) return null;
        return playingStory.getOrDefault(player, null);
    }

    public static void readStory(Player player, String name) {
        if(!storyData.containsKey(name)) return;
        previousLocation.put(player, player.getLocation());
        playingStory.put(player, name);
        playerScene.put(player, new Scene(player, storyData.get(name), name));
        playerScene.get(player).startScene();
    }

    public static void readStoryInstantly(Player player, String name) {
        if(!storyData.containsKey(name)) {
            Msg.warn(player, name + " - 이 스토리를 찾을 수 없습니다.", "&4[ &cERROR &4] &6");
            return;
        }
        readStoryInstantly(player, storyData.get(name));
    }

    public static void readStoryInstantly(Player player, List<String> scripts) {
        new Scene(player, scripts, null);
    }

    public static void executeLine(Player player, String script) {
        try {
            String sct = script.toUpperCase(Locale.ROOT);
            script = script.replaceAll("<player>", player.getName());
            if (sct.startsWith("!CMD:")) {
                Opcmd.opCmd(player, script.split(":")[1]);
            }

            //!AREA:<areaName>:[<force(def=1)>]
            else if (sct.startsWith("!START_AFK")) {
                //main.getPlugin().getAfkManager().startAfk(player, script.split(":")[1].trim());
                new AfkConfirmWindow(player);
            }
            else if (sct.startsWith("!OPEN_GUI")) {
                RukonGUI.inst().getCustomUiManager().getGUI(script.split(":")[1]).open(player);
            }
            else if (sct.startsWith("!CLOSE_GUI")) {
                player.closeInventory();
            }
            else if (sct.startsWith("!AREA:")) {
                int force = 1;
                String[] data = script.split(":");
                String area = data[1];
                if(data.length == 3) {
                    force = Integer.parseInt(data[2]);
                }
                AreaManger.teleportToArea(player, area, force == 1);
            }
            else if (sct.startsWith("!PSP")) {
                String[] data = script.split(":");
                if(player.getSpectatorTarget() instanceof ArmorStand stand) {
                    stand.playSound(net.kyori.adventure.sound.Sound.sound(Sound.valueOf(data[1].trim().toUpperCase(Locale.ROOT)).getKey(), net.kyori.adventure.sound.Sound.Source.MASTER, Float.parseFloat(data[2].trim()), Float.parseFloat(data[3].trim())));
                }
                else player.playSound(player, Sound.valueOf(data[1].trim().toUpperCase(Locale.ROOT)), Float.parseFloat(data[2].trim()), Float.parseFloat(data[3].trim()));
            }
            else if (sct.startsWith("!PS")) {
                String[] data = script.split(":");
                player.playSound(player.getLocation(), data[1].trim().toLowerCase(Locale.ROOT), Float.parseFloat(data[2].trim()), Float.parseFloat(data[3].trim()));
            }
            else if (sct.startsWith("!TP")) {
                String[] data = script.split(":");
                if(data.length>2) {
                    double x, y, z;
                    if(data[1].startsWith("~")) {
                        if(data[1].endsWith("~")) data[1] += "~";
                        x = Double.parseDouble(data[1].replaceFirst("~", "")) + player.getLocation().getX();
                    } else x = Double.parseDouble(data[1]);
                    if(data[2].startsWith("~")) {
                        if(data[2].endsWith("~")) data[2] += "~";
                        y = Double.parseDouble(data[2].replaceFirst("~", "")) + player.getLocation().getY();
                    } else y = Double.parseDouble(data[2]);
                    if(data[3].startsWith("~")) {
                        if(data[3].endsWith("~")) data[3] += "~";
                        z = Double.parseDouble(data[3].replaceFirst("~", "")) + player.getLocation().getZ();
                    } else z = Double.parseDouble(data[3]);
                    float yaw, pitch;
                    if(data.length>4) {
                        yaw = Float.parseFloat(data[4]);
                        pitch = Float.parseFloat(data[5]);
                    } else {
                        yaw = player.getLocation().getYaw();
                        pitch = player.getLocation().getPitch();
                    }
                    Location loc = new Location(player.getWorld(), x, y, z, yaw, pitch);
                    loc.getChunk().load();
                    player.teleport(loc);
                }
                else LocationSaver.tpToLoc(player, data[1].trim());
            }
            else if (sct.startsWith("!GIVE")) {
                String[] data = script.split(":");
                int level;
                try {
                    level = Integer.parseInt(data[2]);
                } catch (NullPointerException | IndexOutOfBoundsException e) {
                    level = 0;
                }

                ItemData itemData = new ItemData(ItemSaver.getItem(data[1].trim()));
                itemData.setLevel(level);
                if(!MailBoxManager.giveOrMail(player, itemData.getItemStack())) {
                    Msg.warn(player, "인벤토리에 공간이 부족하여 아이템이 메일함으로 전송되었습니다.");
                }
            }
            else if (sct.startsWith("!SYSTEM")) {
                String msg = script.split(":")[1].trim();
                Msg.send(player, " ");
                msg = StringEscapeUtils.unescapeJava(msg);
                Msg.send(player, msg, "&6[ &c! &6] &e");
                player.playSound(player, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 2, (float) Rand.randDouble(0.8, 1.3));
            }
            else if (sct.startsWith("!REAL_STORY:")) {
                String storyName = script.split(":")[1].trim();
                stopStory(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        readStory(player, storyName);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if (sct.startsWith("!STORY:")) {
                String storyName = script.split(":")[1].trim();
                readStoryInstantly(player, storyName);
            }
            else if (sct.startsWith("!STORYCODE:")) {
                PlayerData pdc = new PlayerData(player);
                pdc.setStoryCode(Integer.parseInt(script.split(":")[1]));
            }
            else if (sct.startsWith("!COMPLETEOBJECT:")) {
                String name = script.split(":")[1].trim();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        DialogQuestManager.completeCustomObject(player, name);
                        Msg.send(player,  "&6" + name + " &e목표를 달성했습니다!", pfix);
                        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, (float) Rand.randDouble(0.8,1.5));
                    }
                }.runTask(plugin);
            }
            else if (sct.startsWith("!PARTICLE")) {
                String[] data = script.split(":");
                double x, y, z, dx, dy, dz, speed;
                int count;
                Particle particle = Particle.valueOf(data[1].toUpperCase(Locale.ROOT));
                if(data[2].startsWith("~")) {
                    if(data[2].endsWith("~")) data[2] += "0";
                    x = Double.parseDouble(data[2].replaceFirst("~", "")) + player.getLocation().getX();
                } else x = Double.parseDouble(data[2]);
                if(data[3].startsWith("~")) {
                    if(data[3].endsWith("~")) data[3] += "0";
                    y = Double.parseDouble(data[3].replaceFirst("~", "")) + player.getLocation().getY();
                } else y = Double.parseDouble(data[3]);
                if(data[4].startsWith("~")) {
                    if(data[4].endsWith("~")) data[4] += "0";
                    z = Double.parseDouble(data[4].replaceFirst("~", "")) + player.getLocation().getZ();
                } else z = Double.parseDouble(data[4]);
                dx = Double.parseDouble(data[5]);
                dy = Double.parseDouble(data[6]);
                dz = Double.parseDouble(data[7]);
                count = Integer.parseInt(data[8]);
                speed = Double.parseDouble(data[9]);
                if(particle.equals(Particle.BLOCK_CRACK)) {
                    player.spawnParticle(particle, x, y, z, count, dx, dy, dz, speed, Material.getMaterial(data[10].trim()).createBlockData());
                    return;
                }
                else if (particle.equals(Particle.ITEM_CRACK)) {
                    player.spawnParticle(particle, x, y, z, count, dx, dy, dz, speed, new ItemStack(Material.getMaterial(data[10].trim())));
                    return;
                }
                player.spawnParticle(particle, x, y, z, count, dx, dy, dz, speed);
            }
            else if (sct.startsWith("!GM")) {
                String[] data = script.split(":");
                int gm = Integer.parseInt(data[1]);
                if(gm==0) player.setGameMode(GameMode.SURVIVAL);
                else if(gm==1) player.setGameMode(GameMode.CREATIVE);
                else if(gm==2) player.setGameMode(GameMode.ADVENTURE);
                else if(gm==3) {
                    player.setGameMode(GameMode.SPECTATOR);
                    ActionBar.sendActionBar(player, new TextComponent("\uF000"));
                }
            }
            else if (sct.startsWith("!PREVIOUS")) {
                Location loc = previousLocation.get(player);
                loc.getChunk().load();
                player.teleport(loc);
            }
            else if (sct.startsWith("!TESEION")) {
                AvNParty partyPlugin = AvNParty.plugin;
                if(partyPlugin.getParty(player)!=null) {
                    PartyManager.quitParty(partyPlugin.getAvalonPlayer(player), false);
                }
                String finalScript = script;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Party party = new Party(player);
                        partyPlugin.getAvalonPlayer(player).setParty(party);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Objects.requireNonNull(Main.getPlugin().getTeseionManager().getTeseion(finalScript.split(":")[1].trim())).play(player);
                            }
                        }.runTaskLater(plugin, 5);
                    }
                }.runTaskLater(plugin, 5);
            }
            else if (sct.startsWith("!CAMERA")) {
                String[] data = script.split(":");
                double ox, oy, oz, x, y, z;
                float opt, oyw, pt, yw;

                int tick = Integer.parseInt(data[1]);
                int minus = 0;
                if(data.length<12) {
                    Location loc = player.getLocation();
                    ox = loc.getX();
                    oy = loc.getY();
                    oz = loc.getZ();
                    oyw = loc.getYaw();
                    opt = loc.getPitch();
                    minus = 5;
                }
                else {
                    ox = Double.parseDouble(data[2]);
                    oy = Double.parseDouble(data[3]);
                    oz = Double.parseDouble(data[4]);
                    oyw = Float.parseFloat(data[5]);
                    opt = Float.parseFloat(data[6]);
                }
                x = Double.parseDouble(data[7 - minus]);
                y = Double.parseDouble(data[8 - minus]);
                z = Double.parseDouble(data[9 - minus]);
                yw = Float.parseFloat(data[10 - minus]);
                pt = Float.parseFloat(data[11 - minus]);
                new CameraMoving(player, ox, oy, oz, oyw, opt, tick, x, y, z, yw, pt);
            }
            else if (sct.startsWith("!CAM")) {
                String[] data = script.split(":");
                int tick = Integer.parseInt(data[1]);
                double[] num = new double[12];

                for(int i = 2; i < data.length; i++) {
                    num[i - 1] = Double.parseDouble(data[i]);
                }

                if(data.length==3) {
                    new Cinematic(player, tick, num[1]);
                }
                else if(data.length==5) {
                    new Cinematic(player, tick, num[1], num[2], num[3], num[4], num[5]);
                }
                else if(data.length==7) {
                    new Cinematic(player, tick, num[1], num[2], num[3], num[4], num[5]);
                }
                else if (data.length==9) {
                    new Cinematic(player, tick, num[1], num[2], num[3], num[4], num[5], num[6]);
                }
                else if (data.length==10) {
                    new Cinematic(player, tick, num[1], num[2], num[3], num[4], num[5], num[6], num[7], num[8]);
                }
                else if (data.length==12) {
                    new Cinematic(player, tick, num[1], num[2], num[3], num[4], num[5], num[6], num[7], num[8], num[9], num[10]);
                }
                else {
                    throw new IllegalArgumentException("잘못된 파라미터 수입니다.");
                }
            }
            else if (sct.startsWith("!SHAKE")) {
                String[] data = script.split(":");
                double strength;
                int tick;
                tick = Integer.parseInt(data[1]);
                strength = Double.parseDouble(data[2]);
                new CameraShaking(player, tick, strength);
            }
            else if (sct.startsWith("!SHAKE_SPECTATOR")) {
                String[] data = script.split(":");
                double strength;
                int tick;
                tick = Integer.parseInt(data[1]);
                strength = Double.parseDouble(data[2]);
                new SpectatorShaking(player, tick, strength);
            }
            else if (sct.startsWith("!DROP")) {
                String[] data = script.split(":");
                List<ItemStack> items = DropManager.getDropList(player, data[1], Integer.parseInt(data[2]), 1, false, true);
                if (data[3].equals("1")) {
                    Map<String, Integer> itemMap = new HashMap<>();
                    for(ItemStack item : items) {
                        try {
                            String name = Msg.uncolor(item.getItemMeta().getDisplayName());
                            itemMap.putIfAbsent(name, 0);
                            itemMap.put(name, itemMap.get(name) + 1);
                        } catch (Exception e) {
                            itemMap.putIfAbsent("&e이름 없는 아이템", 0);
                            itemMap.put("&e이름 없는 아이템", itemMap.get("&e이름 없는 아이템") + 1);
                        }
                    }
                    if(items.size()==0) {
                        Msg.send(player, "&e아무런 전리품도 얻지 못했습니다.", pfix);
                    }
                    else {
                        Msg.send(player, "&e전리품을 획득하였습니다!", pfix);
                        for(String name : itemMap.keySet()) {
                            Msg.send(player, String.format("&7    - &e%s &f(x%d)", name, itemMap.get(name)));
                        }
                    }
                    MailBoxManager.giveAllOrMailAll(player, items);
                }
            }
            else if (sct.startsWith("!START")) {
                PlayerData.setPlayerSlowStun(player, true);
                if(!sct.endsWith("<NS>")) player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.3f);
                RideManager rideManager = RukonRiding.inst().getRideManager();
                if(rideManager.isPlayerRiding(player)) rideManager.despawnRiding(player);
            }
            else if (sct.startsWith("!FINISH")) {
                if(!sct.endsWith("<NS>")) player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1.3f);
                PlayerData.setPlayerSlowStun(player, false);
            }
            else if (sct.equals("!UNRIDE")) {
                RideManager manager = RukonRiding.inst().getRideManager();
                if(manager.isPlayerRiding(player)) manager.despawnRiding(player);
            }
            else if (sct.equals("!SOLO_ON")) {
                SoloMode.soloMode(player, true);
            }
            else if (sct.equals("!SOLO_OFF")) {
                SoloMode.soloMode(player, false);
            }
            else if (sct.equals("!STUN_ON")) {
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1.3f);
                PlayerData.setPlayerStun(player, true);
                RideManager rideManager = RukonRiding.inst().getRideManager();
                if(rideManager.isPlayerRiding(player)) rideManager.despawnRiding(player);
            }
            else if (sct.equals("!STUN_OFF")) {
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1.3f);
                PlayerData.setPlayerStun(player, false);
            }
            else if (sct.equals("!IGNORE_REGION")) {
                if(!RegionManager.getIgnoreRegionPlayers().add(player)) {
                    RegionManager.getIgnoreRegionPlayers().remove(player);
                }
            }
            //!FIELDWAVE:<fwName>:<fromOrigin>
            else if (sct.equals("!FIELDWAVE")) {
                String fwName = script.split(":")[1];
                boolean fromOrigin = Boolean.parseBoolean(script.split(":")[2]);
                FieldWave fw = RukonWave.inst().getFieldWaveManager().getFieldWave(fwName);
                if(fw==null) {
                    Msg.warn(player, fwName + " - 해당 이름의 필드웨이브를 찾을 수 없습니다.");
                    return;
                }
                if(fromOrigin) fw.play(player);
                else fw.play(player, player.getLocation());
            }
            //title:title,subtitle,stay,fadein,fadeout
            else if(sct.startsWith("!TITLE")) {
                String[] strs = script.split(":");
                int stay, fadeIn, fadeOut;
                try {
                    stay = Integer.parseInt(strs[3]);
                    fadeIn = Integer.parseInt(strs[4]);
                    fadeOut = Integer.parseInt(strs[5]);
                } catch (NumberFormatException e) {
                    Msg.send(player, script + " &c : 오류가 발생했습니다.");
                    Msg.send(player, "&7title:<title>:<subtitle>:<stay>:<fadeIn>:<fadeOut>");
                    return;
                }
                strs[1] = StringEscapeUtils.unescapeJava(strs[1]);
                if(strs[2].trim().equals("")) {
                    Msg.sendTitle(player, strs[1], stay, fadeIn, fadeOut);
                    return;
                }
                strs[2] = StringEscapeUtils.unescapeJava(strs[2]);
                Msg.sendTitle(player, strs[1], strs[2], stay, fadeIn, fadeOut);
            }
            else if(sct.startsWith("!FADE")) {
                String[] split = script.split(":");
                int stay, fi, fo;
                if(split.length>1) {
                    stay = Integer.parseInt(split[1]);
                    fi = Integer.parseInt(split[2]);
                    fo = Integer.parseInt(split[3]);
                }
                else {
                    stay = 20;
                    fi = 10;
                    fo = 10;
                }
                Msg.sendTitle(player, "\uE000", "\uE00C", stay, fi, fo);
            }
            else if(sct.startsWith("!DIALOG")) {
                DialogQuestManager.openDialogInstantly(player, script.split(":")[1].trim());
            }
            else if(sct.startsWith("!SIGNAL")) {
                if(RukonInstance.inst().getInstanceManager().getPlayerInstance(player) instanceof TeseionInstance instance) {
                    ReceiveSignalEvent event = new ReceiveSignalEvent(instance, script.split(":")[1].trim());
                    Bukkit.getPluginManager().callEvent(event);

                }
            }
            else if(sct.startsWith("!STOP_BGM")) {
                OpenAudioListener.stopSong(player, "bgm");
            }
            else if(sct.startsWith("!PLAY_BGM")) {
                OpenAudioListener.playBgm(player, script.split(":")[1]);
            }
            else if(sct.startsWith("!ADD_FLAG")) {
                StoryManager.getReadStory(player).add("flag_" + script.split(":")[1]);
            }
            else if(sct.startsWith("!REMOVE_FLAG")) {
                StoryManager.getReadStory(player).remove("flag_" + script.split(":")[1]);
            }
            else if(sct.startsWith("!TAKEITEM")) {
                String[] data = script.split(":");
                int amount;
                try {
                    amount = Integer.parseInt(data[2]);
                } catch (Exception e) {
                    amount = 1;
                }
                ItemSaver.removeItem(player, data[1], amount);
            }
            else if(sct.startsWith("!HIDDEN_LOG")) {
                /*
                !HIDDEN_LOG:<NAME>:<INFORM>

                INFORM? - 히든 요소가 몇 번 찾아진지 알려주고 많이 찾아지면 값어치가 떨어진다는 것을 알려줌

                0(default): 5번 단위로 히든요소가 몇번째로 찾아진건지 알려줌
                1: 알려주지 않음
                2: 정확히 몇번째로 찾았는지 알려줌


                 */
                /*
                Configure config = new Configure(FileUtil.getOuterPluginFolder() + "/logs/hiddenLog.yml");
                List<String> list = config.getConfig().getStringList("logs");
                list.add(String.format("%s %s - %s (%s)", player.getName(), str, DateUtil.toSimpleTime(new Date()), player.getUniqueId()));
                int num = config.getConfig().getInt("numbers." + str, 0) + 1;
                config.getConfig().set("numbers." + str, num);
                config.getConfig().set("logs", list);
                config.saveConfig();
                 */
                String[] data = script.split(":");
                String str = data[1].trim();
                new LogManager.StatGetter(player, "hidden", str) {
                    @Override
                    protected void getValue(int i) {
                        int num = i + 1;
                        LogManager.stat(player, "hidden", str, 1);
                        LogManager.log(player, "hidden", str);
                        String strNum = String.valueOf(num);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                int num = Integer.parseInt(strNum);
                                Msg.send(player, " ");
                                player.playSound(player, Sound.ENTITY_ENDER_DRAGON_HURT, 1, 0.8f);
                                String informType = "0";
                                if(data.length > 2) {
                                    informType = data[2].trim();
                                }
                                if(informType.equals("0")) {
                                    num = ((num - 1) / 5) * 5;
                                    if(num == 0) Msg.send(player, "&e이 히든 요소는 &c약 5회 미만&e으로 발견된 잘 알려지지 않은 히든 요소입니다. &b히든 요소는 많이 발견될 수록 보상이 줄어듭니다!", pfix);
                                    else if (num <= 10) Msg.send(player, String.format("&e이 히든 요소는 &c약 %d회 이상 &e발견된 조금 덜 알려진 히든 요소입니다. &b히든 요소는 많이 발견될 수록 보상이 줄어듭니다!", num), pfix);
                                    else if (num <= 20) Msg.send(player, String.format("&e이 히든 요소는 &c약 %d회 이상 &e발견된 꽤나 알려진 히든 요소입니다. &b히든 요소는 많이 발견될 수록 보상이 줄어듭니다!", num), pfix);
                                    else Msg.send(player, String.format("&e이 히든 요소는 &c약 %d회 이상 &e발견된 이미 많이 알려진 히든 요소입니다. &b히든 요소는 많이 발견될 수록 보상이 줄어듭니다!", num), pfix);
                                }
                                else if(informType.equals("2")) {
                                    if(num == 1) Msg.send(player, "&e이 히든 요소는 &c지금 처음 발견된 히든 요소&e입니다. &b히든 요소는 많이 발견될 수록 보상이 줄어듭니다!", pfix);
                                    else if (num <= 10) Msg.send(player, String.format("&e이 히든 요소는 &c%d회 &e발견된 조금 덜 알려진 히든 요소입니다. &b히든 요소는 많이 발견될 수록 보상이 줄어듭니다!", num - 1), pfix);
                                    else if (num <= 20) Msg.send(player, String.format("&e이 히든 요소는 &c%d회 &e발견된 꽤나 알려진 히든 요소입니다. &b히든 요소는 많이 발견될 수록 보상이 줄어듭니다!", num - 1), pfix);
                                    else Msg.send(player, String.format("&e이 히든 요소는 &c%d회 &e발견된 이미 많이 알려진 히든 요소입니다. &b히든 요소는 많이 발견될 수록 보상이 줄어듭니다!", num - 1), pfix);
                                }
                            }
                        }.runTaskLater(plugin, 25);
                    }
                };


            }
            else if(sct.startsWith("!UNREAD")) {
                String s = script.split(":")[1].trim();
                DialogQuestManager.getReadDialogs(player).remove(s);
            }
            else if(sct.startsWith("!UNCOMPLETE")) {
                String s = script.split(":")[1].trim();
                DialogQuestManager.getCompletedQuests(player).remove(s);
            }
            else if (sct.startsWith("!READ")) {
                String s = script.split(":")[1].trim();
                DialogQuestManager.getReadDialogs(player).add(s);
            }
            else if (sct.startsWith("!COMPLETE")) {
                String s = script.split(":")[1].trim();
                DialogQuestManager.getCompletedQuests(player).add(s);
            }
            //Player Variable Effects
            //!SETVAR varName <value> - TEMPORARY
            //!MODVAR prm:varName <value>
            else if(sct.split(" ")[0].endsWith("VAR")) {
                PlayerData pdc = new PlayerData(player);
                String[] splitData = script.split(" ");
                String varName = splitData[1];
                int value;
                try {
                    value = Integer.parseInt(splitData[2]);
                } catch (NumberFormatException e) {
                    if(splitData[2].toUpperCase(Locale.ROOT).startsWith("PRM:")) {
                        value = pdc.getVar(splitData[2].trim());
                    }
                    else value = variableManager.getTempVar(player, splitData[2].trim());
                }

                boolean permanentVar = varName.toUpperCase(Locale.ROOT).startsWith("PRM:");

                if(sct.startsWith("!RANDVAR")) {
                    int r1, r2;
                    r1 = Integer.parseInt(splitData[2].split(":")[0]);
                    r2 = Integer.parseInt(splitData[2].split(":")[1]);
                    if(permanentVar) {
                        pdc.setVar(varName, Rand.randInt(r1, r2));
                    }
                    else variableManager.setTempVar(player, varName, Rand.randInt(r1, r2));
                }
                else if(sct.startsWith("!SETVAR")) {
                    if(permanentVar) {
                        pdc.setVar(varName, value);
                    }
                    else {
                        variableManager.setTempVar(player, varName, value);
                    }
                }
                else if(sct.startsWith("!MODVAR")) {
                    if(permanentVar) {
                        pdc.setVar(varName, value + pdc.getVar(varName));
                    }
                    else variableManager.setTempVar(player, varName, variableManager.getTempVar(player, varName) + value);
                }
                else if(sct.startsWith("!MULVAR")) {
                    if(permanentVar) {
                        pdc.setVar(varName, value * pdc.getVar(varName));
                    }
                    else {
                        variableManager.setTempVar(player, varName, value *  variableManager.getTempVar(player, varName));
                    }
                }
                else if(sct.startsWith("!DIVVAR")) {
                    if(permanentVar) {
                        pdc.setVar(varName, pdc.getVar(varName) / value);
                    }
                    else {
                        variableManager.setTempVar(player, varName, variableManager.getTempVar(player, varName) / value);
                    }
                }
                else if(sct.startsWith("!RSTVAR")) {
                    if(permanentVar) {
                        pdc.setVar(varName, pdc.getVar(varName) % value);
                    }
                    else {
                        variableManager.setTempVar(player, varName, variableManager.getTempVar(player, varName) % value);
                    }
                }


            }
            else if (sct.startsWith("!JAVAACTION")) {
                String action = script.split(":")[1].trim();
                javaAction(player, action);
            }
            else {
                if(!script.contains("<ns>")) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1, 1);
                Msg.send(player, " ");
                Msg.send(player, variableManager.parseString(player, script.replaceAll("<ns>", "")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Msg.warn(player, script + " : 이 스크립트에서 오류가 발생했습니다. &7 - ");
        }

    }

    public static boolean checkIf(Player player, String script) {
        script = script.replaceFirst("@IF ", "");
        script = script.replaceFirst("@ELIF ", "");
        String[] args = script.split(" ");
        //CONDITION !,==,>,< VALUE

        String upper = script.toUpperCase(Locale.ROOT);
        int compareType;
        boolean not = args[1].startsWith("!"); //NOT
        if(args[1].endsWith("=")) compareType = 1; //1 -> 같음
        else if(args[1].endsWith(">")) compareType = 2; //1 -> 큼
        else if(args[1].endsWith("<")) compareType = 3; //1 -> 작음
        //비교 연산자가 없음
        else {
            compareType = -1;
        }

        String condition = args[0].toUpperCase(Locale.ROOT);
        if(compareType == -1 && condition.startsWith("!")) {
            condition = condition.replaceFirst("!", "");
            not = true;
        }
        if(condition.startsWith("VAR:")) {
            String varName = args[0].replaceFirst("VAR:", "").replaceFirst("var:", "");
            int value = Integer.parseInt(args[2]);
            PlayerData pdc = null;
            boolean permanent = varName.toUpperCase(Locale.ROOT).startsWith("PRM:");
            if(permanent) {
                pdc = new PlayerData(player);
            }
            if(compareType==1) {
                // 1 == 1 -> True != false -> true
                if(permanent) return (pdc.getVar(varName) == value) != not;
                else return (variableManager.getTempVar(player, varName) == value) != not;
            }
            else if(compareType==2) {
                if(permanent) return (pdc.getVar(varName) > value) != not;
                else return (variableManager.getTempVar(player, varName) > value) != not;
            }
            else if(compareType==3) {
                if(permanent) return (pdc.getVar(varName) < value) != not;
                else return (variableManager.getTempVar(player, varName) < value) != not;
            }
        }
        else if(condition.startsWith("HASITEM")) {
            String value = ArgHelper.sumArg(args, 1);
            int amount;
            try {
                amount = Integer.parseInt(value.split(":")[1]);
            } catch (Exception e) {
                amount = 1;
            }
            return ItemSaver.hasItem(player, value.split(":")[0], amount) != not;

        }
        else if(condition.startsWith("HASFLAG")) {
            String value = "flag_" + ArgHelper.sumArg(args, 1);
            return StoryManager.getReadStory(player).contains(value) != not;
        }
        else if(condition.startsWith("COMPLETESTORY")) {
            String value = ArgHelper.sumArg(args, 1);
            return StoryManager.getReadStory(player).contains(value) != not;
        }
        else if(condition.startsWith("COMPLETEDIALOG")) {
            String value = ArgHelper.sumArg(args, 1);
            return DialogQuestManager.getReadDialogs(player).contains(value) != not;
        }
        else if(condition.startsWith("COMPLETEQUEST")) {
            String value = ArgHelper.sumArg(args, 1);
            return DialogQuestManager.getCompletedQuests(player).contains(value) != not;
        }
        else if(condition.startsWith("HASQUEST")) {
            String value = ArgHelper.sumArg(args, 1);
            for(QuestInProgress qip : DialogQuestManager.getQuestsInProgress(player)) {
                if(qip.getName().equals(value)) return !not;
            }
            return not;
        }
        else {
            Msg.warn(player, script + " - 알 수 없는 조건입니다.");
        }
        return true;
    }

    /**
     * 플레이어의 스토리를 정상적으로 완료함
     * @param player player
     * @param end 이 플레이어가 스토리를 완전히 읽었는가 (스토리 데이터에 완료로 추가하는가)
     */
    public static void endStory(Player player, boolean end) {
        Scene scene = playerScene.get(player);
        if(scene == null) return;
        scene.disable();
        //Checking Story is Instant Scripts
        if(scene.getName()==null) return;
        RegionManager.getIgnoreRegionPlayers().remove(player);
        playerScene.remove(player);
        playingStory.remove(player);
        if(end) StoryManager.addStory(player, scene.getName());
    }

    public static void stopStory(Player player) {
        stopStory(player, false);
    }
    public static void stopStory(Player player, boolean executedByCommand) {
        if(playingStory.get(player)==null) {
            if(executedByCommand) Msg.warn(player, "현재 진행중인 스토리가 존재하지 않습니다.");
            return;
        }
        endStory(player, false);
        Msg.send(player, "스토리 진행을 중단했습니다.", pfix);
    }

    public static Set<String> getReadStory(Player player) {
        return (Set<String>) DialogQuestManager.getPlayerDqData(player).get("readStory");
    }

    public static void setReadStory(Player player, Set<String> stories) {
        DialogQuestManager.getPlayerDqData(player).put("readStory", stories);
    }

    public static boolean isRead(Player player, String name) {
        return getReadStory(player).contains(name);
    }

    public static boolean addStory(Player player, String name) {
        return getReadStory(player).add(name);
    }

    public static boolean removeStory(Player player, String name) {
        return getReadStory(player).remove(name);
    }

    public static void createNewStory(Player player, String name, String path) {
        if(storyData.containsKey(name)) {
            Msg.send(player, "&c해당 스토리는 이미 존재하는 스토리입니다.", pfix);
            return;
        }
        Configure config = getPathConfig();
        if(!path.endsWith(".yml")) path += ".yml";
        if(!pathList.contains(path)) pathList.add(path);
        config.getConfig().set(name, path);
        config.saveConfig();
        config = getStoryConfig(name);
        ArrayList<String> scripts = new ArrayList<>();
        scripts.add("!START");
        scripts.add("<player> 스토리를 진행한다.<ns>");
        scripts.add("!SYSTEM:스토리진행");
        scripts.add("!CMD:say <player>");
        scripts.add("!WAIT:20");
        scripts.add("!TITLE:title:subtitle:stay:fadeIn:fadeOut");
        scripts.add("!PS:ui.button.click:1:1.5");
        scripts.add("!PARTICLE:particleName:x:y:z:dx:dy:dz:count:speed");
        scripts.add("!TP:locationSaverName");
        scripts.add("!TP:x:y:z");
        scripts.add("!TP:x:y:z:yaw:pitch");
        scripts.add("!CAMERA:tick:ox:oy:oz:oyw:opt:x:y:z:yw:pt");
        scripts.add("!SHAKE:tick:strength");
        scripts.add("!STUN_ON");
        scripts.add("!STUN_OFF");
        scripts.add("!GIVE:itemSaverName:Level");
        scripts.add("!GM:3");
        scripts.add("!FINISH");
        scripts.add("!STORY:innerStoryName");
        scripts.add("!REPEAT:repeatNumber:repeatInterval");
        scripts.add("!REPEAT_END");
        scripts.add("!PREVIOUS");
        config.getConfig().set(name, scripts);
        config.saveConfig();
        reloadStory(new CountDownLatch(1));
        Msg.send(player, "새로운 스토리를 생성했습니다.", pfix);
    }

    public static void deleteStory(Player player, String name) {
        if(!storyData.containsKey(name)) {
            Msg.send(player, "&c해당 스토리는 존재하지 않습니다.", pfix);
            return;
        }
        Configure config = getStoryConfig(name);
        config.getConfig().set(name, null);
        config.saveConfig();
        if(config.getFile().length()==0L) {
            config.delete("stories");
        }
        config = getPathConfig();
        config.getConfig().set(name, null);
        config.saveConfig();
        storyData.remove(name);
        Msg.send(player, "성공적으로 삭제했습니다.", pfix);
    }

    public static void sayStoryList(Player player) {
        Msg.send(player, "이 서버에 존재하는 스토리의 목록입니다.", pfix);
        for(String name : storyData.keySet()) {
            Msg.send(player, name);
        }
    }

    public static void javaAction(Player player, String actionKey) {
        try {
            if(actionKey.equalsIgnoreCase("runeLevelUp")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new RuneLevelUpWindow(player);
                    }
                }.runTaskLater(plugin, 1);
            } else if(actionKey.toLowerCase().contains("crafttable")) {
                String name = actionKey.split("=")[1].trim();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        CraftManager.openCraftTable(player, name);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toLowerCase().contains("area")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        AreaManger.openAreaGUI(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toLowerCase(Locale.ROOT).startsWith("shop")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String name = actionKey.split("=")[1].trim();
                        ShopManager.openShop(player, name);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toLowerCase(Locale.ROOT).startsWith("loadaccount")) {
                String name = actionKey.split("=")[1];
                main.getPlugin().getAccountManager().loadAccount(player, name);
            }
            else if(actionKey.equalsIgnoreCase("pvp")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        RukonPVP.inst().getPvpManager().openPvPGUI(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.equalsIgnoreCase("itemAttrClear")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new ItemAttrClearWindow(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toLowerCase().contains("heal")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setHealth(player.getMaxHealth());
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toLowerCase().contains("herospirit")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        int num;
                        try {
                            num = Integer.parseInt(actionKey.split("=")[1]);
                        } catch (Exception e) {
                            num = 1;
                        }
                        player.closeInventory();
                        player.playSound(player, Sound.ITEM_TOTEM_USE, 1, 0.8f);
                        player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);
                        player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.3);
                        PlayerData pdc = new PlayerData(player);
                        pdc.setSpiritOfHero(pdc.getSpiritOfHero() + num);
                        Msg.send(player, "석상과 상호작용하여 영웅의 기억을 " + num + "개 획득했습니다!", pfix);
                        if(StoryManager.getReadStory(player).contains("영웅의 석상")) return;
                        StoryManager.readStory(player, "영웅의 석상");
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toLowerCase().contains("equipmentlevelup")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new ItemLevelUpWindow(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.equalsIgnoreCase("levelDown")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new ItemLevelDownWindow(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toLowerCase().contains("storage")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        StorageManager.openChestSelectGUI(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toLowerCase().contains("teseion")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String name = actionKey.split("=")[1].trim();
                        Main.getPlugin().getTeseionManager().openTeseionGUI(player, name);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.equalsIgnoreCase("cashTrade")) {

                Msg.warn(player, "지금은 삭제된 이용하실 수 없는 기능입니다.");

                /*
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new ExchangeWindow(player);
                    }
                }.runTaskLater(plugin, 1);

                 */
            }
            else if(actionKey.equalsIgnoreCase("riding")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new RidingMenu(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toUpperCase().startsWith("CHANNEL")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new ChannelWindow(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.equalsIgnoreCase("batchRank")) {
                RukonPVP.inst().getPvpManager().batch(player);
            }

            //!javaAction:CASTSHOP=name=<moneyType>
            else if(actionKey.toUpperCase().startsWith("CASHSHOP")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String[] data = actionKey.split("=");
                        String moneyType = "RUNAR";
                        if(data.length >= 3) {
                            moneyType = data[2].toUpperCase(Locale.ROOT);
                        }
                        new ShopWindow(player, (data[1].trim()), MONEY.valueOf(moneyType));
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toUpperCase().startsWith("ENHANCE")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new EnhanceGUI(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toUpperCase().startsWith("SUCCESSION")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new SuccessionGUI(player, 15, 3);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.toUpperCase().startsWith("DISASSEMBLE")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.closeInventory();
                        new ItemDisassembleWindow(player);
                    }
                }.runTaskLater(plugin, 1);
            }
            else if(actionKey.equalsIgnoreCase("SKILLTREE")) {
                main.getPlugin().getSkillTreeManager().openSkillTree(player);
            }
            else if(actionKey.equals("무기선택_검")) {
                InvClass.giveOrDrop(player, ItemSaver.getItem("기본검").getItem());
                Msg.send(player, "&a기본 무기를 지급 받았습니다!", pfix);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.3f);
            }
            else if(actionKey.equals("무기선택_창")) {
                InvClass.giveOrDrop(player, ItemSaver.getItem("기본창").getItem());
                Msg.send(player, "&a기본 무기를 지급 받았습니다!", pfix);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.3f);
            }
            else if(actionKey.equals("무기선택_둔기")) {
                InvClass.giveOrDrop(player, ItemSaver.getItem("기본해머").getItem());
                Msg.send(player, "&a기본 무기를 지급 받았습니다!", pfix);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.3f);
            }
            else if(actionKey.equals("무기선택_활")) {
                InvClass.giveOrDrop(player, ItemSaver.getItem("기본활").getItem());
                Msg.send(player, "&a기본 무기를 지급 받았습니다!", pfix);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.3f);
            }
            else if(actionKey.equals("무기선택_투척")) {
                InvClass.giveOrDrop(player, ItemSaver.getItem("기본투척용단검").getItem());
                Msg.send(player, "&a기본 무기를 지급 받았습니다!", pfix);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.3f);
            }
            else if(actionKey.equals("무기선택_폭탄")) {
                InvClass.giveOrDrop(player, ItemSaver.getItem("기본폭탄").getItem());
                Msg.send(player, "&a기본 무기를 지급 받았습니다!", pfix);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.3f);
            }
            else {
                Msg.send(player, "&4해당 JavaAction은 존재하지 않습니다.", "&c[ &7"+actionKey+"&c ] ");
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
            }
        } catch (Exception e) {
            Msg.warn(player, "&cJavaAction을 처리하는 중 오류가 발생했습니다. &7| &8ActionKey - " + actionKey);
        }
    }

}
