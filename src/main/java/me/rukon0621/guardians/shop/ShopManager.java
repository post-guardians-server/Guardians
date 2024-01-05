package me.rukon0621.guardians.shop;

import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static me.rukon0621.guardians.main.pfix;

public class ShopManager implements Listener {
    private static final main plugin = main.getPlugin();

    private static final String guiName = "&f\uF000\uF010";
    private static HashMap<String, Shop> shopData;
    private static HashMap<Player, Shop> playerShopping;
    private static HashMap<Player, Integer> playerShoppingPage;

    private static File getDirectory() {
        return new File(FileUtil.getOuterPluginFolder()+"/shops");
    }

    public static HashMap<String, Shop> getShopData() {
        return shopData;
    }

    private static Configure getConfig(String shopName) {
        File file = new File(getDirectory().getPath()+"/"+shopName+".yml");
        return new Configure(file);
    }

    public ShopManager() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reloadAllShops();
    }

    public static void reloadAllShops() {
        shopData = new HashMap<>();
        playerShopping = new HashMap<>();
        playerShoppingPage = new HashMap<>();
        File dir = getDirectory();
        dir.mkdir();
        for(File file : dir.listFiles()) {
            Configure config = new Configure(file);
            Shop shop = (Shop) config.getConfig().get("shopData");
            shopData.put(shop.getName(), shop);
        }
    }

    public static void createNewShop(Player player, String name, boolean isSellingShop) {
        File file = new File(getDirectory().getPath()+"/"+name+".yml");
        if(file.exists()) {
            Msg.send(player, "&c해당 상점은 이미 존재합니다.", pfix);
            return;
        }
        Configure config = new Configure(file);
        Shop shop = new Shop(name, new ArrayList<>(), new ArrayList<>(), isSellingShop);
        config.getConfig().set("shopData", shop);
        config.saveConfig();
        reloadAllShops();
        Msg.send(player, "성공적으로 상점을 생성했습니다.", pfix);
    }

    public static void sendShopList(Player player) {
        for(String name : shopData.keySet()) {
            Msg.send(player, name);
        }
    }

    public static void deleteShop(Player player, String name) {
        File file = new File(getDirectory().getPath()+"/"+name+".yml");
        if(!file.exists()) {
            Msg.send(player, "&c해당 상점은 존재하지 않는 상점입니다.", pfix);
            return;
        }
        Configure config = new Configure(file);
        config.delete("shops");
        reloadAllShops();
        Msg.send(player, "성공적으로 상점을 삭제했습니다.", pfix);
    }

    public static boolean openShop(Player player, String name) {
        if(!shopData.containsKey(name)) return false;
        Shop shop = shopData.get(name);
        playerShopping.put(player, shop);
        playerShoppingPage.put(player, 1);
        openShopGUI(player, shop);
        return true;
    }

    private static void openShopGUI(Player player, Shop shop) {
        int page = playerShoppingPage.get(player);

        InvClass inv = new InvClass(6, guiName);

        int id = (page-1)*36;

        ArrayList<ItemStack> items = shop.getConvertedItems();
        ArrayList<Long> prices = shop.getPrices();

        int slot = 9;
        for(int i = id; i < id + 36;i++) {
            try {
                ItemClass item = new ItemClass(new ItemStack(items.get(i)));
                item.addLore(" ");
                if(shop.isSellingShop()) {
                    item.addLore("&b판매 가격: " + prices.get(i));
                    item.addLore(" ");
                    item.addLore("&7인벤토리에서 판매할 아이템을 클릭해주세요.");
                    item.addLore("&7쉬프트 클릭으로 클릭한 아이템을 전부 판매할 수 있습니다.");
                }
                else {
                    item.addLore("&a구매 가격: " + prices.get(i));
                }

                inv.setslot(slot, item.getItem());


            } catch (IndexOutOfBoundsException e) {
                break;
            }
            slot++;
        }

        String pageStr = String.format("&7( %d / %d )", playerShoppingPage.get(player), shop.getMaxPage());

        ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&c[ 이전 페이지 ] " + pageStr);
        it.setCustomModelData(7);
        inv.setslot(48, it.getItem());
        it = new ItemClass(new ItemStack(Material.SCUTE), "&c[ 다음 페이지 ] " + pageStr);
        it.setCustomModelData(7);
        inv.setslot(50, it.getItem());

        player.openInventory(inv.getInv());
    }

    public static void addShopItem(Player player, String shopName, ItemStack item, long price) {
        if(!shopData.containsKey(shopName)) {
            Msg.send(player, "&c해당 상점은 존재하지 않는 상점입니다.", pfix);
            return;
        }
        Shop shop = shopData.get(shopName);
        shop.addNewItem(item, price);

        Configure config = getConfig(shopName);
        config.getConfig().set("shopData", shop);
        config.saveConfig();
        reloadAllShops();
        Msg.send(player, "새로운 아이템을 등록하였습니다.", pfix);
    }
    public static void removeShopItem(Player player, String shopName, int id) {
        if(!shopData.containsKey(shopName)) {
            Msg.send(player, "&c해당 상점은 존재하지 않는 상점입니다.", pfix);
            return;
        }
        Shop shop = shopData.get(shopName);

        if(id >= shop.getItems().size()) {
            Msg.send(player, "&c해당 ID는 존재하지 않습니다.", pfix);
            return;
        }

        shop.removeItem(id);
        Configure config = getConfig(shopName);
        config.getConfig().set("shopData", shop);
        config.saveConfig();
        reloadAllShops();

        Msg.send(player, "기존 아이템을 삭제하였습니다.", pfix);
    }

    @EventHandler
    public void onClickShop(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals(guiName)) return;
        e.setCancelled(true);
        if(e.getClick().equals(ClickType.DOUBLE_CLICK)) return;
        Shop shop = playerShopping.get(player);
        int page = playerShoppingPage.get(player);
        int maxPage = shop.getMaxPage();

        if(e.getRawSlot()==-999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1 ,1.3f);
            player.closeInventory();
            return;
        }

        //페이지
        if(e.getRawSlot()==48) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1 ,1.3f);
            if(page==1) {
                page = maxPage;
            }
            else page--;
            playerShoppingPage.put(player, page);
            openShopGUI(player, shop);
            return;
        }
        else if (e.getRawSlot()==50) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1 ,1.3f);
            if(page==maxPage) {
                page = 1;
            }
            else page++;

            playerShoppingPage.put(player, page);
            openShopGUI(player, shop);
            return;
        }
        if(e.getCurrentItem()==null) return;

        if(shop.isSellingShop()) {
            if(e.getRawSlot()<54) return;
            if(main.unusableSlots.contains(e.getRawSlot())) return;

            if(e.getClick().equals(ClickType.SHIFT_RIGHT)||e.getClick().equals(ClickType.SHIFT_LEFT)) {
                e.getCurrentItem().setAmount(shop.sellItem(player, e.getCurrentItem(), true));
            }
            else {
                e.getCurrentItem().setAmount(shop.sellItem(player, e.getCurrentItem(), false));
            }
            return;
        }

        if(!(e.getRawSlot()>=9&&e.getRawSlot()<27)) return;
        int id = ((page-1)*36) + (e.getRawSlot()-9);
        shop.buyItem(player, id);
    }

}
