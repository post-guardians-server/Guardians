package me.rukon0621.guardians.areawarp;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.helper.ItemClass;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AreaEnvironment implements ConfigurationSerializable {
    private final String environType;
    private final String attrType;
    private final int decreasing;
    private final Map<Stat, Double> statMap = new HashMap<>();

    public AreaEnvironment(Map<String, Double> statMap, String environType, String attrType, int decreasing) throws Exception {
        if(statMap != null) {
            for(String key : statMap.keySet()) {
                Stat stat = Stat.valueOf(key.trim().toUpperCase());
                if(stat.isUsingPercentage()) {
                    this.statMap.put(stat, statMap.get(key) / 100);
                }
                else this.statMap.put(stat, statMap.get(key));
            }
        }

        this.environType = environType;
        this.attrType = attrType;
        this.decreasing = decreasing;
        ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "env");
        ItemData itemData = new ItemData(item);
        itemData.setType("null");
    }

    public Map<String, Double> getParsedStatMap() {
        Map<String, Double> map = new HashMap<>();
        for(Stat stat : statMap.keySet()) {
            if(stat.isUsingPercentage()) map.put(stat.toString(), statMap.get(stat) * 100);
            else map.put(stat.toString(), statMap.get(stat));
        }
        return map;
    }

    public String getEnvironType() {
        return environType;
    }

    public String getAttrType() {
        return attrType;
    }

    public int getDecreasing() {
        return decreasing;
    }

    public Map<Stat, Double> getStatMap() {
        return statMap;
    }
    public double getParsedStat(Stat stat, int resLevel) {
        return statMap.get(stat) * (1 - resLevel * decreasing * 0.01);
    }

    public static AreaEnvironment deserialize(Map<String, Object> map) throws Exception {
        return new AreaEnvironment((Map<String, Double>) map.get("environmentLores"),
                (String) map.getOrDefault("environType", "습지"), (String) map.getOrDefault("attrType", "습기"), (Integer) map.getOrDefault("decreasing", 1));
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("environmentLores", getParsedStatMap());
        map.put("environType", environType);
        map.put("attrType", attrType);
        map.put("decreasing", decreasing);
        return map;
    }
}
