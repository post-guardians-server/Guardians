package me.rukon0621.guardians.vote;

import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class VoteManager {

    private final Map<Integer, ItemStack> items = new HashMap<>();

    public VoteManager() {
        reload();
    }

    public Configure getConfig() {
        return new Configure(FileUtil.getOuterPluginFolder() + "/voteCheckItems.yml");
    }

    public void reload() {
        items.clear();
        Configure config = getConfig();
        for(String key : config.getConfig().getKeys(false)) {
            items.put(Integer.parseInt(key), config.getConfig().getItemStack(key));
        }
    }

    public Map<Integer, ItemStack> getItems() {
        return items;
    }

    public ItemStack getItem(int day) {
        return items.get(day);
    }

    public void setItem(int day, ItemStack item) {
        Configure config = getConfig();
        config.getConfig().set(String.valueOf(day), item);
        config.saveConfig();
        items.put(day, item);
    }
}
