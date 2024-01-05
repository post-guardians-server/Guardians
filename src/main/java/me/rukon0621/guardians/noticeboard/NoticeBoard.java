package me.rukon0621.guardians.noticeboard;

import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SerializableAs("customNoticeBoard")
public class NoticeBoard implements ConfigurationSerializable {

    private final String name; //게시판의 이름
    private Map<Integer, ItemStack> items; //각 슬롯에 저장된 아이템
    private ArrayList<Block> registered; //현재 이 게시판이 등록된 블록을 목록

    public NoticeBoard(String name, Map<Integer, ItemStack> items, ArrayList<Location> locations) {
        this.items = items;
        this.name = name;
        ArrayList<Block> registered = new ArrayList<>();
        for(Location location : locations) {
            registered.add(location.getBlock());
        }
        this.registered = registered;
    }

    //해당 플레이어에게 게시판 창을 띄움
    public void openBoard(Player player) {
        InvClass inv = new InvClass(5, NoticeBoardManager.getGuiName());
        for(int slot : items.keySet()) {
            ItemClass i = new ItemClass(items.get(slot).clone());
            i.addLore(" ");
            i.addLore("&e\uE011\uE00C\uE00C클릭하여 좌표를 추적할 수 있습니다.");
            inv.setslot(slot, i.getItem());
        }
        player.openInventory(inv.getInv());
    }

    //해당 플레이어에게 게시판 창을 띄움 (수정 가능)
    public void modifyBoard(Player player) {
        InvClass inv = new InvClass(5, NoticeBoardManager.getGuiName() + "\uE206\uE201\uE201");
        for(int slot : items.keySet()) {
            inv.setslot(slot, items.get(slot));
        }
        player.openInventory(inv.getInv());
    }

    //특정 블럭에 게시판을 등록
    //등록하면 true, 등록 해제하면 false
    public boolean registeredBlock(Block block) {
        if(registered.contains(block)) {
            registered.remove(block);
            return false;
        }
        registered.add(block);
        return true;
    }

    //현재 게시판에 등록된 아이템들의 맵을 수정
    public boolean modifyItems(HashMap<Integer, ItemStack> items) {
        this.items = items;
        return true;
    }

    public ArrayList<Block> getRegisteredBlocks() {
        return registered;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("items", items);

        ArrayList<Location> locations = new ArrayList<>();
        for(Block block : registered) {
            locations.add(block.getLocation());
        }
        data.put("locations", locations);
        return data;
    }

    public static NoticeBoard deserialize(Map<String, Object> data) {
        return new NoticeBoard((String) data.get("name"), (Map<Integer, ItemStack>) data.get("items"), (ArrayList<Location>) data.get("locations"));
    }
}
