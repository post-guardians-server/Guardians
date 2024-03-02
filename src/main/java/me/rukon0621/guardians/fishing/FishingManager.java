package me.rukon0621.guardians.fishing;

import me.rukon0621.guardians.areawarp.Area;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.ItemGrade;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class FishingManager implements Listener {
    private final Map<String, Map<ItemGrade, Set<String>>> fishingData = new HashMap<>();

    private final double[] stackedArrayList = new double[8];

    public FishingManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, main.getPlugin());
        reloadFishingData();
        for(int x = 0; x < 8; x++) {
            stackedArrayList[x] = Math.sqrt(-(14 * x - 100)) * 1.3;
            if(x > 0) stackedArrayList[x] += stackedArrayList[x - 1];
        }
    }

    public void reloadFishingData() {
        File folder = new File(FileUtil.getOuterPluginFolder() + "/rukonFishing");
        if(!folder.exists()) folder.mkdir();
        for(Area area : AreaManger.getAreaData().values()) {
            new Configure(folder.getPath() + "/" + area.getName() + ".yml");
        }
        Bukkit.getLogger().info("낚시 정보 리로드 중...");
        fishingData.clear();
        for(File file : folder.listFiles()) {
            String area = file.getName().replaceAll(".yml", "");
            if(!area.equals("default") && AreaManger.getArea(area) == null) {
                Bukkit.getLogger().warning(area + " - 이 지역은 존재하지 않는 지역입니다.");
                continue;
            }
            Configure config = new Configure(file);
            Map<ItemGrade, Set<String>> data = new HashMap<>();
            for(String key : config.getConfig().getKeys(false)) {
                try {
                    Set<String> set = new HashSet<>();
                    ItemGrade grade = ItemGrade.valueOf(key.toUpperCase());
                    for(String item : config.getConfig().getStringList(key)) {
                        if(!ItemSaver.isItemExist(item)) {
                            Bukkit.getLogger().severe(item + " - 이 아이템 세이버를 탐색할 수 없습니다.");
                            continue;
                        }
                        set.add(item);
                    }
                    data.put(grade, set);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning(key + "은(는) 존재하는 않는 등급입니다.");
                }
            }
            fishingData.put(area, data);
        }

        for(ItemGrade grade : fishingData.get("default").keySet()) {
            for(String s : fishingData.get("default").get(grade)) {
                for(Area area : AreaManger.getAreaData().values()) {
                    if(!fishingData.get(area.getName()).containsKey(grade)) {
                        fishingData.get(area.getName()).put(grade, new HashSet<>());
                    }
                    fishingData.get(area.getName()).get(grade).add(s);
                }
            }
        }
        for (String area : fishingData.keySet()) {
            System.out.println("area - " + area);
            for(ItemGrade grade : fishingData.get(area).keySet()) {
                System.out.println(grade);
                for(String s : fishingData.get(area).get(grade)) {
                    System.out.println(s);
                }
            }
        }
        Bukkit.getLogger().info("낚시 정보 리로드 완료!");
    }

    public Set<String> getFishingData(String area, ItemGrade grade) {
        if(!fishingData.get(area).containsKey(grade)) return new HashSet<>();
        return fishingData.get(area).get(grade);
    }
    public static ItemGrade generateGrade(double startRange) {
        double d = Rand.randDouble(startRange, 100);
        for(int i = ItemGrade.values().length - 1; i >= 0; i--) {
            ItemGrade grade = ItemGrade.values()[i];
            if(d >= (100 - grade.getFishingChance())) return grade;
        }
        return ItemGrade.NORMAL;
    }

    public double getFishPrice(ItemData fishData) {
        long price;
        /*
        switch (fishData.getGrade()) {
            case NORMAL -> price = 34;
            case UNCOMMON -> price = 38;
            case UNIQUE -> price = 1190;
            case EPIC -> price = 2652;
            case LEGEND -> price = 11866;
            case ANCIENT -> price = 399398;
            default -> price = 0;
        }
         */
        switch (fishData.getGrade()) {
            case NORMAL -> price = 100;
            case UNCOMMON -> price = 200;
            case UNIQUE -> price = 3200;
            case EPIC -> price = 7500;
            case LEGEND -> price = 32000;
            case ANCIENT -> price = 399398;
            default -> price = 0;
        }
        price *= fishData.getQuality();
        price *= (fishData.getLevel() / 25D);
        price /= 30;
        return price;
    }

    @Nullable
    public ItemData getResult(Player player, ItemData rodData) {
        PlayerData pdc = new PlayerData(player);
        String area = pdc.getArea();
        int qualLevel = rodData.getAttrLevel("고품질 포획률 증가");
        int gradeLevel = rodData.getAttrLevel("고등급 포획률 증가") / 2;
        int addLevel = rodData.getAttrLevel("고레벨 포획률 증가") / 2;

        ItemGrade resultGrade = generateGrade(stackedArrayList[gradeLevel]);
        while(fishingData.get(area).get(resultGrade) == null || fishingData.get(area).get(resultGrade).isEmpty()) {
            resultGrade = ItemGrade.values()[resultGrade.ordinal() - 1];
        }

        Set<String> items = new HashSet<>(getFishingData(area, resultGrade));
        if(items.isEmpty()) {
            Msg.warn(player, "오류: 비어있는 데이터를 참조했습니다.");
            return null;
        }
        ItemData fishData = new ItemData(ItemSaver.getItem(Rand.getRandomCollectionElement(items)));
        fishData.setQuality(generateQualityByLevel(qualLevel));
        fishData.setLevel(new PlayerData(player).getLevel() - Rand.randInt(0, (10 - addLevel)));
        player.getInventory().setItemInMainHand(rodData.getItemStack());
        return fishData;
    }

    public static double generateQualityByLevel(int level) {
        double[] stackedQualityArrayList = new double[100];
        for(int i = 0;i < 100; i++) {
            stackedQualityArrayList[i] = pdf(i, level);
            if(i > 0) stackedQualityArrayList[i] += stackedQualityArrayList[i - 1];
        }
        double d = Rand.randDouble(0, stackedQualityArrayList[99]);
        int qual = 0;
        while(d > stackedQualityArrayList[qual]) {
            qual++;
            if(qual > 99) {
                qual = 99;
                break;
            }
        }
        return qual + new Random().nextDouble();
    }

    private static double pdf(int x, int level) {
        return Math.pow(Math.E, -(Math.pow((x - (50 + (level * 1.5))), 2)/500)) * 100;
    }
}
