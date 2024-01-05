package me.rukon0621.guardians.region;

import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.listeners.OpenAudioListener;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SerializableAs("customRegion")
public class Region implements ConfigurationSerializable {
    private final Location pos1;
    private final Location pos2;
    private final String name;
    private final ArrayList<String> enterEvents;
    private final ArrayList<String> exitEvents;
    private final ArrayList<String> dialogConditions;
    private final ArrayList<String> questConditions;
    private final ArrayList<String> storyConditions;
    private final ArrayList<String> specialOptions;
    private final String bgm;
    private final int priority;
    public Region(String name, Location pos1, Location pos2) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        Configure config = RegionManager.getRegionConfig(name);
        enterEvents = (ArrayList<String>) config.getConfig().getList("enterEvents");
        exitEvents = (ArrayList<String>) config.getConfig().getList("exitEvents");
        dialogConditions = (ArrayList<String>) config.getConfig().getList("dialogConditions");
        questConditions = (ArrayList<String>) config.getConfig().getList("questConditions");
        storyConditions = (ArrayList<String>) config.getConfig().getList("storyConditions");
        specialOptions = (ArrayList<String>) config.getConfig().getList("specialOptions", new ArrayList<>());
        bgm = config.getConfig().getString("bgm", null);
        this.priority = config.getConfig().getInt("eventPriority", 10);
    }
    //check if player is in specific region.
    public boolean isInRegion(Player player) {
        Location loc = player.getLocation();
        if(!loc.getWorld().equals(pos1.getWorld())) return false;
        double x1, x2, y1, y2, z1, z2;
        if(pos1.getBlockX()<pos2.getBlockX()) {
            x1 = pos1.getBlockX();
            x2 = pos2.getBlockX();
        }
        else {
            x1 = pos2.getBlockX();
            x2 = pos1.getBlockX();
        }
        if(pos1.getBlockY()<pos2.getBlockY()) {
            y1 = pos1.getBlockY();
            y2 = pos2.getBlockY();
        }
        else {
            y1 = pos2.getBlockY();
            y2 = pos1.getBlockY();
        }
        if(pos1.getBlockZ()<pos2.getBlockZ()) {
            z1 = pos1.getBlockZ();
            z2 = pos2.getBlockZ();
        }
        else {
            z1 = pos2.getBlockZ();
            z2 = pos1.getBlockZ();
        }
        if(x1<=loc.getBlockX()&&loc.getBlockX()<=x2) {
            if(y1<=loc.getBlockY()&&loc.getBlockY()<=y2) {
                return z1 <= loc.getBlockZ() && loc.getBlockZ() <= z2;
            }
        }
        return false;
    }
    public String getName() {
        return name;
    }
    public ArrayList<String> getEnterEvents() {
        return enterEvents;
    }

    public ArrayList<String> getExitEvents() {
        return exitEvents;
    }

    public ArrayList<String> getDialogConditions() {
        return dialogConditions;
    }
    public ArrayList<String> getQuestConditions() {
        return questConditions;
    }
    public ArrayList<String> getStoryConditions() {
        return storyConditions;
    }

    public boolean hasBgm() {
        return bgm != null;
    }

    public void playBgm(Player player) {
        if(bgm==null) return;
        OpenAudioListener.playBgm(player, bgm);
    }

    //Custom Serializer
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("pos1", pos1);
        data.put("pos2", pos2);
        return data;
    }

    public static Region deserialize(Map<String, Object> data) {
        return new Region((String) data.get("name"), (Location) data.get("pos1"), (Location) data.get("pos2"));
    }

    public ArrayList<String> getSpecialOptions() {
        return specialOptions;
    }

    public int getPriority() {
        return priority;
    }
}