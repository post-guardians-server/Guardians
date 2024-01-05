package me.rukon0621.guardians.data;

import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TypeData {
    private static final main plugin = main.getPlugin();

    private static final Map<String, TypeData> typeMap = new HashMap<>();

    private static Configure getConfig() {
        return new Configure("typeData.yml", FileUtil.getOuterPluginFolder().getPath());
    }
    public static Map<String, TypeData> getTypeMap() {
        return typeMap;
    }

    public static String getWeaponType(Player player) {
        if(EquipmentManager.getWeapon(player).getType().equals(Material.AIR)) return "장착되지 않음";
        else {
            String type = new ItemData(EquipmentManager.getWeapon(player)).getType();
            if(TypeData.getType(type).isMaterialOf("검")) return "검";
            else if(TypeData.getType(type).isMaterialOf("창")) return "창";
            else if(TypeData.getType(type).isMaterialOf("둔기")) return "둔기";
            else if(TypeData.getType(type).isMaterialOf("활")) return "활";
            else if(TypeData.getType(type).isMaterialOf("투척")) return "투척";
            else if(TypeData.getType(type).isMaterialOf("폭탄")) return "폭탄";
        }
        return "알 수 없음";
    }

    public static Set<String> getTypeNames() {
        return typeMap.keySet();
    }

    public static TypeData getType(String typeName) {
        if(typeName==null) typeName = "null";
        return typeMap.get(typeName);
    }

    public static void reloadTypeData() {
        typeMap.clear();
        Configure config = getConfig();
        Stack<String> stack = new Stack<>();

        for(String key : config.getConfig().getKeys(false)) {
            if(key.equals("customModelData")) continue;
            stack.add(key);
            new TypeData(key, 1);
        }
        while(stack.size()>0) {
            String key = stack.pop();
            int level = 1;
            for(char c : key.toCharArray()) {
                if(c=='.') level++;
            }
            new TypeData(key, level); //새로운 속성 생성
            if(config.getConfig().isConfigurationSection(key)) {
                for (String innerKey : config.getConfig().getConfigurationSection(key).getKeys(false)) {
                    stack.add(key + "." + innerKey);
                }
            }
        }
        System.out.println("아이템 타입 생성 완료");

        //아이템 세이버 생성
        for(TypeData type : getTypeMap().values()) {
            ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&f" + type.getName() + " 타입의 아이템");
            if(!config.getConfig().contains("customModelData."+type.getName())) {
                config.getConfig().set("customModelData."+type.getName(), 10000);
            }
            item.setCustomModelData(config.getConfig().getInt("customModelData."+type.getName()));

            item.addLore("&7이 타입의 아이템 또는 하위 타입의 아이템을 포함합니다.");

            if(type.getChild().size()>0) {
                item.addLore(" ");
                item.addLore("&7『 이 타입의 하위 타입 』");
                for(String childName : type.getChild()) {
                    TypeData child = typeMap.get(childName);
                    if(child.getLevel() - 1 != type.getLevel()) continue;
                    item.addLore(" &7- " + childName);
                }
            }
            ItemSaver.addNewItemSaver("타입:"+type.getName(), item.getItem());
        }
        System.out.println("아이템 타입 세이버 생성 완료");

        if(main.isDevServer()) {
            for(String key : config.getConfig().getConfigurationSection("customModelData").getKeys(false)) {
                if(typeMap.containsKey(key)) continue;
                config.getConfig().set("customModelData."+key, null);
                ItemSaver.removeItemSaver("타입:" + key);
                System.out.println("유효하지 않은 아이템 타입이 삭제됨 - " + key);
            }
            config.saveConfig();
            ItemSaver.saveAllFromCache();
        }
        else System.out.println("Dev가 아니기에 타입 세이버 자동 생성이 비활성화됨.");

    }

    private final String name;
    private final Set<String> parents;
    private final Set<String> child;
    private final int level;

    public TypeData(String key, int level) {
        this.level = level;
        parents = new HashSet<>();
        child = new HashSet<>();
        String[] keyData = key.split("\\.");
        name = keyData[keyData.length - 1].trim();
        //부모 타입 객체 생성
        for(int i = 0; i < keyData.length - 1; i++) {
            parents.add(keyData[i]);
            typeMap.get(keyData[i]).addChild(name); //부모 객체의 자식 객체
        }
        typeMap.put(name, this);
    }

    /**
     * ex) 예를 들어 재료에서 '무기'속성이 필요하다면,
     * 무기 속성 자체를 포함한 하위 속성까지 재료로 쓰일 수 있음.
     * @param type 속성
     * @return 해당 속성이 제작 속성으로 쓰일 수 있는지를 반환 (해당 속성이 이 객체의 타입의 하위 속성인지 또는 같은 속성인지 반환)
     */
    public boolean isMaterialOf(String type) {
        return type.equals(name) || parents.contains(type);
    }
    public void addChild(String type) {
        child.add(type);
    }
    public String getName() {
        return name;
    }
    public int getLevel() {
        return level;
    }
    public Set<String> getParents() {
        return parents;
    }
    public Set<String> getChild() {
        return child;
    }
}
