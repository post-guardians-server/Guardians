package me.rukon0621.guardians.areawarp;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class AreaManger implements Listener {
    private static HashMap<String, Area> areaData;
    private static Area[][] areaMap;
    private static int mapSize;
    private static HashMap<Player, Integer> playerX;
    private static HashMap<Player, Integer> playerY;
    private static final main plugin = main.getPlugin();
    private static final String guiName = "&f\uF000\uF01D";

    private static Configure getConfig() {
        return new Configure("areaData.yml", FileUtil.getOuterPluginFolder().getPath());
    }
    public static Area getArea(String areaName) {
        return areaData.get(areaName);
    }

    public static HashMap<String, Area> getAreaData() {
        return areaData;
    }

    public AreaManger() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reloadAreaData();
    }
    public static void reloadAreaData() {
        areaData = new HashMap<>();
        playerX = new HashMap<>();
        playerY = new HashMap<>();
        Configure config = getConfig();
        if(!config.getConfig().getKeys(false).contains("size")) {
            config.getConfig().set("size", 10);
            config.saveConfig();
        }
        mapSize = config.getConfig().getInt("size");
        areaMap = new Area[mapSize][mapSize];
        for(String name : config.getConfig().getKeys(false)) {
            if(name.equals("size")) continue;
            Area area = (Area) config.getConfig().get(name);
            if(!area.isHide()) areaMap[area.getMapX()][area.getMapY()] = area;
            areaData.put(name, area);
        }
        for(Player player : Bukkit.getOnlinePlayers()) {
            EquipmentManager.reloadEquipment(player, false);
        }
    }
    public static void createNewArea(Player player, String name) {
        if(areaData.containsKey(name)) {
            Msg.warn(player, "&c이미 존재하는 이름의 지역입니다. /지역 수정 명령어를 통해 지역의 위치를 갱신할 수 있습니다.");
            return;
        }
        ItemClass icon = new ItemClass(new ItemStack(Material.TURTLE_EGG), name);
        icon.setCustomModelData(1);
        ArrayList<String> conditions = new ArrayList<>();
        conditions.add("levelAbove:0");
        Area area = null;
        try {
            area = new Area(player.getLocation(), player.getLocation(), name, icon.getItem(), new ArrayList<>(), 5, 5, conditions, new ArrayList<>(), 1, new AreaEnvironment(null, "늪지", "습기", 10), false);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("오류 발생");
        }
        areaData.put(name, area);
        Configure config = getConfig();
        config.getConfig().set(name, area);
        config.saveConfig();
        Msg.send(player, "성공적으로 생성했습니다.", pfix);
    }
    public static void deleteArea(Player player, String name) {
        if(!areaData.containsKey(name)) {
            Msg.warn(player, "&c존재하는 지역이 아닙니다.");
            return;
        }
        areaData.remove(name);
        Configure config = getConfig();
        config.getConfig().set(name, null);
        config.saveConfig();
        Msg.send(player, "성공적으로 삭제했습니다.", pfix);
    }
    public static void changeAreaLocation(Player player, String name) {
        if(!areaData.containsKey(name)) {
            Msg.warn(player, "&c존재하는 지역이 아닙니다.");
            return;
        }
        Area area = areaData.get(name);
        area.setLocation(player.getLocation());
        Configure config = getConfig();
        config.getConfig().set(name, area);
        config.saveConfig();
        Msg.send(player, "성공적으로 위치를 수정했습니다.", pfix);
    }

    public static void changeReturnLocation(Player player, String name) {
        if(!areaData.containsKey(name)) {
            Msg.warn(player, "&c존재하는 지역이 아닙니다.");
            return;
        }
        Area area = areaData.get(name);
        area.setReturnLocation(player.getLocation());
        Configure config = getConfig();
        config.getConfig().set(name, area);
        config.saveConfig();
        Msg.send(player, "성공적으로 위치를 수정했습니다.", pfix);
    }
    public static void changeAreaIcon(Player player, String name) {
        if(!areaData.containsKey(name)) {
            Msg.warn(player, "&c존재하는 지역이 아닙니다.");
            return;
        }
        if(player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            Msg.warn(player, "손에 아이템을 들어주세요.");
            return;
        }
        Area area = areaData.get(name);
        area.setIcon(player.getInventory().getItemInMainHand());
        Configure config = getConfig();
        config.getConfig().set(name, area);
        config.saveConfig();
        Msg.send(player, "성공적으로 아이콘을 수정했습니다.", pfix);
    }
    public static void teleportToArea(Player player, String name) {
        teleportToArea(player, name, false);
    }
    public static void teleportToArea(Player player, String name, boolean force) {
        if(!areaData.containsKey(name)) {
            Msg.warn(player, "&c존재하는 지역이 아닙니다.");
            return;
        }
        areaData.get(name).warp(player, force);
        Msg.send(player, "성공적으로 이동했습니다.", pfix);
    }
    public static void showListOfArea(Player player) {
        Msg.send(player, "서버에 존재하는 에리어 목록입니다.", pfix);
        for(String name : areaData.keySet()) {
            Msg.send(player, name);
        }
    }

    public static void openAreaGUI(Player player) {
        Area area = areaData.get(new PlayerData(player).getArea());
        playerX.put(player, area.getMapX());
        playerY.put(player, area.getMapY());
        InvClass inv = new InvClass(6, guiName);
        reloadVoyageGUI(player, inv.getInv(), playerX.get(player), playerY.get(player));
        player.openInventory(inv.getInv());
    }

    /**
     * x,y 상대 위치를 기반으로 맵 GUI를 리로드
     * @param inv inv
     * @param x xLoc
     * @param y yLoc
     */
    private static void reloadVoyageGUI(Player player, Inventory inv, int x, int y) {
        int slot = -1;
        for(int dy = y - 2; dy <= y + 3; dy++) {
            for(int dx = x - 4; dx <= x + 4; dx++) {
                slot++;
                try {
                    if(areaMap[dx][dy]==null) inv.setItem(slot, null);
                    else {
                        Area area = areaMap[dx][dy];
                        inv.setItem(slot, area.getFinalIcon(player));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    inv.setItem(slot, null);
                }
            }
        }
        ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&c『 위로 』");
        item.setCustomModelData(7);
        inv.setItem(43, item.getItem());
        item.setName("&c『 아래로 』");
        inv.setItem(52, item.getItem());
        item.setName("&c『 오른쪽으로 』");
        inv.setItem(53, item.getItem());
        item.setName("&c『 왼쪽으로 』");
        inv.setItem(51, item.getItem());
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals(guiName)) return;
        e.setCancelled(true);

        if(e.getRawSlot()==-999) {
            player.closeInventory();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.4f);
            return;
        }
        if(e.getCurrentItem()==null) return;

        //방향키 이동
        int y = playerY.get(player);
        int x = playerX.get(player);
        if(e.getCurrentItem().getType().equals(Material.SCUTE)) {
            if(e.getRawSlot()==43) {
                y--;
                if(y<2) y = 2;
            }
            else if(e.getRawSlot()==52) {
                y++;
                if(y>mapSize - 4) y = mapSize - 4;
            }
            else if(e.getRawSlot()==51) {
                x--;
                if(x < 4) x = 4;
            }
            else if(e.getRawSlot()==53) {
                x++;
                if(x > mapSize - 5) x = mapSize - 5;
            }
            playerX.put(player, x);
            playerY.put(player, y);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, Rand.randFloat(1.2, 1.5));
            reloadVoyageGUI(player, e.getInventory(), x, y);
            return;
        }

        //실제 이동
        x = x + (e.getRawSlot() % 9 - 4);  //클릭한 곳의 X좌표
        y = y + (e.getRawSlot() / 9 - 2); //클릭한 곳의 y좌표
        Area area = areaMap[x][y];
        area.warp(player);
        player.closeInventory();
    }

}
