package me.rukon0621.guardians.data;

import me.rukon0621.guardians.GUI.item.ItemDisassembleWindow;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import me.rukon0621.pay.RukonPayment;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * ItemData 클래스는 아이템 로어에 존재하는 모든 정보를 담는 클래스다.
 *
 * 표기만 다를 뿐 레벨 1과 0은 같다. (0은 ?로 표기된다.)
 *
 * 새로운 스텟(ex: 공격력, 방어력)을 만들면 레벨당 증가 값을 고려해야한다. (setLevel 메소드)
 *  -> item stat mapping
 *  -> pdc base, item
 *  -> setter, getter, analyze, getItem
 *
 * 새로운 속성을 만들때는 해당 속성을 구분해야한다. (bad, good, rare)
 */
public class ItemData {
    protected static final main plugin = main.getPlugin();
    protected static final double QUALITY_MULTIPLIER = 0.4;
    protected final HashMap<String, Object> dataMap;
    //순서 때문에 ArrayList 사용
    protected final HashMap<Integer, List<String>> sectionMap;
    protected final ItemClass item;
    protected String name;
    protected final List<String> extraLore; //아이템에 들어있는 추가 설명 (단순 설명)
    protected static Set<String> weaponType; //무기 종류
    protected static Set<String> armorType; //무기 종류
    protected static Set<String> accessoryType; //무기 종류
    protected static Set<String> equipmentType; //장비 종류
    protected static Set<String> attrList; //속성 종류
    protected static Set<String> goodAttrList; //좋은 속성
    protected static Set<String> badAttrList; //저주 속성
    protected static Set<String> rareAttrList; //희귀 속성

    /*특정 아이템 타입에 따라 속성의 이름이 변화

    뼈(재료) 뾰족함
    검(무기) 치명타 확률 증가
    감옷(방어구) 가시

    Map
    Map<뾰족함, (무기 : 치명타 확률 증가), (방어구 : 가시)>
     */

    /**
     * 해당 아이템이 있는지 확인하고 이름을 비교 후 아이템을 가져감
     * @param player player
     * @param targetItem 가져갈 아이템
     * @param checkSeason 아이템의 유효 시즌을 확인할 것인가
     * @return 아이템을 성공적으로 가져갔는지 반환
     */
    public static boolean removeItem(Player player, ItemData targetItem, boolean checkSeason) {
        int season = RukonPayment.inst().getPassManager().getSeason();
        for(ItemStack item : player.getInventory().getStorageContents()) {
            if(item==null) continue;
            ItemData itemData = new ItemData(item);
            if(itemData.getName()==null) continue;
            if(itemData.getType()==null) continue;
            if(!itemData.getName().equals(targetItem.getName())) continue;
            if(checkSeason&&itemData.getSeason()!=season) continue;
            item.setAmount(item.getAmount() - 1);
            return true;
        }
        return false;
    }

    protected static HashMap<String, HashMap<String, String>> attrParsingMap;

    protected static HashMap<Integer, Long> levelData = new HashMap<>(); //해당 레벨에서 필요한 경험치의 량
    protected static final int maxLevel = LevelData.maxLevel;

    public HashMap<String, Object> getDataMap() {
        return dataMap;
    }

    public static Configure getAttrDataConfig() {
        return new Configure("attrData.yml", FileUtil.getOuterPluginFolder().getPath());
    }

    /**
     * 아이템 레벨업시 각 레벨당 필요한 경험치의 양
     * @param level 필요 경험치 양을 구할 레벨
     * @return 1000 배율된 레벨당 필요경험치
     */
    public static long getMaxExpAtLevel(int level) {
        if(level>maxLevel) return levelData.get(maxLevel) * 1000L;
        return levelData.getOrDefault(level, levelData.get(1)) * 1000L;
    }
    /**
     * 아이템 레벨업시 각 레벨당 필요한 경험치의 양
     * @param level 필요 경험치 양을 구할 레벨
     * @return 1000 배율된 레벨당 필요경험치
     */
    private static long getLongedMaxExpAtLevel(int level) {
        if(level>maxLevel) return levelData.get(maxLevel) * 1000L;
        return levelData.getOrDefault(level, levelData.get(1)) * 1000L;
    }


    public static void reloadItemData() {
        armorType = new HashSet<>();
        accessoryType = new HashSet<>();
        equipmentType = new HashSet<>();
        weaponType = new HashSet<>();
        attrList = new HashSet<>();
        goodAttrList = new HashSet<>();
        badAttrList = new HashSet<>();
        rareAttrList = new HashSet<>();
        levelData = new HashMap<>();
        attrParsingMap = new HashMap<>();

        //SET CONFIG
        Configure config = getAttrDataConfig();
        if(config.getFile().length()==0) {
            config.getConfig().set("bad.type", 0);
            config.getConfig().set("good.type", 1);
            config.getConfig().set("rare.type", 2);
            config.getConfig().set("뾰족함.type", 1);
            config.getConfig().set("뾰족함.parsing.무기", "치명타 확률 증가");
            config.getConfig().set("뾰족함.parsing.방어구", "가시");
            config.saveConfig();
        }
        //레벨당 경험치 설정
        levelData = new HashMap<>();
        levelData.put(1, 1L);
        levelData.put(2, 2L);
        levelData.put(3, 3L);
        levelData.put(4, 4L);
        levelData.put(5, 4L);
        levelData.put(6, 5L);
        levelData.put(7, 5L);
        levelData.put(8, 6L);
        levelData.put(9, 6L);
        levelData.put(10, 7L);
        levelData.put(11, 7L);
        levelData.put(12, 8L);
        levelData.put(13, 8L);
        levelData.put(14, 9L);
        levelData.put(15, 10L);
        levelData.put(16, 12L);
        levelData.put(17, 14L);
        for(int i = 18; i < LevelData.maxLevel; i++) {
            levelData.put(i, 10000L);
        }
        //최소, 최고레벨 필요 경험치
        levelData.put(0, 1L);
        levelData.put(maxLevel, 0L);


        //타입 맵 생성
        TypeData.reloadTypeData();

        weaponType.addAll(TypeData.getType("무기").getChild());
        armorType.addAll(TypeData.getType("방어구").getChild());
        accessoryType.addAll(TypeData.getType("장신구").getChild());

        equipmentType.add("사증");
        equipmentType.addAll(weaponType);
        equipmentType.addAll(armorType);
        equipmentType.addAll(accessoryType);

        //속성 추가
        for(String attr : config.getConfig().getKeys(false)) {
            int type = config.getConfig().getInt(attr+".type");
            registerAttribute(attr, type);
            if(!config.getConfig().contains(attr+".parsing")) {
                attrParsingMap.put(attr, new HashMap<>());
                continue;
            }
            HashMap<String, String> parsingMap = new HashMap<>();
            for(String keyAttr : config.getConfig().getConfigurationSection(attr+".parsing").getKeys(false)) {
                String subAttr = config.getConfig().getString(attr+".parsing."+keyAttr);
                parsingMap.put(keyAttr, subAttr);
            }
            attrParsingMap.put(attr, parsingMap);
        }
    }

    protected static void registerAttribute(String attrName, int type) {
        if(attrList.contains(attrName)) return;
        if(type==0) badAttrList.add(attrName);
        else if(type==1) goodAttrList.add(attrName);
        else rareAttrList.add(attrName);
        attrList.add(attrName);
    }
    public static Set<String> getAttrList() {
        return attrList;
    }

    public static Set<String> getGoodAttrList() {
        return goodAttrList;
    }

    public static Set<String> getBadAttrList() {
        return badAttrList;
    }

    public static Set<String> getRareAttrList() {
        return rareAttrList;
    }

    private static double getQualityMultiplier(double quality) {
        return (1 - QUALITY_MULTIPLIER) + (quality / 100 * QUALITY_MULTIPLIER);
    }


    /**
     *
     * @return 분해시 돌려받는 경험치 무기에 투자한 경험치의 35% ~ 65%
     */
    public int getDisassembleExp() {
        return getDisassembleExp(Rand.randDouble(ItemDisassembleWindow.MIN_DISASSEMBLE_PROPORTION, ItemDisassembleWindow.MAX_DISASSEMBLE_PROPORTION));
    }

    /**
     *
     * @param forcedChance 강제적으로 투자한 경험치 값을 조정
     * @return 분해시 돌려받는 경험치 무기에 투자한 경험치의 forcedChance%
     */
    public int getDisassembleExp(double forcedChance) {
        int first = getCraftLevel() + 1;
        int max = getLevel();
        double exp = 0;
        for(int i = first; i < max; i++) {
            exp += levelData.get(i);
        }
        return (int) Math.ceil((exp * forcedChance));
    }

    //Constructor
    public ItemData(ItemStack item) {
        this(new ItemClass(item));
    }
    public ItemData(ItemClass item) {
        dataMap = new HashMap<>();
        sectionMap = new HashMap<>();
        extraLore = new ArrayList<>();
        this.item = item;
        if(item.getItem().hasItemMeta()) {
            try {
                name = item.getItem().getItemMeta().getDisplayName();
            } catch (NullPointerException e) {
                name = null;
            }
            analyzeAll();
        }
        if(!dataMap.containsKey("level")) setLevel(0);
        if(!dataMap.containsKey("type")) setType("null");
    }

    protected void analyzeAll() {
        for(String lore : item.getLore()) {
            String coloredLore = lore;
            lore = Msg.uncolor(lore);
            if(lore.equals(" ")) continue;

            boolean percent = lore.endsWith("%");
            if(percent) {
                lore = lore.replaceAll("%", "");
            }
            boolean statFound = false;
            for(Stat stat : Stat.values()) {
                if(stat.isUsingPercentage() != percent) continue;
                if(lore.contains(": ") && lore.startsWith(stat.getKorName())) {
                    double value = Double.parseDouble(lore.split(": ")[1]);
                    setStat(stat, value);
                    statFound = true;
                    break;
                }
            }
            if(statFound) continue;

            //Section 0 BaseData
            if(lore.startsWith("레벨: ")) {
                int value;
                if(lore.endsWith("?")) value = 0;
                else value = Integer.parseInt(lore.split(": ")[1].trim());
                setLevel(value);
            }
            else if(lore.startsWith("강화: ")) {
                setEnhanceLevel(EnhanceLevel.getEnhanceLevel(Integer.parseInt(lore.split(": ")[1].replaceFirst("강", ""))));
            }
            else if(lore.startsWith("제작 레벨: ")) {
                int value;
                value = Integer.parseInt(lore.split(": ")[1].trim());
                setCraftLevel(value);
            }
            else if(lore.startsWith("등급: ")) {
                setGrade(ItemGrade.getValueByName(lore.split(": ")[1].trim()));
            }
            else if(lore.startsWith("경험치: ")) {
                double proportion = Double.parseDouble(lore.split(": ")[1].replaceAll("%", "").trim());
                setExpLonged(Math.round(getLongedMaxExpAtLevel(getLevel()) / 100D * proportion));
            }
            else if(lore.startsWith("타입: ")) {
                String value = lore.split(": ")[1].trim();
                setType(value);
            }
            else if(lore.startsWith("가공 가능 횟수: ")) {
                int value = Integer.parseInt(lore.split(": ")[1].trim());
                setProcessTime(value);
            }
            else if(lore.startsWith("유효 시즌: ")) {
                int value = Integer.parseInt(lore.split(": ")[1].trim());
                setSeason(value);
            }
            else if(lore.startsWith("요구 무기 속성: ")) {
                setRequiredWeaponType(lore.split(": ")[1].trim());
            }
            else if(lore.startsWith("지속 시간: ")) {
                setDuration(Integer.parseInt(lore.split(": ")[1].replaceAll("분", "").trim()));
            }
            else if(lore.startsWith("사용 시간: ")) {
                setUsingTime(Double.parseDouble(lore.split(": ")[1].replaceAll("초", "").trim()));
            }
            else if(lore.startsWith("품질: ")) {
                setQuality(Double.parseDouble(removePercent(lore).split(": ")[1].trim()));
            }
            else if(lore.startsWith("수치: ")) {
                setValue(Double.parseDouble(lore.split(": ")[1]));
            }
            else if(lore.startsWith("요구 레벨: ")) {
                setRequiredLevel(Integer.parseInt(lore.split(":")[1].trim()));
            }
            else if (lore.startsWith("공격 속도: ")) {
                setAttackSpeed(lore.split(": ")[1].trim());
            }
            /*
            //Section 5 장비 고유 스텟
            else if(lore.startsWith("공격력: ")) {
                if(lore.endsWith("%")) {
                    double value = Double.parseDouble(removePercent(lore).split(": ")[1]);
                    setAttackDamagePercentage(value);
                }
                else {
                    double value = Double.parseDouble(lore.split(": ")[1]);
                    setAttackDamage(value);
                }

            }
            else if(lore.startsWith("방어력: ")) {
                if(lore.endsWith("%")) {
                    double value = Double.parseDouble(removePercent(lore).split(": ")[1]);
                    setArmorPercentage(value);
                }
                else {
                    double value = Double.parseDouble(lore.split(": ")[1]);
                    setArmor(value);
                }
            }
            else if(lore.startsWith("추가 체력: ")) {
                if(lore.endsWith("%")) {
                    double value = Double.parseDouble(removePercent(lore).split(": ")[1]);
                    setHealthPercentage(value);
                }
                else {
                    double value = Double.parseDouble(lore.split(": ")[1]);
                    setHealth(value);
                }
            }
            else if(lore.startsWith("치명타 확률: ")) {
                double value = Double.parseDouble(lore.split(": ")[1].replaceAll("%", ""));
                setCriticalChance(value);
            }
            else if(lore.startsWith("치명타 피해량: ")) {
                double value = Double.parseDouble(lore.split(": ")[1].replaceAll("%", "").trim());
                setCriticalDamage(value);
            }
            else if(lore.startsWith("행운력: ")) {
                double value = Double.parseDouble(lore.split(": ")[1].trim());
                setLuckLevel(value);
            }
            else if(lore.startsWith("재생력: ")) {
                if(lore.endsWith("%")) {
                    double value = Double.parseDouble(removePercent(lore).split(": ")[1]);
                    setRegenPercentage(value);
                }
                else {
                    double value = Double.parseDouble(lore.split(": ")[1]);
                    setRegen(value);
                }
            }
            else if(lore.startsWith("회피력: ")) {
                double value = Double.parseDouble(lore.split(": ")[1]);
                setEvade(value);
            }
            else if(lore.startsWith("이동속도: ")) {
                double value = Double.parseDouble(lore.split(": ")[1].trim());
                setMovementSpeed(value);
            }
            else if(lore.startsWith("방어 관통력: ")) {
                double value = Double.parseDouble(lore.split(": ")[1].trim());
                setArmorIgnore(value);
            }

             */
            //Section 30 거래불가/퀘스트
            else if(lore.equals("※ 거래 불가")) {
                setUntradable(true);
            }
            else if(lore.equals("※ 가공 불가")) {
                setUnprecessable(true);
            }
            else if(lore.equals("※ 퀘스트 아이템")) {
                setQuestItem(true);
            }
            else if(lore.equals("※ 중요한 물건")) {
                setImportantItem(true);
            }
            else if(lore.equals("※ 분해 불가")||lore.equals("※ 영혼 추출 불가")) {
                setDisassemble(true);
            }

            //Section 20 부가 속성
            //\uE001 단단함 Lv.3
            else if (lore.startsWith("\uE001")||lore.startsWith("\uE002")||lore.startsWith("\uE003")) {
                String[] datas = new String[]{};
                if(lore.startsWith("\uE001")) {
                    datas = lore.replaceAll("\uE001", "").split("Lv.");
                }
                else if(lore.startsWith("\uE002")) {
                    datas = lore.replaceAll("\uE002", "").split("Lv.");
                }
                else if(lore.startsWith("\uE003")) {
                    datas = lore.replaceAll("\uE003", "").split("Lv.");
                }
                String attrName = datas[0].trim();
                int level = Integer.parseInt(datas[1].trim());
                addAttr(attrName, level);
            }

            else {
                extraLore.add(coloredLore);
            }
        }
    }

    public ItemClass getItem() {
        parseAllAttribute();
        if(isEquipment()) {
            if(!hasAttr("grade")) {
                setGrade(ItemGrade.UNKNOWN);
            }
        }
        item.clearLore();
        for(String lore : extraLore) {
            item.addLore(lore);
        }
        if(!extraLore.isEmpty() && !getType().equals("null")) {
            item.addLore(" ");
        }
        ArrayList<Integer> sectionNumber = new ArrayList<>(sectionMap.keySet());
        Collections.sort(sectionNumber);
        for(int section : sectionNumber) {
            if(section!=0) item.addLore(" ");
            List<String> list = getSection(section);
            if(section==0) {
                list = getSortedSectionOne(list);
            }
            for(String key : list) {
                if(section==0) { //enhanceLevel
                    switch (key) {
                        case "level":
                            if (getLevel() > 0) item.addLore("&7레벨: &f" + getLevel());
                            break;
                        case "reqLevel":
                            if (getRequiredLevel() > 0) item.addLore("&7요구 레벨: &f" + getRequiredLevel());
                            break;
                        case "craftLevel":
                            item.addLore("&7제작 레벨: &f" + getCraftLevel());
                            break;
                        case "enhanceLevel":
                            EnhanceLevel enhance = getEnhanceLevel();
                            if (enhance.getLevel() > 0) item.addLore("&7강화: " + enhance.getColorKey() + enhance.getLevel() + "강");
                            break;
                        case "grade":
                            ItemGrade grade = getGrade();
                            item.addLore("&7등급: " + grade.getColoredStr());
                            break;
                        case "exp":
                            item.addLore(String.format("&7경험치: &f%.2f%%", getExpPercentage()));
                            break;
                        case "type":
                            if(!getType().equals("null")) item.addLore("&7타입: &f" + getType());
                            break;
                        case "processTime":
                            item.addLore("&7가공 가능 횟수: &f" + getProcessTime());
                            break;
                        case "season":
                            item.addLore("&7유효 시즌: &f" + getSeason());
                            break;
                        case "requiredWeaponType":
                            item.addLore("&7요구 무기 속성: &f" + getRequiredWeaponType());
                            break;
                        case "duration":
                            item.addLore("&7지속 시간: &f" + getDuration() + "분");
                            break;
                        case "quality":
                            double q = getQuality();
                            if(q >= 100) item.addLore(String.format("&7품질: #e06666%.2f%%", getQuality()));
                            else item.addLore(String.format("&7품질: &f%.2f%%", getQuality()));
                            break;
                        case "usingTime":
                            item.addLore(String.format("&7사용 시간: &f%.2f초", getUsingTime()));
                            break;
                        case "value":
                            double v = getValue();
                            if((v * 100) % 100 == 0) item.addLore(String.format("&7수치: &f%d", (int) getValue()));
                            else item.addLore(String.format("&7수치: &f%.2f", getValue()));
                            break;
                    }
                }
                else if (section==5) {
                    Stat stat = Stat.getStatByCodeName(key);
                    if(stat != null) {
                        if(stat.isUsingPercentage()) item.addLore(String.format("%s%s: %.2f%%", stat.getStatColor(), stat.getKorName(), getStat(stat)));
                        else item.addLore(String.format("%s%s: %.2f", stat.getStatColor(), stat.getKorName(), getStat(stat)));
                    }
                    else {
                        if(key.equals("attackSpeed")) item.addLore(String.format("#f9aa5d공격 속도: %s", getAttackSpeed()));
                    }
                    /*
                    switch (key) {
                        case "attackDamagePercentage" -> item.addLore(String.format("#f09999공격력: %.2f%%", getAttackDamagePercentage()));
                        case "attackDamage" -> item.addLore(String.format("#f09999공격력: %.2f", getAttackDamage()));
                        case "attackSpeed" -> item.addLore(String.format("#f9aa5d공격 속도: %s", getAttackSpeed()));
                        case "armorPercentage" -> item.addLore(String.format("#8accad방어력: %.2f%%", getArmorPercentage()));
                        case "armor" -> item.addLore(String.format("#8accad방어력: %.2f", getArmor()));
                        case "healthPercentage" -> item.addLore(String.format("#f9aa5d추가 체력: %.2f%%", getHealthPercentage()));
                        case "health" -> item.addLore(String.format("#f9aa5d추가 체력: %.2f", getHealth()));
                        case "criticalChance" -> item.addLore(String.format("#fff866치명타 확률: %.2f%%", getCriticalChance()));
                        case "criticalDamage" -> item.addLore(String.format("#ff3333치명타 피해량: %.2f%%", getCriticalDamage()));
                        case "luckLevel" -> item.addLore(String.format("#61fd6d행운력: %.2f", getLuckLevel()));
                        case "regen" -> item.addLore(String.format("#ffc7ec재생력: %.2f", getRegen()));
                        case "regenPercentage" -> item.addLore(String.format("#ffc7ec재생력: %.2f%%", getRegenPercentage()));
                        case "evade" -> item.addLore(String.format("#93FF33회피력: %.2f", getEvade()));
                        case "movementSpeed" -> item.addLore(String.format("#d3f28d이동속도: %.2f", getMovementSpeed()));
                        case "armorIgnore" -> item.addLore(String.format("#b12dd0방어 관통력: %.2f", getArmorIgnore()));
                    }
                     */
                }
                else if (section==20) {
                    if(badAttrList.contains(key)) item.addLore(String.format("&f\uE001&7%s Lv.%d", key, getAttrLevel(key)));
                    else if (goodAttrList.contains(key)) item.addLore(String.format("&f\uE002#88eeff%s Lv.%d", key, getAttrLevel(key)));
                    else if (rareAttrList.contains(key)) item.addLore(String.format("&f\uE003#ffaaaa%s Lv.%d", key, getAttrLevel(key)));
                }
                else if (section==30) {
                    switch (key) {
                        case "questItem" -> item.addLore("&c※ 퀘스트 아이템");
                        case "importantItem" -> item.addLore("&c※ 중요한 물건");
                        case "untradable" -> item.addLore("&c※ 거래 불가");
                        case "unprocessable" -> item.addLore("&c※ 가공 불가");
                        case "disassemble" -> item.addLore("&c※ 영혼 추출 불가");
                    }
                }
            }
        }
        try {
            item.setName(getGrade().getColor() + Msg.uncolor(name));
        } catch (NullPointerException e) {
            item.setName(name);
        }
        item.addFlag(ItemFlag.HIDE_ATTRIBUTES);
        return item;
    }

    @NotNull
    private ArrayList<String> getSortedSectionOne(List<String> list) {
        ArrayList<String> sorted = new ArrayList<>();
        if(list.contains("level")) {
            sorted.add("level");
        }
        if(list.contains("exp")) {
            sorted.add("exp");
        }
        if(list.contains("enhanceLevel")) {
            sorted.add("enhanceLevel");
        }
        if(list.contains("type")) {
            sorted.add("type");
        }
        if(list.contains("grade")) {
            sorted.add("grade");
        }
        if(list.contains("processTime")) {
            sorted.add("processTime");
        }
        if(list.contains("duration")) {
            sorted.add("duration");
        }
        if(list.contains("usingTime")) {
            sorted.add("usingTime");
        }
        if(list.contains("value")) {
            sorted.add("value");
        }
        if(isEquipment()&&!getType().equals("사증")) {
            sorted.add("quality");
        }
        //if(list.contains("season")) sorted.add("season");
        if(list.contains("requiredWeaponType")) {
            sorted.add("requiredWeaponType");
        }
        if(list.contains("craftLevel")) {
            sorted.add("craftLevel");
        }
        if(list.contains("reqLevel")) {
            sorted.add("reqLevel");
        }
        return sorted;
    }

    public ItemStack getItemStack() {
        return getItem().getItem();
    }

    /**
     * 섹션이 존재하지 않는다면 새로운 섹션을 생성
     * @param sectionIndex 추가할 섹션
     */
    protected void createSection(int sectionIndex) {
        if(sectionMap.containsKey(sectionIndex)) return;
        sectionMap.put(sectionIndex, new ArrayList<>());
    }

    /**
     * 섹션을 제거하고 그 안에 있는 데이터까지 모두 삭제
     * @param sectionIndex 제거할 섹션
     */
    protected void removeSection(int sectionIndex) {
        for(String key : sectionMap.get(sectionIndex)) dataMap.remove(key);
        sectionMap.remove(sectionIndex);
    }

    /**
     * 섹션이 비어있다면 해당 섹션을 삭제
     * @param sectionIndex 제거할 섹션
     */
    protected void removeEmptySection(int sectionIndex) {
        if(getSection(sectionIndex).size()==0) {
            removeSection(sectionIndex);
        }
    }

    protected String removePercent(String str) {
        return str.replaceAll("%", "");
    }

    /**
     * 아이템 세이버 데이터 객체를 실체화된 아이템 데이터 객체로 변환
     * @return 변환된 아이템 데이터 객체를 반환
     */
    public ItemData convertSaver() {
        int amount = item.getItem().getAmount();
        ItemData newData = new ItemData(ItemSaver.getItem(Msg.recolor(name)).getItem().clone());
        newData.setAmount(amount);

        //레벨 반영
        newData.setLevel(this.getLevel());

        //부가 속성 반영
        if(sectionMap.containsKey(20)) {
            for(String attr_name : sectionMap.get(20)) {
                newData.addAttr(attr_name, getAttrLevel(attr_name));
            }
        }
        return newData;
    }

    public void setAmount(int amount) {
        item.setAmount(amount);
    }
    public int getAmount() {
        return item.getItem().getAmount();
    }

    /**
     * 부가속성이 없으면 -1000점
     *
     * 부가속성 품질
     * -> 나쁜 속성 1레벨당 -1점
     * -> 좋은 속성 1레벨당 +1점
     * -> 희귀 속성 1레벨당 +1000점
     *
     * @return 아이템의 부가 속성 품질을 반환
     */
    public int getAttrQuality() {
        int q = 0;

        if(getAttrs().size()>0) {
            for(String attr : getAttrs()) {
                if(goodAttrList.contains(attr)) q++;
                else if(badAttrList.contains(attr)) q++;
                else if(rareAttrList.contains(attr)) q += 1000;
            }
        }
        return q;
    }

    /**
     * 아이템의 부가속성을 아이템 속성에 맞게 모두 파싱 후 합침
     */
    public void parseAllAttribute() {
        for(String attr : new ArrayList<>(getAttrs())) {
            String parsedAttr = parseAttribute(attr);
            if(parsedAttr.equals(attr)) continue;
            addAttr(parsedAttr, getAttrLevel(attr));
            setAttr(attr, 0);
        }
    }

    /**
     *
     * @param attr 속성
     * @return 해당 부가속성을 아이템 타입에 맞게 파싱한 새로운 타입의 이름을 반환
     */
    public String parseAttribute(String attr) {
        HashMap<String, String> parsingData = attrParsingMap.get(attr);
        TypeData originalType = TypeData.getType(getType());

        //하위 속성을 더 우선적으로 파싱함
        String newAttr = attr;
        int level = 0;
        for(String key : parsingData.keySet()) {
            TypeData type = TypeData.getType(key);
            if(!originalType.isMaterialOf(type.getName())) continue;
            if(type.getLevel() > level) {
                level = type.getLevel();
                newAttr = parsingData.get(key);
            }
        }
        return newAttr;
    }

    /**
    * 1레벨의 수치를 기준으로 각 레벨에서의 수치가 몇인지를 계산.
    * 레벨 0과 레벨 1은 똑같이 1로 계산한다.
    *
    * @param originLevel 변동 전의 레벨
    * @param newLevel 새롭게 변동될 레벨
    * @param value 현재 수치가 몇인가
    * @param scale 레벨당 몇 %의 수치가 증가하는가 (0과 1사이의 수로 배율을 작성)
    *
    * @return 새로운 레벨에 해당 하는 값을 반환
     */
    protected double setValueByLevelAndQuality(int originLevel, int newLevel, EnhanceLevel enhanceLevel, EnhanceLevel newEnhanceLevel, double originalQuality, double newQuality, double value, double scale) {
        if(originLevel==0) originLevel = 1;
        if(newLevel==0) newLevel = 1;
        double valueAtOne = value / getQualityMultiplier(originalQuality) / (1 + scale * ((originLevel - 1) + enhanceLevel.getMultiply()));
        return (valueAtOne * (1 + scale * ((newLevel - 1) + newEnhanceLevel.getMultiply()))) * getQualityMultiplier(newQuality);
    }


    public long addExp(double exp, boolean qualityProtecting, int levelLimit) {
        return addExpByLong((long) (exp * 1000), qualityProtecting, levelLimit) / 1000L;
    }
    /**
     * 아이템의 레벨을 올림
     * 최고 레벨일 경우 남은 경험치를 반환
     * @param expLonged 1000배율 경험치
     * @param qualityProtecting 이 옵션이 활성화되면 레벨업해도 품질이 감소하지 않음
     * @return 최고 레벨을 달성하고 남은 경험치양을 반환
     */
    private long addExpByLong(long expLonged, boolean qualityProtecting, int levelLimit) {
        if(getLevel() >= Math.min(levelLimit, maxLevel)) {
            long remain = getExpLonged();
            setExpLonged(0);
            return remain;
        }
        setExpLonged(getExpLonged() + expLonged);
        if(getExpLonged() >= getLongedMaxExp()) {
            setExpLonged(getExpLonged() - getLongedMaxExp());
            setLevel(getLevel() + 1);
            if(!qualityProtecting) setQuality(getQuality() - Rand.randDouble(0, 2));
            return addExpByLong(0, qualityProtecting, levelLimit);
        }
        return 0L;
    }

    /**
     * 장비들의 종합적 수치를 담은 맵을 수정
     *
     * @param data 각종 스텟을 담고 있는 맵
     * @return 새로운 정보가 갱신된 맵
     */
    public HashMap<String, Number> mappingEquipmentStatus(HashMap<String, Number> data) {
        for(String key : getSection(5)) {
            Stat stat = Stat.getStatByCodeName(key);
            if(stat == null) continue;
            if(stat.isUsingPercentage()) data.put(key, data.getOrDefault(key, 0).doubleValue() + (getStat(stat) + getAddedStat(stat)) / 100);
            else data.put(key, data.getOrDefault(key, 0).doubleValue() + getStat(stat) + getAddedStat(stat));
        }


        //전체 속성만 영향을 미침
        //방어 관통도 영향을 미침
        for(String attr : getAttrs()) {
            data.putIfAbsent(attr, 0);
            data.put(attr, data.get(attr).intValue() + getAttrLevel(attr));
        }
        return data;
    }


    private void reloadItemStat(int originalLevel, int newLevel, EnhanceLevel enLevel, EnhanceLevel newEnLevel , double quality, double newQuality) {
        double scale = 1;
        if(isRune()) scale = 2.5;

        //레벨에 따라서 각 스텟 리매핑

        for(Stat stat : Stat.values()) {
            if(!hasAttr(stat.getCodeName())) continue;
            if(stat.isScaledOnlyAccessory() && !isAccessory()) continue;
            setStat(stat, setValueByLevelAndQuality(originalLevel, newLevel, enLevel, newEnLevel, quality, newQuality, getStat(stat), stat.getLevelScale() * scale));
        }
    }

    //섹션 없으면 createSection
    public List<String> getSection(int sectionIndex) {
        createSection(sectionIndex);
        return sectionMap.get(sectionIndex);
    }

    public boolean hasKey(String keyName) {
        return dataMap.containsKey(keyName);
    }
    //GETTER AND SETTER
    //Section 0
    public boolean hasAttr(String value) {
        return dataMap.containsKey(value);
    }
    public static boolean isEquipment(String type) {
        return TypeData.getType(type).isMaterialOf("장비");
    }
    public boolean isWeapon() {
        return weaponType.contains(getType());
    }
    public boolean isArmor() {
        return armorType.contains(getType());
    }
    public boolean isAccessory() {
        return accessoryType.contains(getType()) || isRune();
    }
    public boolean isRiding() {
        return TypeData.getType(getType()).isMaterialOf("라이딩") || getType().equals("라이딩");
    }
    public boolean isRune() {
        return getType().equals("룬");
    }
    public boolean isEquipment() {
        return equipmentType.contains(getType());
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 현재 레벨에서의 최대 경험치 (필요 경험치)
     * @return 현재 레벨에서의 최대 경험치 (필요 경험치)
     */
    public double getMaxExp() {
        return (double) getLongedMaxExp() / 1000L;
    }
    /**
     * 현재 레벨에서의 최대 경험치 (필요 경험치)
     * @return 현재 레벨에서의 최대 경험치 (필요 경험치)
     */
    private long getLongedMaxExp() {
        return getLongedMaxExpAtLevel(getLevel());
    }

    public double getValue() {
        return DataClass.toInt(dataMap.get("value"));
    }
    public void setValue(double value) {
        String keyName = "value";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);

    }

    /**
     * @return 현재 경험치의 양을 나타냄
     */
    public double getExp() {
        return (double) getExpLonged() / 1000L;
    }
    /**
     * @return 현재 경험치의 양을 나타냄
     */
    private long getExpLonged() {
        return ((Number) dataMap.getOrDefault("exp", 0)).longValue();
    }
    /**
     * @return 현재 경험치의 바율을 나타냄
     */
    public double getExpPercentage() {
        if(getLevel()==maxLevel) return 0;
        return (double) getExpLonged() / getLongedMaxExpAtLevel(getLevel()) * 100D;
    }
    public void setExp(double value) {
        setExpLonged((long) (value * 1000L));
    }
    private void setExpLonged(long longedValue) {
        String keyName = "exp";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, longedValue);
    }

    /**
     * 레벨에 따라서 장비의 스텟을 다시 결정함
     * 아직 장비의 스텟을 설정하지 않음.
     *
     * @param value 변경할 레벨
     */
    public void setLevel(int value) {
        reloadItemStat(getLevel(), value, getEnhanceLevel(), getEnhanceLevel(), getQuality(), getQuality());
        String keyName = "level";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public int getLevel() {
        return DataClass.toInt(dataMap.getOrDefault("level", 1));
    }

    public void setEnhanceLevel(EnhanceLevel value) {
        reloadItemStat(getLevel(), getLevel(), getEnhanceLevel(), value, getQuality(), getQuality());
        String keyName = "enhanceLevel";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public EnhanceLevel getEnhanceLevel() {
        return (EnhanceLevel) dataMap.getOrDefault("enhanceLevel", EnhanceLevel.ZERO);
    }

    public void setRequiredLevel(int value) {
        String keyName = "reqLevel";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public int getRequiredLevel() {
        return DataClass.toInt(dataMap.getOrDefault("reqLevel", 1));
    }

    public void setCraftLevel(int value) {
        String keyName = "craftLevel";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public int getCraftLevel() {
        return DataClass.toInt(dataMap.getOrDefault("craftLevel", getLevel()));
    }

    public double getQuality() {
        return DataClass.toDouble(dataMap.getOrDefault("quality", 50));
    }
    public void setQuality(double value) {
        String keyName = "quality";
        if(value > 100) value = 100;
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        reloadItemStat(getLevel(), getLevel(), getEnhanceLevel(), getEnhanceLevel(), getQuality(), value);
        dataMap.put(keyName, value);
    }

    public int getSeason() {
        return DataClass.toInt(dataMap.getOrDefault("season", RukonPayment.inst().getPassManager().getSeason()));
    }
    public void setSeason(int value) {
        String keyName = "season";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }

    public int getDuration() {
        return DataClass.toInt(dataMap.getOrDefault("duration", 0));
    }
    public void setDuration(int value) {
        String keyName = "duration";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }

    public double getUsingTime() {
        return DataClass.toInt(dataMap.getOrDefault("usingTime", 0));
    }
    public void setUsingTime(double value) {
        String keyName = "usingTime";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }

    public String getType() {
        if(!dataMap.containsKey("type")) return "";
        return (String) dataMap.get("type");
    }
    public void setType(String value) {
        String keyName = "type";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public int getProcessTime() {
        if(!dataMap.containsKey("processTime")) return -1;
        return DataClass.toInt(dataMap.get("processTime"));
    }
    public void setProcessTime(int value) {
        String keyName = "processTime";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }

    public void setGrade(ItemGrade itemGrade) {
        String keyName = "grade";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, itemGrade.toString());
    }

    public ItemGrade getGrade() {
        try {
            return ItemGrade.valueOf((String) dataMap.get("grade"));
        } catch (IllegalArgumentException e) {
            return ItemGrade.UNKNOWN;
        } catch (Exception e) {
            return null;
        }
    }
    public String getRequiredWeaponType() {
        return (String) dataMap.get("requiredWeaponType");
    }
    public void setRequiredWeaponType(String value) {
        String keyName = "requiredWeaponType";
        int section = 0;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }

    //Section 5
    public void setStat(Stat stat, double value) {
        List<String> section = getSection(5);
        if(value == 0) {
            dataMap.remove(stat.getCodeName());
            section.remove(stat.getCodeName());
        }
        else {
            if(!section.contains(stat.getCodeName())) {
                section.add(stat.getCodeName());
            }
            dataMap.put(stat.getCodeName(), value);
        }

    }
    public double getStat(Stat stat) {
        return ((Number) dataMap.getOrDefault(stat.getCodeName(), 0)).doubleValue();
    }

    public double getAddedStat(Stat stat) {
        return DataClass.toDouble(getStat(stat) * ((getAttrLevel(stat.getKorName() + " 증가")-getAttrLevel(stat.getKorName() + " 감소"))/10.0));
    }

    public String getAttackSpeed() {
        return (String) dataMap.get("attackSpeed");
    }
    public void setAttackSpeed(String value) {
        String keyName = "attackSpeed";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }

    /*
    public double getAttackDamage() {
        return DataClass.toDouble(dataMap.get("attackDamage"));
    }
    public void setAttackDamage(double value) {
        String keyName = "attackDamage";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getAttackDamagePercentage() {
        return DataClass.toDouble(dataMap.get("attackDamagePercentage"));
    }
    public void setAttackDamagePercentage(double value) {
        String keyName = "attackDamagePercentage";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }

    public double getArmor() {
        return DataClass.toDouble(dataMap.get("armor"));
    }
    public void setArmor(double value) {
        String keyName = "armor";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getEvade() {
        return DataClass.toDouble(dataMap.get("evade"));
    }
    public void setEvade(double value) {
        String keyName = "evade";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getArmorPercentage() {
        return DataClass.toDouble(dataMap.get("armorPercentage"));
    }
    public void setArmorPercentage(double value) {
        String keyName = "armorPercentage";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getHealth() {
        return DataClass.toDouble(dataMap.get("health"));
    }
    public void setRegen(double value) {
        String keyName = "regen";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getRegen() {
        return DataClass.toDouble(dataMap.get("regen"));
    }
    public void setRegenPercentage(double value) {
        String keyName = "regenPercentage";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getRegenPercentage() {
        return DataClass.toDouble(dataMap.get("regenPercentage"));
    }
    public void setHealth(double value) {
        String keyName = "health";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getHealthPercentage() {
        return DataClass.toDouble(dataMap.get("healthPercentage"));
    }
    public void setHealthPercentage(double value) {
        String keyName = "healthPercentage";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getCriticalChance() {
        return DataClass.toDouble(dataMap.get("criticalChance"));
    }
    public void setCriticalChance(double value) {
        String keyName = "criticalChance";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getCriticalDamage() {
        return DataClass.toDouble(dataMap.get("criticalDamage"));
    }
    public void setLuckLevel(double value) {
        String keyName = "luckLevel";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getLuckLevel() {
        return DataClass.toDouble(dataMap.get("luckLevel"));
    }
    public void setCriticalDamage(double value) {
        String keyName = "criticalDamage";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public String getAttackSpeed() {
        return (String) dataMap.get("attackSpeed");
    }
    public void setAttackSpeed(String value) {
        String keyName = "attackSpeed";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getMovementSpeed() {
        return DataClass.toDouble(dataMap.get("movementSpeed"));
    }
    public void setMovementSpeed(double value) {
        String keyName = "movementSpeed";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }
    public double getArmorIgnore() {
        return DataClass.toDouble(dataMap.get("armorIgnore"));
    }
    public void setArmorIgnore(double value) {
        String keyName = "armorIgnore";
        int section = 5;
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, value);
    }

    //REAL 은 속성으로 인해 추가된 값
    public double getAddAttackDamage() {
        return DataClass.toDouble(dataMap.get("attackDamage")) * ((getAttrLevel("공격력 증가")-getAttrLevel("공격력 감소"))/10.0);
    }
    public double getAddAttackDamagePercentage() {
        return DataClass.toDouble(dataMap.get("attackDamagePercentage")) * ((getAttrLevel("공격력 증가")-getAttrLevel("공격력 감소"))/10.0);
    }
    public double getAddRegen() {
        return DataClass.toDouble(dataMap.get("regen")) * ((getAttrLevel("재생력 증가")-getAttrLevel("재생력 감소"))/10.0);
    }
    public double getAddRegenPercentage() {
        return DataClass.toDouble(dataMap.get("regenPercentage")) * ((getAttrLevel("재생력 증가")-getAttrLevel("재생력 감소"))/10.0);
    }
    public double getAddArmor() {
        return DataClass.toDouble(dataMap.get("armor")) * ((getAttrLevel("방어력 증가")-getAttrLevel("방어력 감소"))/10.0);
    }
    public double getAddArmorPercentage() {
        return DataClass.toDouble(dataMap.get("armorPercentage")) * ((getAttrLevel("방어력 증가")-getAttrLevel("방어력 감소"))/10.0);
    }
    public double getAddHealth() {
        return DataClass.toDouble(dataMap.get("health")) * ((getAttrLevel("체력 증가")-getAttrLevel("체력 감소"))/10.0);
    }
    public double getAddHealthPercentage() {
        return DataClass.toDouble(dataMap.get("healthPercentage")) * ((getAttrLevel("체력 증가")-getAttrLevel("체력 감소"))/10.0);
    }
    public double getAddCriticalChance() {
        return DataClass.toDouble(dataMap.get("criticalChance")) * ((getAttrLevel("치명타 확률 증가")-getAttrLevel("치명타 확률 감소"))/10.0);
    }
    public double getAddCriticalDamage() {
        return DataClass.toDouble(dataMap.get("criticalDamage")) * ((getAttrLevel("치명타 피해량 증가")-getAttrLevel("치명타 피해량 감소"))/10.0);
    }
    public double getAddMovementSpeed() {
        return DataClass.toDouble(dataMap.get("movementSpeed")) * ((getAttrLevel("이동속도 증가")-getAttrLevel("이동속도 감소"))/10.0);
    }
    public double getAddLuckLevel() {
        return DataClass.toDouble(dataMap.get("luckLevel")) * ((getAttrLevel("행운력 증가")-getAttrLevel("행운력 감소"))/10.0);
    }
    */

    //Section 20 부가 속성

    /**
     * 부가 속성의 레벨을 변화 (0이 되면 삭제, 0이였다가 변하면 해당 데이터를 새롭게 추가
     *
     * @param attrName 부가 속성의 레벨
     * @param level 얼마나 레벨을 더하거나 뺼지 (레벨이 0이 되면 해당 속성은 자동으로 삭제)
     */
    public void addAttr(String attrName, int level) {
        int section = 20;
        if(!dataMap.containsKey(attrName)) {
            if(!getSection(section).contains(attrName)) getSection(section).add(attrName);
        }
        int newLevel = getAttrLevel(attrName) + level;
        if(newLevel==0) {
            dataMap.remove(attrName);
            getSection(section).remove(attrName);
            removeEmptySection(section);
            return;
        }
        dataMap.put(attrName, newLevel);
    }

    /**
     *
     * @param attrName 해당 속성을 해당 레벨로 설정 0으로 설정시 속성 삭제
     * @param level 레벨
     */
    public void setAttr(String attrName, int level) {
        int section = 20;
        if(!dataMap.containsKey(attrName)) {
            if(!getSection(section).contains(attrName)) getSection(section).add(attrName);
        }
        if(level==0) {
            dataMap.remove(attrName);
            getSection(section).remove(attrName);
            removeEmptySection(section);
            return;
        }
        dataMap.put(attrName, level);
    }

    /**
     * 아이템 데이터에 있는 부가 속성의 레벨을 반환
     *
     * @param attrName 부가 속성의 이름
     * @return 해당 부가 속성의 레벨을 반환, 속성이 존재하지 않는다면 0을 반환
     */
    public int getAttrLevel(String attrName) {
        if(!dataMap.containsKey(attrName)) return 0;
        else return ((Number) dataMap.get(attrName)).intValue();
    }

    /**
     * @return 해당 아이템이 가지고 있는 부가 속성을 반환
     */
    public List<String> getAttrs() {
        int section = 20;
        if(!sectionMap.containsKey(section)) return new ArrayList<>();
        return getSection(section);
    }

    //Section 30
    public boolean isImportantItem() {
        return dataMap.containsKey("importantItem");
    }
    public boolean isQuestItem() {
        return dataMap.containsKey("questItem") || dataMap.containsKey("importantItem");
    }

    public void setQuestItem(boolean value) {
        String keyName = "questItem";
        int section = 30;
        if(!value) {
            dataMap.remove(keyName);
            getSection(section).remove(keyName);
            removeEmptySection(section);
            return;
        }
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, true);
    }
    public void setImportantItem(boolean value) {
        String keyName = "importantItem";
        int section = 30;
        if(!value) {
            dataMap.remove(keyName);
            getSection(section).remove(keyName);
            removeEmptySection(section);
            return;
        }
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, true);
    }

    public boolean isUntradable() {
        return dataMap.containsKey("untradable");
    }
    public void setUntradable(boolean value) {
        String keyName = "untradable";
        int section = 30;
        if(!value) {
            dataMap.remove(keyName);
            getSection(section).remove(keyName);
            removeEmptySection(section);
            return;
        }
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, true);
    }
    public boolean isUnprecessable() {
        return dataMap.containsKey("unprocessable");
    }
    public void setUnprecessable(boolean value) {
        String keyName = "unprocessable";
        int section = 30;
        if(!value) {
            dataMap.remove(keyName);
            getSection(section).remove(keyName);
            removeEmptySection(section);
            return;
        }
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, true);
    }

    public boolean isDisassemble() {
        return dataMap.containsKey("disassemble");
    }
    public void setDisassemble(boolean value) {
        String keyName = "disassemble";
        int section = 30;
        if(!value) {
            dataMap.remove(keyName);
            getSection(section).remove(keyName);
            removeEmptySection(section);
            return;
        }
        if(!getSection(section).contains(keyName)) getSection(section).add(keyName);
        dataMap.put(keyName, true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append("{").append("Lv.").append(getLevel());
        if(!getAttrs().isEmpty()) {
            for(String attr : getAttrs()) {
                sb.append(", ");
                sb.append(attr).append(getAttrLevel(attr));
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
