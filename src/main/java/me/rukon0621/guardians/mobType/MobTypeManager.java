package me.rukon0621.guardians.mobType;

import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.MobData;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class MobTypeManager {

    private final HashMap<String, String> mobTypeData = new HashMap<>();
    private List<String> typeData = new ArrayList<>();

    private Configure getConfig() {
        return new Configure(main.getPlugin(), "mobType.yml", FileUtil.getOuterPluginFolder().getPath());
    }

    public MobTypeManager() {
        reloadMapConfig();
    }

    public List<String> getTypes() {
        return typeData;
    }

    public HashMap<String, String> getMobTypeData() {
        return mobTypeData;
    }

    public List<String> getMobNames() {
        MobManager mobManager = MythicBukkit.inst().getMobManager();
        List<String> names = new ArrayList<>();
        for(MythicMob mm : mobManager.getMobTypes()) {
            if(mm.getDisplayName()==null) continue;
            String name = mm.getDisplayName().get();
            if(name==null) continue;
            names.add(Msg.uncolor(Msg.color(MobData.getMobData(name).getFirst())).trim());
        }
        return names;
    }

    private void reloadMapConfig() {
        mobTypeData.clear();
        Configure config = getConfig();
        for(String mob : config.getConfig().getKeys(false)) {
            if(mob.equals("typeData")) {
                typeData = (List<String>) config.getConfig().getList("typeData", new ArrayList<>());
            }
            else mobTypeData.put(mob, config.getConfig().getString(mob));
        }
    }

    public void createNewType(Player player, String type) {
        if(getTypes().contains(type)) {
            Msg.warn(player, "이미 존재하는 타입입니다.");
            return;
        }
        Configure config = ItemData.getAttrDataConfig();
        config.getConfig().set("속성 파괴력: "+type+".type", 1);
        config.getConfig().set("속성 저항력: "+type+".type", 1);
        config.getConfig().set("속성 반감: "+type+".type", 0);
        config.saveConfig();
        ItemData.reloadItemData();
        config = getConfig();
        typeData.add(type);
        config.getConfig().set("typeData", typeData);
        config.saveConfig();
        Msg.send(player, "새로운 몹 속성을 추가했습니다.", pfix);
    }

    public void deleteType(Player player, String type) {
        if(!getTypes().contains(type)) {
            Msg.warn(player, "존재하지 않는 타입입니다.");
            return;
        }
        Configure config = getConfig();
        typeData.remove(type);
        config.getConfig().set("typeData", typeData);

        for(String mob : config.getConfig().getKeys(false)) {
            if(config.getConfig().getString(mob).equals(type)) {
                config.getConfig().set(mob, null);
            }
        }

        config.saveConfig();
        reloadMapConfig();
        Msg.send(player, "새로운 몹 속성을 삭제했습니다.", pfix);
    }

    public void registerMobType(Player player, String mobName, String type) {
        if(!getTypes().contains(type)) {
            Msg.warn(player, "존재하지 않는 타입입니다.");
            return;
        }
        if(!getMobNames().contains(mobName)) {
            Msg.warn(player, "존재하지 않는 몹입니다.");
            return;
        }
        Configure config = getConfig();
        if(config.getConfig().getKeys(false).contains(mobName)) {
            Msg.warn(player, "이미 등록된 몬스터입니다.");
            return;
        }
        config.getConfig().set(mobName, type);
        config.saveConfig();
        reloadMapConfig();
        Msg.send(player, "성공적으로 등록했습니다.", pfix);
    }

    public void unregisterMobType(Player player, String mobName) {
        if(!getMobTypeData().containsKey(mobName)) {
            Msg.warn(player, "존재하지 않는 몹입니다.");
            return;
        }
        Configure config = getConfig();
        if(!config.getConfig().getKeys(false).contains(mobName)) {
            Msg.warn(player, "등록되지 않은 몬스터입니다.");
            return;
        }
        config.getConfig().set(mobName, null);
        config.saveConfig();
        reloadMapConfig();
        Msg.send(player, "성공적으로 등록을 해제했습니다.", pfix);
    }

    @Nullable
    public String getMobType(String filteredMobName) {
        return mobTypeData.get(filteredMobName);
    }

}
