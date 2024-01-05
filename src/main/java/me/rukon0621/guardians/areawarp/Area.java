package me.rukon0621.guardians.areawarp;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*

Special Conditions

levelAbove:<num> - 특정 레벨 이상이 되야지 입장 가능

 */
@SerializableAs("customAreaLocation")
public class Area implements ConfigurationSerializable {
    private final String name;
    private ItemStack icon;
    private final ItemStack finalIcon;
    private Location loc;
    private Location returnLoc;
    private final ArrayList<String> conditions;
    private final ArrayList<String> battleBgm;
    private final List<String> flagConditions;
    private final int mapX;
    private final int mapY;
    private final int enhanceStoneLevel;
    private final boolean pvp;
    private int levelAbove = -1;
    private int levelBelow = 9999;
    @Nullable
    private final AreaEnvironment environment;
    private final boolean hide;

    public Area(Location loc, Location returnLoc, String name, ItemStack icon, List<String> flagConditions, int x, int y, ArrayList<String> conditions, ArrayList<String> battleBgm, int enhanceStoneLevel, @Nullable AreaEnvironment environment, boolean hide) {
        this.loc = loc;
        this.name = name;
        this.flagConditions = flagConditions;
        this.battleBgm = battleBgm;
        this.enhanceStoneLevel = enhanceStoneLevel;
        this.environment = environment;
        this.returnLoc = returnLoc;
        this.hide = hide;

        try {
            for(String condition : conditions) {
                if(condition.toLowerCase().startsWith("levelabove")) {
                    levelAbove = Integer.parseInt(condition.split(":")[1].trim());
                }
                else if(condition.toLowerCase().startsWith("levelbelow")) {
                    levelBelow = Integer.parseInt(condition.split(":")[1].trim());
                }
            }
        } catch (Exception e) {
            main.getPlugin().getLogger().warning(name + " : 에리어 로딩 중 조건식에서 에러가 발견되었습니다.");
        }
        this.icon = icon;
        ItemClass ic = new ItemClass(new ItemStack(icon));
        if(conditions.size()>0) ic.addLore(" ");
        if(levelAbove!=-1) {
            if(levelBelow!=9999) {
                ic.addLore("&c\uE011 입장 레벨 제한: " + levelAbove + " ~ " + levelBelow);
            }
            else {
                ic.addLore("&c\uE011 입장 레벨 제한: " + levelAbove + " 이상");
            }
        }
        else if(levelBelow!=9999) {
            ic.addLore("&c\uE011 입장 레벨 제한: " + levelBelow + " 이하");
        }
        pvp = conditions.contains("pvp");
        if(pvp) {
            ic.addLore(" ");
            ic.addLore("&4※PVP 허용 구역");
        }
        if(conditions.contains("playerDamageHalf")) {
            ic.addLore("&6※이 섬에서는 플레이어에게 받는 피해가 20% 감소합니다.");
        }
        this.finalIcon = ic.getItem();
        mapX = x;
        mapY = y;
        this.conditions = conditions;
    }
    public String getName() {
        return name;
    }

    public ItemStack getFinalIcon(Player player) {
        if(environment==null) return finalIcon;
        ItemClass item = new ItemClass(finalIcon.clone());
        int resistanceLevel = new PlayerData(player).getEnvironmentResistance().getOrDefault(environment.getAttrType(), 0);
        item.addLore(" ");
        item.addLore("&c\uE014\uE00C\uE00C" + environment.getEnvironType() + " 환경에 의한 영향");
        for(Stat stat : environment.getStatMap().keySet()) {
            if(stat.isUsingPercentage()) {
                item.addLore(String.format("   &7- %s: %.2f%%", stat.getKorName(), environment.getParsedStat(stat, resistanceLevel) * 100));
            }
            else {
                item.addLore(String.format("   &7- %s: %.2f", stat.getKorName(), environment.getParsedStat(stat, resistanceLevel)));
            }
        }
        item.addLore(" ");
        if(resistanceLevel==0) {
            item.addLore("&f\uE011\uE00C\uE00C&e" + environment.getAttrType() + " 적응력 속성&f을 통해 환경의 영향을 덜 받을 수 있습니다.");
        }
        else item.addLore(String.format("&f\uE011\uE00C\uE00C&e%d레벨의 %s 적응력&f으로 &6%d%%&f만큼 환경에 영향을 덜 받습니다.", resistanceLevel, environment.getAttrType(), Math.min(100, environment.getDecreasing() * resistanceLevel)));
        return item.getItem();
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }
    public void setReturnLocation(Location loc) {
        this.returnLoc = loc;
    }
    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }
    public Location getLoc() {
        return loc;
    }
    public int getMapX() {
        return mapX;
    }
    public int getMapY() {
        return mapY;
    }
    public boolean pvpEnabled() { return pvp; }

    public int getEnhanceStoneLevel() {
        return enhanceStoneLevel;
    }

    public ArrayList<String> getBattleBgm() {
        return battleBgm;
    }

    public boolean isHide() {
        return hide;
    }

    /**
     * force가 true면 조건 확인 없이 이동
     * @param player player
     * @param force 조건을 신경쓸 것인가
     */
    public void warp(Player player, boolean force) {
        //이동 조건 확인
        PlayerData pdc = new PlayerData(player);
        if(!force) {
            if(pdc.getLevel()< levelAbove) {
                Msg.warn(player, "&c레벨이 낮아 이동할 수 없습니다.");
                return;
            }
            else if(pdc.getLevel()>levelBelow) {
                Msg.warn(player, "&c레벨이 너무 높아 이동할 수 없습니다.");
                return;
            }
            for(String cond : flagConditions) {
                if(cond.startsWith("not!")) {
                    if(StoryManager.getReadStory(player).contains("flag_" + cond.replaceFirst("not!", ""))) {
                        Msg.warn(player, "조건에 만족하지 않아 이동할 수 없습니다.");
                        return;
                    }
                }
                else {
                    if(!StoryManager.getReadStory(player).contains("flag_" + cond.replaceFirst("not!", ""))) {
                        Msg.warn(player, "조건에 만족하지 않아 이동할 수 없습니다.");
                        return;
                    }
                }
            }

        }
        pdc.setArea(name);
        EquipmentManager.reloadEquipment(player, false);
        player.teleportAsync(loc);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(player, Sound.ENTITY_BAT_TAKEOFF, 1, 0.8f);
            }
        }.runTaskLater(main.getPlugin(), 1);
    }
    /**
     * 이동 가능한지 확인하고 해당 플레이어를 해당 지역으로 텔레포트
     * @param player player
     */
    public void warp(Player player) {
        warp(player, false);
    }

    public Location getReturnLoc() {
        return returnLoc;
    }

    public ArrayList<String> getConditions() {
        return conditions;
    }

    public List<String> getFlagConditions() {
        return flagConditions;
    }

    @Nullable
    public AreaEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", loc);
        data.put("returnLocation", returnLoc);
        data.put("name", name);
        data.put("icon", icon);
        data.put("x", mapX);
        data.put("y", mapY);
        data.put("conditions", conditions);
        data.put("battleBgm", battleBgm);
        data.put("environment", environment);
        data.put("hide", hide);
        data.put("flagConditions", flagConditions);
        data.put("enhanceStoneLevel", enhanceStoneLevel);
        return data;
    }

    public static Area deserialize(Map<String, Object> data) {
        return new Area((Location) data.get("location"), (Location) data.getOrDefault("returnLocation", data.get("location")),
                (String) data.get("name"), (ItemStack) data.get("icon"), (List<String>) data.getOrDefault("flagConditions", new ArrayList<>()), (Integer) data.get("x"), (Integer) data.get("y"),
                (ArrayList<String>) data.get("conditions"), (ArrayList<String>) data.getOrDefault("battleBgm", new ArrayList<>()),
                (Integer) data.getOrDefault("enhanceStoneLevel", 1), (AreaEnvironment) data.getOrDefault("environment", null), (Boolean) data.getOrDefault("hide", false));
    }
}
