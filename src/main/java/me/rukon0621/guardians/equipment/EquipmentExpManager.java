package me.rukon0621.guardians.equipment;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import me.rukon0621.guardians.helper.ItemSaver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentExpManager {

    private final Map<String, Double> expDropMap = new HashMap<>(); // <pureMobName, DropExp>
    private final Map<Integer, Double> expReqMap = new HashMap<>(); //사증 렙업에 필요한 양
    private final Map<Integer, double[]> statMap = new HashMap<>(); //사증 레벨의 스텟 양
    private static final String[] expTarget = new String[]{"사증"};
    private static final int maxLevel = 200;

    public EquipmentExpManager() {
        reload();
    }

    private Configure getConfig() {
        return new Configure(FileUtil.getOuterPluginFolder() + "/equipmentExpDropList.yml");
    }
    private File getExpDataFile() {
        return new File(FileUtil.getOuterPluginFolder() + "/equipmentExpData.yml");
    }

    public void reload() {
        expDropMap.clear();
        expReqMap.clear();
        statMap.clear();
        expReqMap.put(0, 1D);
        statMap.put(0, new double[]{0,0,0});
        Configure config = getConfig();
        for(String key : config.getConfig().getKeys(false)) {
            expDropMap.put(key, config.getConfig().getDouble(key, 0));
        }
        File file = getExpDataFile();
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine()) != null) {
                String[] data = line.split(";");
                int level = Integer.parseInt(data[0]);
                double exp = Double.parseDouble(data[1]);
                double at = Double.parseDouble(data[2]);
                double am = Double.parseDouble(data[3]);
                double hp = Double.parseDouble(data[4]);
                if(level == 1) statMap.put(level, new double[]{at, am, hp});
                else statMap.put(level, new double[]{statMap.get(level - 1)[0] + at, statMap.get(level - 1)[1] + am, statMap.get(level - 1)[2] + hp});
                expReqMap.put(level, exp);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Deprecated
    public ItemStack reloadCertificateItem(ItemData original) {
        ItemData itemData = new ItemData(ItemSaver.getItem("『 가디언의 사증 』").getItem().clone());
        itemData.setLevel(original.getLevel());
        itemData.setExp(original.getExp());
        double[] stats = statMap.get(itemData.getLevel());
        itemData.setStat(Stat.ATTACK_DAMAGE, stats[0]);
        itemData.setStat(Stat.ARMOR, stats[1]);
        itemData.setStat(Stat.HEALTH, stats[2]);
        return itemData.getItemStack();
    }



    @Deprecated
    public boolean addExp(Player player, double exp) {
        String koreanKeyName = "사증";
        if(EquipmentManager.getItem(player, koreanKeyName).getType().equals(Material.AIR)) return false;
        ItemData itemData = new ItemData(EquipmentManager.getItem(player, koreanKeyName));
        int lv = itemData.getLevel();
        itemData.addExp(exp, true, maxLevel);
        EquipmentManager.setItem(player, koreanKeyName, reloadCertificateItem(itemData));
        return lv != itemData.getLevel();
    }

    @Deprecated
    /**
     * @param pureMobName 몬스터의 순수 이름
     * @return 해당 레벨의 몬스터를 잡았을때 드롭되는 경험치 양
     */
    public double getExpOfMob(String pureMobName) {
        return expDropMap.getOrDefault(pureMobName, 0D);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     *
     * @param level 가디언의 사증 레벨
     * @return 해당 레벨의 가디언 사증 경험치
     */
    public double getMaxExpAtLevel(int level) {
        return expReqMap.getOrDefault(level, 0D);
    }

}
