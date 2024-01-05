package me.rukon0621.guardians.region;

import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.ridings.RideManager;
import me.rukon0621.ridings.RukonRiding;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static me.rukon0621.guardians.main.pfix;

public class RegionManager implements Listener {
    private static final main plugin = main.getPlugin();
    private static final HashMap<String, Region> regionMap = new HashMap<>();
    private static final List<Region> regionPriorityMap = new ArrayList<>();
    private static final Set<Player> playerInWardrobe = new HashSet<>();

    private static HashMap<Player, Location> pos1;
    private static HashMap<Player, Location> pos2;
    private static HashMap<Player, Set<String>> playerRegion;
    private static Set<Player> blockInteract;
    private static final Set<Player> ignoreRegion = new HashSet<>();

    public static Set<Player> getIgnoreRegionPlayers() {
        return ignoreRegion;
    }

    private static Configure getConfig() {
        return new Configure("regionSaver.yml", FileUtil.getOuterPluginFolder()+"/regions");
    }

    public static Configure getRegionConfig(String name) {
        return new Configure(name+".yml", FileUtil.getOuterPluginFolder()+"/regions/events");
    }

    public static HashMap<String, Region> getRegions() {
        return regionMap;
    }

    /**
     * @param player player
     * @return 해당 플레이어가 현재 속한 지역의 목록을 반환
     */
    public static ArrayList<Region> getRegionsOfPlayer(Player player) {
        ArrayList<Region> regions = new ArrayList<>();
        for(Region region : regionMap.values()) {
            if(region.isInRegion(player)) regions.add(region);
        }
        return regions;
    }

    public static Set<String> getPlayerRegion(Player player) {
        return playerRegion.get(player);
    }

    public RegionManager() {
        reloadRegionData();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    if(player.getGameMode().equals(GameMode.SPECTATOR)
                            ||ignoreRegion.contains(player)
                            ||LogInOutListener.getLoadingPlayers().contains(player.getName())
                            ||LogInOutListener.getUnloadedPlayers().contains(player)) continue;
                    Set<String> regions = playerRegion.get(player);
                    for(Region region : regionPriorityMap) {
                        String name = region.getName();
                        if(!region.isInRegion(player)) {
                            if(regions.remove(name)) {
                                if(onExitRegion(player, region)) break;
                            }
                        }
                        else {
                            if(regions.add(name)) {
                                if(onEnterRegion(player, region)) break;
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 10);
    }

    public static void reloadBgm(Player player) {
        List<Region> regions = new ArrayList<>();
        for(String str : playerRegion.get(player)) {
           regions.add(getRegions().get(str));
        }
        regions.sort((o1, o2) -> o2.getPriority() - o1.getPriority());
        for(Region region : regions) {
            if(!region.hasBgm()) continue;
            region.playBgm(player);
            return;
        }
    }

    public static boolean playerInWardrobe(Player player) {
        return playerInWardrobe.contains(player);
    }

    public static void reloadRegionData() {
        regionMap.clear();
        regionPriorityMap.clear();
        playerRegion = new HashMap<>();
        pos1 = new HashMap<>();
        pos2 = new HashMap<>();
        blockInteract = new HashSet<>();
        Configure config = getConfig();
        if(config.getFile().length()==0L) {
            Bukkit.getLogger().severe(config.getFile().getName() + " - 파일이 비어있습니다. 작댁 바보 멍청이");
        }
        for(String name : config.getConfig().getKeys(false)) {
            Region region = (Region) config.getConfig().get(name);
            regionMap.put(name, region);
            regionPriorityMap.add(region);
        }
        regionPriorityMap.sort((o1, o2) -> o2.getPriority() - o1.getPriority());
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            playerRegion.put(player, new HashSet<>());
            Set<String> regions = playerRegion.get(player);
            for(Region region : regionMap.values()) {
                String name = region.getName();
                if(region.isInRegion(player)) regions.add(name);
            }
        }
    }

    public static void createNewRegion(Player player, String name) {

        if(!pos1.containsKey(player)||!pos2.containsKey(player)) {
            Msg.warn(player, "&c뼈를 통해 지역을 잡아주세요. (좌클릭 우클릭)");
            return;
        }

        if(regionMap.containsKey(name)) {
            Msg.warn(player, "&c해당 이름의 지역은 이미 존재합니다.");
            return;
        }

        if(!pos1.get(player).getWorld().equals(pos2.get(player).getWorld())) {
            Msg.warn(player, "두개의 위치가 서로 다른 월드에 떨어져있습니다.");
            return;
        }

        Region region = new Region(name, pos1.get(player), pos2.get(player));
        regionMap.put(name, region);
        Configure config = getConfig();
        config.getConfig().set(name, region);
        config.saveConfig();
        config = getRegionConfig(name);
        config.getConfig().set("dialogConditions", new ArrayList<>());
        config.getConfig().set("questConditions", new ArrayList<>());
        config.getConfig().set("storyConditions", new ArrayList<>());
        config.getConfig().set("specialOptions", new ArrayList<>());
        config.getConfig().set("eventPriority", 10);
        config.getConfig().set("enterEvents", new ArrayList<>());
        config.getConfig().set("exitEvents", new ArrayList<>());
        config.saveConfig();
        Msg.send(player, "&f성공적으로 지역을 생성했습니다.", pfix);
        pos1.remove(player);
        pos2.remove(player);
    }

    public static void deleteRegion(Player player, String name) {
        if(!regionMap.containsKey(name)) {
            Msg.send(player, "&c해당 이름의 지역은 존재하지 않습니다.", pfix);
            return;
        }
        Configure config = getConfig();
        config.getConfig().set(name, null);
        config.saveConfig();
        regionMap.remove(name);
        config = getRegionConfig(name);
        config.delete("events");
        Msg.send(player, "&6성공적으로 지역을 삭제했습니다", pfix);
    }

    public static void redefineRegion(Player player, String name) {
        if(!pos1.containsKey(player)||!pos2.containsKey(player)) {
            Msg.warn(player, "&c뼈를 통해 지역을 잡아주세요. (좌클릭 우클릭)", pfix);
            return;
        }

        if(!regionMap.containsKey(name)) {
            Msg.warn(player, "&c해당 이름의 지역은 존재하지 않습니다.", pfix);
            return;
        }
        Region region = new Region(name, pos1.get(player), pos2.get(player));
        regionMap.put(name, region);
        Configure config = getConfig();
        config.getConfig().set(name, region);
        config.saveConfig();
        pos1.remove(player);
        pos2.remove(player);
        Msg.send(player, "지역 위치를 수정하였습니다.", pfix);
    }

    public boolean onEnterRegion(Player player, Region region) {
        return interpretEvent(player, region, true);
    }

    public boolean onExitRegion(Player player, Region region) {
        if(region.hasBgm()) {
            reloadBgm(player);
        }
        return interpretEvent(player, region, false);
    }

    private boolean interpretEvent(Player player, Region region, boolean enter) {
        ArrayList<String> scripts;
        if(enter) scripts = region.getEnterEvents();
        else scripts = region.getExitEvents();
        if(scripts==null) return false;

        //Check Conditions
        Set<String> datas = DialogQuestManager.getReadDialogs(player);
        for(String name : region.getDialogConditions()) {
            if(name.startsWith("not!")) {
                if(datas.contains(name.split("!")[1].trim())) return false;
            }
            else {
                if(!datas.contains(name)) return false;
            }
        }
        datas = DialogQuestManager.getCompletedQuests(player);
        for(String name : region.getQuestConditions()) {
            if(name.startsWith("not!")) {
                if(datas.contains(name.split("!")[1].trim())) return false;
            }
            else {
                if(!datas.contains(name)) return false;
            }
        }
        datas = StoryManager.getReadStory(player);
        for(String name : region.getStoryConditions()) {
            if(name.startsWith("not!")) {
                if(datas.contains(name.split("!")[1].trim())) return false;
            }
            else {
                if(!datas.contains(name)) return false;
            }
        }

        //Interpreting
        if(enter&&region.hasBgm()) reloadBgm(player);


        Iterator<String> itr  =scripts.iterator();


        new BukkitRunnable() {
            @Override
            public void run() {
                while(itr.hasNext()) {
                    String script = itr.next();
                    String sct = script.toUpperCase(Locale.ROOT);
                    if(!sct.startsWith("!")) {
                        sct = "!" + sct;
                    }
                    if(sct.startsWith("!COMPLETEOBJECT")) {
                        String name = script.split(":")[1].trim();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(DialogQuestManager.completeCustomObject(player, name)) {
                                    Msg.send(player,  "&6" + name + " &e목표를 달성했습니다!", pfix);
                                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, (float) Rand.randDouble(0.8,1.5));
                                }
                            }
                        }.runTask(plugin);
                    }
                    //ps:<SoundName>:<volume>:<pitch>
                    else if(sct.startsWith("!PS")) {
                        final String[] strs = script.split(":");
                        final float volume, pitch;
                        String sound;
                        try {
                            sound = strs[1];
                            volume = Float.parseFloat(strs[2]);
                            pitch = Float.parseFloat(strs[3]);
                            player.playSound(player.getLocation(), sound, volume, pitch);
                        } catch (Exception e) {
                            Msg.send(player, script + " &c : 오류가 발생했습니다.");
                        }
                    }
                    else if(sct.startsWith("!TP")) {
                        LocationSaver.tpToLoc(player, script.split(":")[1]);
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
                            continue;
                        }
                        if(strs[2].trim().equals("")) {
                            Msg.sendTitle(player, StringEscapeUtils.unescapeJava(strs[1]), stay, fadeIn, fadeOut);
                            continue;
                        }
                        Msg.sendTitle(player, StringEscapeUtils.unescapeJava(strs[1]), StringEscapeUtils.unescapeJava(strs[2]), stay, fadeIn, fadeOut);
                    }
                    //completeAndRead:CustomObjectName:StoryName
                    else if(sct.startsWith("!COMPLETEANDREAD")) {
                        String name = script.split(":")[1].trim();
                        String story = script.split(":")[2].trim();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(DialogQuestManager.completeCustomObject(player, name)) {
                                    Msg.send(player,  "&6" + name + " &e목표를 달성했습니다!", pfix);
                                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, (float) Rand.randDouble(0.8,1.5));
                                    StoryManager.readStory(player, story);
                                }
                            }
                        }.runTask(plugin);

                    }
                    else if (sct.startsWith("!STORY")) {
                        String name = script.split(":")[1].trim();
                        StoryManager.readStory(player, name);
                    }
                    else if (sct.startsWith("!UNRIDE")) {
                        RideManager manager = RukonRiding.inst().getRideManager();
                        if(manager.isPlayerRiding(player)) manager.despawnRiding(player);
                    }
                    else if (sct.startsWith("!ENTERTITLE")) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                String name = script.split(":")[1].trim();
                                player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 1, Rand.randFloat(0.8,1.2));
                                Msg.sendTitle(player, "&f\uF100\uE00C"+name+"&f\uE00C\uF101", "&7\uE109지역에 입장합니다.&f\uE108\uF102", 40, 50, 40);
                            }
                        }.runTaskLater(plugin, 10);
                    }
                    else if (sct.startsWith("!WARDROBE")) {
                        if(!playerInWardrobe.add(player)) {
                            playerInWardrobe.remove(player);
                        }
                    }
                    else if (sct.startsWith("!STARTSTORY")) {
                        List<String> scr = new ArrayList<>();
                        while(itr.hasNext()) {
                            scr.add(itr.next());
                        }
                        StoryManager.readStoryInstantly(player, scr);
                        return;
                    }
                }
            }
        }.runTask(plugin);

        return true;
    }

    /*
    Region Events example
    enterEvents:
    - 'completeObject:<목표이름>' //해당 커스텀 목표를 완료
    - 'story:<스토리 이름>' //해당 스토리를 재생
    - completeAndRead:<목표이름>:<목표를 완료했다면 읽을 스토리>
    exitEvents:
    - 'actions...'
     */

    @EventHandler
    public void onSelectRegion(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(!player.isOp()) return;
        if(!player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(!player.getInventory().getItemInMainHand().getType().equals(Material.BONE)) return;
        e.setCancelled(true);
        if(blockInteract.contains(player)) return;
        blockInteract.add(player);
        if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            pos1.put(player, e.getClickedBlock().getLocation());
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
            Msg.send(player, "&61번 지역이 설정되었습니다.", pfix);
        }
        else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            pos2.put(player, e.getClickedBlock().getLocation());
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
            Msg.send(player, "&62번 지역이 설정되었습니다.", pfix);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                blockInteract.remove(player);
            }
        }.runTaskLater(plugin, 2);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        pos1.remove(player);
        pos2.remove(player);
        playerRegion.putIfAbsent(player, new HashSet<>());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        if(!e.getFrom().getWorld().equals(e.getTo().getWorld()) || e.getTo().distanceSquared(e.getFrom()) > 40000) {
            playerRegion.put(player, new HashSet<>());
            DialogQuestManager.setNavigatingQuest(player, null);
        }
    }
}
