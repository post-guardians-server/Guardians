package me.rukon0621.guardians.storage;

import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemSaver;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("customStorage")
public class Storage implements ConfigurationSerializable {
    private String name;
    private HashMap<Integer, ItemStack> itemData;
    private final int index;

    public Storage(String name, HashMap<Integer, ItemStack> itemData, int index) {
        this.name = name;
        this.itemData  = itemData;
        this.index = index;
    }

    public InvClass getInventory() {
        InvClass inv = new InvClass(6, "\uE200\uE200\uE201\uE207");
        for(int slot : itemData.keySet()) {
            inv.setslot(slot, itemData.get(slot));
        }
        return inv;
    }

    public int getIndex() {
        return index;
    }

    public void setItemData(HashMap<Integer, ItemStack> itemData) {
        this.itemData = itemData;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public static Storage deserialize(Map<String, Object> map) {
        HashMap<Integer, ItemStack> itemData = (HashMap<Integer, ItemStack>) map.get("itemData");
        HashMap<Integer, ItemStack> reloadedItemData = new HashMap<>();
        for(Integer i : itemData.keySet()) {
            reloadedItemData.put(i, ItemSaver.reloadItem(itemData.get(i)));
        }
        return new Storage((String) map.get("name"), reloadedItemData, (Integer) map.get("index"));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("index", index);
        map.put("itemData", itemData);
        return map;
    }
}
