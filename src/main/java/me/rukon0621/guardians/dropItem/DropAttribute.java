package me.rukon0621.guardians.dropItem;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("customDropAttributes")
public class DropAttribute implements ConfigurationSerializable {

    private final String attributeName;
    private final int attrLevel;
    private final int levelLimit;
    private final double chance;

    public DropAttribute(String attrName, int attrLevel, int levelLimit, double chance) {
        this.attributeName = attrName;
        this.attrLevel = attrLevel;
        this.levelLimit = levelLimit;
        this.chance = chance;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public int getAttrLevel() {
        return attrLevel;
    }

    public int getLevelLimit() {
        return levelLimit;
    }

    public double getChance() {
        return chance;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("attrName", attributeName);
        if(levelLimit!=0) data.put("levelLimit", levelLimit);
        if(attrLevel!=1) data.put("attrLevel", attrLevel);
        if(chance!=100.0) data.put("chance", chance);
        return data;
    }

    public static DropAttribute deserialize(Map<String, Object> data) {
        int attrLevel, levelLimit;
        Number chance;
        attrLevel = (int) data.getOrDefault("attrLevel", 1);
        levelLimit = (int) data.getOrDefault("levelLimit", 0);
        chance = (Number) data.getOrDefault("chance", 100.0);
        return new DropAttribute((String) data.get("attrName"), attrLevel, levelLimit, chance.doubleValue());
    }

}
