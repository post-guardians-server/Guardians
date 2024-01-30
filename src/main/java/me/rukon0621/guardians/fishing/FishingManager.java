package me.rukon0621.guardians.fishing;

import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.ItemGrade;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class FishingManager implements Listener {
    private final Map<String, Map<ItemGrade, Set<String>>> fishingData = new HashMap<>();
    private final Map<String, ItemGrade> highestGradeAtArea = new HashMap<>();

    public FishingManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, main.getPlugin());
    }

    public void reloadFishingData() {
        File folder = new File(FileUtil.getOuterPluginFolder() + "/rukonFishing");
        if(!folder.exists()) folder.mkdir();
        Bukkit.getLogger().info("낚시 정보 리로드 중...");
        fishingData.clear();
        highestGradeAtArea.clear();
        for(File file : folder.listFiles()) {
            String area = file.getName().replaceAll(".yml", "");
            if(!area.equals("default") && AreaManger.getArea(area) == null) {
                Bukkit.getLogger().warning(area + " - 이 지역은 존재하지 않는 지역입니다.");
                continue;
            }
            Configure config = new Configure(file);
            Map<ItemGrade, Set<String>> data = new HashMap<>();
            highestGradeAtArea.put(area, ItemGrade.NORMAL);
            for(String key : config.getConfig().getKeys(false)) {
                try {
                    Set<String> set = new HashSet<>();
                    ItemGrade grade = ItemGrade.valueOf(key.toUpperCase());
                    highestGradeAtArea.put(area, (highestGradeAtArea.get(area).ordinal() < grade.ordinal() ? grade : highestGradeAtArea.get(area)));
                    for(String item : config.getConfig().getStringList(key)) {
                        if(!ItemSaver.isItemExist(item)) {
                            Bukkit.getLogger().warning(item + " - 이 아이템 세이버를 탐색할 수 없습니다.");
                            continue;
                        }
                        set.add(item);
                    }
                    if(set.isEmpty()) {
                        Bukkit.getLogger().warning(area + " - 이 지역에 대한 정보는 비어 있습니다.");
                    }
                    data.put(grade, set);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning(key + "은(는) 존재하는 않는 등급입니다.");
                }
            }
            fishingData.put(area, data);
        }
        for(String area : highestGradeAtArea.keySet()) {
            highestGradeAtArea.put(area, highestGradeAtArea.put(area, highestGradeAtArea.get("default").ordinal() > highestGradeAtArea.get(area).ordinal() ? highestGradeAtArea.get("default") : highestGradeAtArea.get(area)));
        }
        Bukkit.getLogger().info("낚시 정보 리로드 완료!");
    }

    public Set<String> getFishingData(String area, ItemGrade grade) {
        if(!fishingData.get(area).containsKey(grade)) return new HashSet<>();
        return fishingData.get(area).get(grade);
    }

    @Nullable
    public ItemData getResult(Player player, ItemData rod) {
        PlayerData pdc = new PlayerData(player);
        String area = pdc.getArea();
        double max = 0;
        for(ItemGrade grade : ItemGrade.values()) {
            if(highestGradeAtArea.get(area).ordinal() < grade.ordinal()) break;
            max += grade.getFishingChance();
        }
        double stacked = 0;
        double r = Rand.randDouble(0, max);
        ItemGrade resultGrade = null;
        for(ItemGrade grade : ItemGrade.values()) {
            if (highestGradeAtArea.get(area).ordinal() < grade.ordinal()) break;
            stacked += grade.getFishingChance();
            if(r >= stacked) {
                resultGrade = grade;
            }
        }
        if(resultGrade == null) {
            Msg.warn(player, "낚시 도중 오류가 발생했습니다.");
            return null;
        }
        Set<String> items = new HashSet<>(getFishingData(area, resultGrade));
        items.addAll(getFishingData("default", resultGrade));
        if(items.isEmpty()) {
            Msg.warn(player, "오류: 비어있는 데이터를 참조했습니다.");
            return null;
        }
        String result = Rand.getRandomCollectionElement(items);
        return new ItemData(ItemSaver.getItem(result));
    }
}
