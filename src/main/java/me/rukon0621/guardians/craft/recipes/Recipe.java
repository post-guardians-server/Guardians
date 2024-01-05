package me.rukon0621.guardians.craft.recipes;

import com.craftmend.thirdparty.iolettuce.core.ScriptOutputType;
import me.rukon0621.guardians.blueprint.BluePrintManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*


레시피 specialOptions

overTime
 - 주재료를 유효 기간이 지난 아이템으로 채워야한다.

onlyChangeAttributes
 - 주재료는 반드시 1개만 사용한다.
 - 주재료의 모든 옵션이 그대로 나오고 특정 옵션이 추가되기만 한다.
 - addingAttributes에 추가될 옵션을 작성한다
    - ex) 방어력 증가 ; 3
    - 위와 같이 작성하면 방어력 증가 속성이 3추가된다.
 - ex) 듀랑고의 가죽 무두질

processing
 - 가공처리 작업으로 가공 횟수를 차감한다.
 - 결과물의 가공처리 횟수 값은 주재료의 가공처리 횟수의 평균 - 1이 된다.

 */

public class Recipe {
    private final List<String> mainMater; //주재료의 itemdata 이름
    private final List<String> subMater; //부재료의 ''
    private final List<String> materials; //모든 재료
    private final long craftingTime;
    private final int precessTime; //가공 횟수
    private final int cost; //가공 횟수
    private final Couple<String, Integer> result;
    private final String recipeName;
    private final String replacer;
    private final String replacerWithPat;
    private final List<String> lores;
    private final Map<String, Integer> addingAttr;
    private final ArrayList<String> requiredSkills;
    private final ArrayList<String> specialOptions;
    private final ArrayList<String> blueprints;
    private final boolean useRecipeFileName;
    private final String anotherName;
    private final int maxResultLevel;
    private static final int[] mainSlots = new int[]{9,10,11,18,19,20};
    private static final int[] subSlots = new int[]{15,16,17,24,25,26};

    //레시피 객체
    public Recipe(Configure config, String recipeName) {
        this.recipeName = recipeName;
        mainMater = config.getConfig().getStringList("mainMaterial");
        subMater = config.getConfig().getStringList("subMaterial");
        materials = new ArrayList<>(mainMater);
        materials.addAll(subMater);
        craftingTime = config.getConfig().getLong("craftingTime", 0);
        precessTime = config.getConfig().getInt("precessTime", 0);
        cost = config.getConfig().getInt("cost", 0);
        requiredSkills = (ArrayList<String>) config.getConfig().getList("requiredSkills", new ArrayList<>());
        specialOptions = (ArrayList<String>) config.getConfig().getList("specialOptions", new ArrayList<>());
        blueprints = (ArrayList<String>) config.getConfig().getList("blueprints", new ArrayList<>());
        useRecipeFileName = config.getConfig().getBoolean("useRecipeFileName", false);
        anotherName = config.getConfig().getString("useAnotherName", null);
        maxResultLevel = config.getConfig().getInt("maxResultLevel", -1);
        replacer = config.getConfig().getString("replacer", null);

        if(replacer == null) replacerWithPat = null;
        else replacerWithPat = "(" + replacer + ") ";

        String s = config.getConfig().getString("result.itemDataName", "슬라임의 점액");
        if(s.contains(";")) {
            result = new Couple<>(s.split(";")[0].trim(), Integer.parseInt(s.split(";")[1].trim()));
        } else {
            result = new Couple<>(s.split(";")[0].trim(), 1);
        }
        lores = config.getConfig().getStringList("explanation");

        List<String> addingAttrList = config.getConfig().getStringList("result.addingAttributes");

        //단단함:3 -> 단단함 3레벨이 추가
        addingAttr = new HashMap<>();
        for(String line : addingAttrList) {
            String[] data = line.split(":");
            addingAttr.put(data[0].trim(), Integer.valueOf(data[1].trim()));
        }
    }

    /**
     * @param putItem 넣을 아이템
     * @param player player
     * @param inv 현재 인벤토리
     * @return 아이템을 넣어야할 슬롯
     */
    public int put(ItemStack putItem, Player player, Inventory inv) {
        String target = null; //현재 넣어야하는 아이템

        int index = 0, slot = -1;
        for(String s : mainMater) {
            if(inv.getItem(mainSlots[index])==null) {
                target = s;
                slot = mainSlots[index];
                break;
            }
            index++;
        }
        if(target==null) {
            index = 0;
            for(String s : subMater) {
                if(inv.getItem(subSlots[index])==null) {
                    target = s;
                    slot = subSlots[index];
                    break;
                }
                index++;
            }
            if(target==null) {
                Msg.warn(player, "이미 모든 아이템을 다 채웠습니다.");
                return -1;
            }
        }
        ItemData itemData = new ItemData(putItem);
        if(target.startsWith("타입:")) {
            String requiredType = target.replaceAll("타입:", "").trim();
            if(!TypeData.getType(itemData.getType()).isMaterialOf(requiredType)) {
                Msg.warn(player, String.format("지금 넣어야할 아이템은 &e%s 타입&c의 아이템입니다.", requiredType));
                return -1;
            }
            return slot;
        }
        else {
            String name = itemData.getName();
            if(replacer != null && target.startsWith(replacerWithPat)) {
                target = target.split("\\)")[1].trim();
                name = name.replaceAll(replacer + " ", "");
            }
            if(Msg.uncolor(name).equals(Msg.uncolor(ItemSaver.getItem(target).getItem().getItemMeta().getDisplayName()))) {
                return slot;
            }
            else {
                Msg.warn(player, String.format("지금 넣어야할 아이템은 &e%s&c입니다.", target));
                return -1;
            }
        }
    }

    public boolean isFilled(Inventory inv) {
        String target = null; //현재 넣어야하는 아이템

        int index = 0;
        for(String s : mainMater) {
            if(inv.getItem(mainSlots[index])==null) {
                target = s;
                break;
            }
            index++;
        }
        if(target==null) {
            index = 0;
            for(String s : subMater) {
                if(inv.getItem(subSlots[index])==null) {
                    target = s;
                    break;
                }
                index++;
            }
        }
        return target==null;
    }

    public ItemClass getResult() {
        ItemClass item = ItemSaver.getItem(result.getFirst());
        item.setAmount(result.getSecond());
        return item;
    }

    public int getCost() {
        return cost;
    }

    public long getCraftingTime() {
        return craftingTime;
    }

    public int getPrecessTime() {
        return precessTime;
    }

    public ArrayList<String> getBlueprints() {
        return blueprints;
    }

    public Map<String, Integer> getAddingAttr() {
        return addingAttr;
    }

    //해당 레시피의 주재료의 목록을 반환
    //ItemSaver에 쓰이는 문자열로 반환
    public List<String> getMainMater() {
        return mainMater;
    }

    //해당 레시피의 부재료 목록을 반환
    //ItemSaver에 쓰이는 문자열로 반환
    public List<String> getSubMater() {
        return subMater;
    }

    public ArrayList<String> getRequiredSkills() {
        return requiredSkills;
    }

    public ArrayList<String> getSpecialOptions() {
        return specialOptions;
    }
    public String getRecipeName() {
        return recipeName;
    }

    //외부 제작대에 띄울 아이템을 반환 (제작 남은 시간등을 표기함)
    public ItemStack getIcon(Player player) {
        PlayerData pdc = new PlayerData(player);
        boolean canCraft = true;
        ItemClass it = new ItemClass(new ItemStack(Material.BARRIER), "&f"+recipeName);
        List<String> required = new ArrayList<>();

        BluePrintManager manager = main.getPlugin().getBluePrintManager();
        for(String blueprint : getBlueprints()) {
            if(!pdc.hasBlueprint(blueprint)) {
                canCraft = false;
                required.add(blueprint);
            }
        }
        if(!canCraft) {
            it.addLore("&9이 아이템을 제작하기 위한 청사진이 필요합니다.");
            it.addLore(" ");
            it.addLore("&f『 현재 필요한 청사진 』");
            for(String bp : required) {
                if(manager.getConsumableBlueprintData().containsKey(bp)) it.addLore("&7 - " + bp + " 『일회성』");
                else it.addLore("&7 - " + bp);
            }
            return it.getItem();
        }

        for(String skill : getRequiredSkills()) {
            if(!pdc.hasSkill(skill)) {
                canCraft = false;
                required.add(skill);
            }
        }
        if(!canCraft) {
            it.addLore("&c이 아이템을 제작하기 위한 스킬 트리를 모두 배워야합니다.");
            it.addLore(" ");
            it.addLore("&f『 현재 배워야하는 스킬 』");
            for(String skill : required) {
                it.addLore("&7 - " + skill);
            }
            return it.getItem();
        }

        it = getResult();
        if(useRecipeFileName) {
            it.setName("&e" + recipeName);
            it.clearLore();
        }
        else if (anotherName!=null) {
            it.setName(anotherName);
            it.clearLore();
        }
        else {
            it.addLore(" ");
        }
        if(!lores.isEmpty()) {
            for(String s : lores) {
                it.addLore(s);
            }
        }

        boolean specialOptionLore = false;
        for(String specialOption : getSpecialOptions()) {
            if(specialOption.toLowerCase().startsWith("addprocesstime")) {
                int processTime = Integer.parseInt(specialOption.split(":")[1].trim());
                it.addLore("&7- 가공 횟수 " + processTime + "회 추가");
                specialOptionLore = true;
            }
            else if(specialOption.equalsIgnoreCase("removeProcessTime")) {
                it.addLore("&7- 가공 횟수 1회 소모");
                specialOptionLore = true;
            }
        }
        if(specialOptionLore) it.addLore(" ");


        if(craftingTime==0) it.addLore("&e\uE004\uE00C\uE00C제작시간: "+"&6즉시 제작");
        else {
            it.addLore("&e\uE004\uE00C\uE00C제작시간: &6"+ DateUtil.formatDate(craftingTime));
        }

        if(maxResultLevel!=-1) it.addLore("&6\uE011\uE00C\uE00C제작 결과물 최대레벨: " + maxResultLevel + "레벨");

        it.addLore("&a\uE015\uE00C\uE00C기본 제작 비용: "+cost);
        it.addLore(" ");
        it.addLore("&c\uE006\uE00C\uE00C우클릭하여 조합 방법을 확인합니다.");

        return it.getItem();
    }

    public int getMaxResultLevel() {
        return maxResultLevel;
    }

    public ItemStack getParsedItem(String s) {
        if(replacer == null) return ItemSaver.getItem(s).getItem();
        if(!s.startsWith(replacerWithPat)) return ItemSaver.getItem(s).getItem();
        ItemClass item = new ItemClass(ItemSaver.getItem(s.split("\\)")[1].trim()).getItem().clone());
        item.setName("&f" + s);
        return item.getItem();
    }
}
