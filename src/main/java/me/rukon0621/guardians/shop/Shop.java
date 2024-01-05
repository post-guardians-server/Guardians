package me.rukon0621.guardians.shop;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Rand;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.rukon0621.guardians.main.pfix;

@SerializableAs("customShop")
public class Shop implements ConfigurationSerializable {
    private final String name;
    private ArrayList<ItemStack> items;
    private ArrayList<ItemStack> convertedItems;
    private ArrayList<Long> prices;
    private ArrayList<ItemData> dataMapping; //각 아이템의 최소 판매 레벨
    private final boolean isSellingShop;

    public Shop(String name, ArrayList<ItemStack> items, ArrayList<Long> prices, boolean isSellingShop) {
        this.name = name;
        this.items = items;
        convertedItems = new ArrayList<>();
        dataMapping = new ArrayList<>();
        this.prices = prices;
        this.isSellingShop = isSellingShop;

        for(ItemStack item : items) {
            ItemData itemData = new ItemData(item);
            if (itemData.getType().equals("세이버")) {
                try {
                    convertedItems.add(itemData.convertSaver().getItemStack());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                convertedItems.add(item);
            }
            dataMapping.add(itemData);
        }
    }

    public String getName() {
        return name;
    }

    public ArrayList<ItemStack> getItems() {
        return items;
    }
    public ArrayList<ItemStack> getConvertedItems() {
        return convertedItems;
    }

    public ArrayList<Long> getPrices() {
        return prices;
    }

    public int getMaxPage() {
        return (items.size()-1)/36 + 1;
    }

    public void buyItem(Player player, int id) {
        PlayerData pdc = new PlayerData(player);

        long money = ((Number) pdc.getMoney()).longValue();
        long price = ((Number) prices.get(id)).longValue();

        if(money < price) {
            Msg.send(player, "&c돈이 부족하여 구매하실 수 없습니다.", pfix);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
            return;
        }
        if(!InvClass.hasEnoughSpace(player.getInventory(), convertedItems.get(id))) {
            Msg.send(player, "&c인벤토리에 공간이 부족하여 아이템을 구매하실 수 없습니다.", pfix);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
            return;
        }
        pdc.setMoney(money - price);
        player.getInventory().addItem(convertedItems.get(id));
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, Rand.randFloat(0.8, 1.3));
        Msg.send(player, String.format("&7※ %d -> %d", money, money - price));
    }


    /**
     * 판매 상점에서 클릭한 아이템을 판매
     * @param item 판매하려는 아이템
     * @param isShift 쉬프트를 클릭하였는가 -> 클릭하면 클릭한 아이템 전부 판매
     * @return 판매 후 남은 아이템의 수 (이걸 통해 setAmount)
     */
    public int sellItem(Player player, ItemStack item, boolean isShift) {
        String name = item.getItemMeta().getDisplayName();
        int id = -1;
        for(int i = 0; i < convertedItems.size();i++) {
            String targetName = convertedItems.get(i).getItemMeta().getDisplayName();
            if(targetName.equals(name)) {
                id = i;
                break;
            }
        }
        if(id==-1) {
            Msg.send(player, "&c해당 아이템은 이 상점에서 판매할 수 없는 아이템입니다.", pfix);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
            return item.getAmount();
        }


        //아이템 데이터 체크
        ItemData itemData = new ItemData(item);
        ItemData viewData = dataMapping.get(id);
        if(itemData.hasAttr("level")) {
            if(itemData.getLevel()<viewData.getLevel()) {
                Msg.send(player, "&c해당 아이템의 레벨이 너무 낮아 아이템을 판매할 수 없습니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                return item.getAmount();
            }
        }
        for(String attr : viewData.getAttrs()) {
            if(itemData.getAttrLevel(attr)<viewData.getAttrLevel(attr)) {
                Msg.send(player, "&c해당 아이템은"+ attr +" 속성이 부족하여 판매할 수 없습니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                return item.getAmount();
            }
        }

        PlayerData pdc = new PlayerData(player);
        long money = ((Number) pdc.getMoney()).longValue();
        long price = ((Number) prices.get(id)).longValue();
        if(isShift) {
            int amount = item.getAmount();
            pdc.setMoney(money + (price * amount));
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, Rand.randFloat(0.8, 1.3));
            Msg.send(player, String.format("&7※ %d -> %d (총 %d개를 판매하였습니다.)", money, money + (price * amount), amount));
            return 0;
        }
        else {
            pdc.setMoney(money + price);
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, Rand.randFloat(0.8, 1.3));
            Msg.send(player, String.format("&7※ %d -> %d", money, money + price));
            return item.getAmount() - 1;
        }
    }

    public void addNewItem(ItemStack item, long price) {
        items.add(item);
        prices.add(price);
        ItemData itemData = new ItemData(item);
        try {
            if(itemData.getType().equals("세이버")) {
                convertedItems.add(itemData.convertSaver().getItemStack());
            }
            else convertedItems.add(item);
            dataMapping.add(itemData);
        } catch (NullPointerException e) {
            dataMapping.add(itemData);
        }
    }
    public void removeItem(int id) {
        items.remove(id);
        convertedItems.remove(id);
        prices.remove(id);
        dataMapping.remove(id);
    }

    public boolean isSellingShop() {return isSellingShop;}

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("items", items);
        data.put("prices", prices);
        data.put("selling", isSellingShop);
        return data;
    }

    public static Shop deserialize(Map<String, Object> data) {
        return new Shop((String) data.get("name"), (ArrayList<ItemStack>) data.get("items"), (ArrayList<Long>) data.get("prices"), (Boolean) data.get("selling"));
    }
}
