package me.rukon0621.guardians.craft.recipes;

import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecipeManager {
    public static Map <String, Recipe> recipesData; //각 이름에 따른 레시피
    private static final main plugin = main.getPlugin();
    public static Map<Player, Recipe> playerCrafting; //현재 플레이어가 제작하는 레시피

    //레시피 configure 전체를 리로드
    public static void reloadRecipes() {;
        recipesData = new HashMap<>();
        playerCrafting = new HashMap<>();
        Configure pathConfig = getPathData();
        File file = new File(FileUtil.getOuterPluginFolder()+"/recipeData/recipes");
        for(String key : pathConfig.getConfig().getKeys(false)) {
            try {
                recipesData.put(key, new Recipe(getConfig(key), key));
            } catch (Exception e) {
                plugin.getLogger().warning(key + " - 레시피를 읽어들이지 못했습니다.");
            }
        }
        file.mkdir();
    }

    //레시피의 이름을 통해 해당 레시피 객체를 받음
    public static Recipe getRecipes(String name) {
        return recipesData.get(name);
    }

    //새로운 레시피 생성 이미 존재하는 레시피면 false 반환
    public static boolean createNewRecipes(String fileData) {
        String name;
        if(fileData.contains("/")) {
            name = fileData.split("/")[fileData.split("/").length-1];
        }
        else {
            name = fileData;
        }
        if(!fileData.endsWith(".yml")) fileData += ".yml";

        if(getPathData().getConfig().contains(name)) return false;
        Configure config = new Configure(FileUtil.getOuterPluginFolder()+"/recipeData/recipes/"+fileData);
        config.getConfig().set("mainMaterial", new ArrayList<>());
        config.getConfig().set("subMaterial", new ArrayList<>());
        config.getConfig().set("craftingTime", 0);
        config.getConfig().set("precessTime", 0);
        config.getConfig().set("cost", 0);
        config.getConfig().set("maxResultLevel", -1);
        config.getConfig().set("requiredSkills", new ArrayList<>());
        config.getConfig().set("blueprints", new ArrayList<>());
        config.getConfig().set("specialOptions", new ArrayList<>());
        config.getConfig().set("explanation", new ArrayList<>());
        config.getConfig().set("result.itemDataName", "null");
        config.getConfig().set("result.addingAttributes", new ArrayList<>());
        config.saveConfig();
        config = getPathData();
        config.getConfig().set(name, fileData);
        config.saveConfig();
        return true;
    }

    //Recipe PathData configure을 받음
    public static Configure getPathData() {
        return new Configure("path.yml", FileUtil.getOuterPluginFolder()+"/recipeData");
    }

    //해당 레시피의 configure을 받음
    public static Configure getConfig(String name) {
        Configure config = getPathData();
        return new Configure(FileUtil.getOuterPluginFolder()+"/recipeData/recipes/"+config.getConfig().getString(name));
    }

    //기존 레시피 삭제 존재하지 않는 레시피면 false 반환
    public static boolean deleteRecipes(String name) {
        if(!getPathData().getConfig().contains(name)) return false;
        Configure config = getConfig(name);
        config.delete("recipes");
        config = getPathData();
        config.getConfig().set(name, null);
        config.saveConfig();
        return true;
    }

    //인벤토리에 레시피를 그려줌
    public static InvClass showRecipe(Player player, InvClass inv) {
        Recipe recipe = playerCrafting.get(player);
        //9,10,11,18,19,20,15,16,17,24,25,26,13
        int[] slots = {9,10,11,18,19,20};
        int index = 0;
        for(String name : recipe.getMainMater()) {
            inv.setslot(slots[index], recipe.getParsedItem(name));
            index++;
        }
        slots = new int[]{15,16,17,24,25,26};
        index = 0;
        for(String name : recipe.getSubMater()) {
            inv.setslot(slots[index], recipe.getParsedItem(name));
            index++;
        }
        inv.setslot(40, recipe.getResult().getItem());
        return inv;
    }


}
