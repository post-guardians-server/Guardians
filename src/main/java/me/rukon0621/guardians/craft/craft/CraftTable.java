package me.rukon0621.guardians.craft.craft;

import me.rukon0621.guardians.craft.recipes.Recipe;
import me.rukon0621.guardians.craft.recipes.RecipeManager;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class CraftTable {

    private ArrayList<String> craftList;
    private int line;
    private static final main plugin = main.getPlugin();
    private final String name;

    public CraftTable(Configure config, String name) {
        craftList = (ArrayList<String>) config.getConfig().getList("recipes");
        line = config.getConfig().getInt("lines");
        this.name = name;
    }

    public void openCraftTableGUI(Player player, int yLoc) {
        InvClass inv = new InvClass(6, "&f\uF000\uF013");
        ItemClass it;
        PlayerData pdc = new PlayerData(player);

        int max = yLoc*8+40;
        if(craftList.size()<max) max = craftList.size();
        int slot = 0;
        for(int id = yLoc*8 ; id < max ; id++) {
            try {
                inv.setslot(slot, RecipeManager.getRecipes(craftList.get(id)).getIcon(player));
            } catch (Exception e) {
                Msg.warn(player, "&c레시피를 로드하는 중 오류가 발생했습니다.", "&7[ &e" + craftList.get(id) + "&7 ] ");
                e.printStackTrace();
                continue;
            }
            slot++;
            if((slot+1)%9==0) slot++;
        }
        //Scroll
        it = new ItemClass(new ItemStack(Material.SCUTE), "&7");
        it.setCustomModelData(7);
        it.setName("&e『 &6위로 &e』");
        inv.setslot(26, it.getItem());

        it.setCustomModelData(7);
        it.setName("&e『 &6아래로 &e』");
        inv.setslot(35, it.getItem());


        //제작 대기열
        ArrayList<WaitingItem> waitingItems = pdc.getWaitingItems();
        it = new ItemClass(new ItemStack(Material.BARRIER), "&c제작 대기열 비활성화");
        it.addLore("&7더 높은 제작 스킬을 활성화시켜");
        it.addLore("&7더 많은 제작 대기열을 해금하세요!");
        int line = pdc.getCraftLineSize(false);
        for(int i = 0 ; i < 8 ; i++) {
            if(waitingItems.size()==i) break;
            inv.setslot(46+i, waitingItems.get(i).getIcon());
        }
        for(int i = line ; i < 8 ; i++) {
            if(inv.getInv().getItem(46+i)==null) {
                inv.setslot(46+i, it.getItem());
            }
        }

        it = new ItemClass(new ItemStack(Material.SCUTE), "&7");
        it.setCustomModelData(7);
        it.setName("&c\uE004\uE00C\uE00C제작 대기열");
        it.addLore("&e즉시 제작되는 아이템이 아닌 경우 이곳에서 제작됩니다.");
        inv.setslot(45, it.getItem());
        CraftManager.playerCraftingTable.put(player, this);
        player.openInventory(inv.getInv());
    }

    public int getSize() {
        return craftList.size();
    }

    public int getHeight() {
        return (getSize()+1)/8 + 1;
    }

    public Recipe getRecipe(int index) {
        return RecipeManager.getRecipes(craftList.get(index));
    }

    public int getLine() {
        return line;
    }

    public String getCraftingSlot(int id) {
        return craftList.get(id);
    }
}
