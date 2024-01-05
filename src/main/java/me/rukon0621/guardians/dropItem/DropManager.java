package me.rukon0621.guardians.dropItem;

import me.rukon0621.backpack.BackPackUtil;
import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static me.rukon0621.guardians.main.pfix;

public class DropManager {
    private static final main plugin = main.getPlugin();
    private static HashMap<String, ArrayList<Drop>> dropData;
    private static final ArrayList<String> dropPathList = new ArrayList<>();

    public static Set<String> getDropNames() {
        return dropData.keySet();
    }

    public static HashMap<String, ArrayList<Drop>> getDropData() {
        return dropData;
    }

    /**
     *
     * @return 드롭의 path가 저장된 configure 객체를 반환
     */
    public static Configure getPathConfig() {
        return new Configure( "dropPath.yml", FileUtil.getOuterPluginFolder() + "/dropData");
    }

    /**
     * @param name 드롭 데이터 config의 이름
     * @return 해당 드롭 config의 configure 객체를 반환
     */
    private static Configure getDropDataConfig(String name) {
        return new Configure(FileUtil.getOuterPluginFolder() +"/dropData/drops/"+getPathConfig().getConfig().getString(name));
    }

    public static ArrayList<String> getDropPathList() {
        return dropPathList;
    }

    public static void reloadAllDropData() {
        dropData = new HashMap<>();
        Configure pathConfig = getPathConfig();
        pathConfig.saveConfig();
        dropPathList.clear();
        for(String name : pathConfig.getConfig().getKeys(false)) {
            System.out.println("드롭 로딩 중 - " + name);
            dropPathList.add(pathConfig.getConfig().getString(name));
            Configure config = getDropDataConfig(name);
            ArrayList<Drop> drops = (ArrayList<Drop>) config.getConfig().getList(name);
            dropData.put(name, drops);
        }
    }

    public static boolean hasDrop(String name) {
        return dropData.containsKey(name);
    }

    public static void giveDrop(Player player, String dropName,int level , double contribution) {
        List<ItemStack> items = new ArrayList<>();
        for(Drop drop : dropData.get(dropName)) {
            items.addAll(drop.makeDropList(player, level, contribution));
        }
        BackPackUtil.giveOrBackPack(player, items);

        //Logging
        new BukkitRunnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder(dropName);
                boolean first = true;
                sb.append("[");
                for(ItemStack item : items) {
                    if(first) first = false;
                    else sb.append(", ");
                    sb.append(new ItemData(item));
                }
                sb.append("]");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        LogManager.log(player, "drop", sb.toString());
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

    }

    public static ArrayList<ItemStack> getDropList(Player player, String dropName, int level, double contribution) {
        return getDropList(player, dropName, level, contribution, false);
    }

    public static ArrayList<ItemStack> getDropList(Player player, String dropName, int level, double contribution, boolean SHOW_MODE) {
        return getDropList(player, dropName, level, contribution, SHOW_MODE, false);
    }
    public static ArrayList<ItemStack> getDropList(Player player, String dropName, int level, double contribution, boolean SHOW_MODE, boolean ignoreLuckAndParty) {
        ArrayList<ItemStack> items = new ArrayList<>();
        ArrayList<Drop> drops = dropData.get(dropName);
        if(drops==null) return items;
        for(Drop drop : drops) {
            items.addAll(drop.makeDropList(player, level, contribution, SHOW_MODE, ignoreLuckAndParty));
        }
        return items;
    }

    public static void createNewDropData(Player player, String name, String path) {
        if(dropData.containsKey(name)) {
            Msg.warn(player, "이미 존재하는 이름의 데이터입니다.");
            return;
        }
        if(!path.endsWith(".yml")) path += ".yml";
        if(!dropPathList.contains(path)) dropPathList.add(path);

        Configure config = getPathConfig();
        config.getConfig().set(name, path);
        config.saveConfig();

        ArrayList<Drop> drops = new ArrayList<>();
        ArrayList<DropAttribute> dropAttrs = new ArrayList<>();
        dropAttrs.add(new DropAttribute("질김", 1, 0, 50));
        drops.add(new Drop("슬라임의 점액", 5, 10, 2, dropAttrs, 50, 0));
        config = getDropDataConfig(name);
        config.getConfig().set(name, drops);
        config.saveConfig();
        dropData.put(name, drops);
        Msg.send(player, "성공적으로 새로운 드롭데이터를 생성했습니다.", pfix);
    }

    public static void deleteDropData(Player player, String name) {
        if(!dropData.containsKey(name)) {
            Msg.warn(player, "해당 이름의 드롭 데이터는 존재하지 않습니다.");
            return;
        }
        Configure config = getDropDataConfig(name);
        config.getConfig().set(name, null);
        config.saveConfig();
        config = getPathConfig();
        config.getConfig().set(name, null);
        config.saveConfig();
        dropData.remove(name);
        if(config.getFile().length()==0) {
            config.delete("drops");
        }
        Msg.send(player, "드롭 데이터를 삭제했습니다.", pfix);
    }

    public static void showDropDataList(Player player) {
        Msg.send(player, "서버에 존재하는 드롭 데이터입니다", pfix);
        for(String name : getPathConfig().getConfig().getKeys(false)) {
            Msg.send(player, name + " &8: &7"+getPathConfig().getConfig().getString(name));
        }
    }

    /**
     * 해당 플레이어에게 특정 아이템을 줌, 인벤 공간이 없으면 메일 사용
     * @param player player
     * @param dropList dropList
     */
    public static void giveDropOrMail(Player player, ArrayList<ItemStack> dropList) {
        MailBoxManager.sendAll(player, dropList);
    }
}
