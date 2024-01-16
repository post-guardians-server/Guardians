package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.main;
import me.rukon0621.pay.RukonPayment;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class ItemSaver implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    private static HashMap<String, ItemStack> itemSaverData;


    /**
     *
     * @param containString 포함 문자열
     * @return 해당 문자열을 포함하는 이름의 itemSaver를 검색
     */
    public static List<String> searchItemSaverNames(String containString) {
        List<String> result = new ArrayList<>();
        for(String s : itemSaverData.keySet()) {
            if(!s.toLowerCase().contains(containString.toLowerCase())) continue;
            result.add(s);
        }
        return result;
    }

    private static Configure getConfig() {
        return new Configure("itemSaver.yml", FileUtil.getOuterPluginFolder().getPath());
    }

    public static void reloadItemSaver() {
        itemSaverData = new HashMap<>();
        Configure config = getConfig();
        for(String name : config.getConfig().getKeys(false)) {
            itemSaverData.put(name, (ItemStack) config.getConfig().get(name));
        }
    }

    public static ItemClass getItem(String name) {
        if(!itemSaverData.containsKey(name)) {
            Bukkit.getLogger().severe("탐색되지 않는 아이템 세이버 - " + name);
            return null;
        }
        return new ItemClass(new ItemStack(itemSaverData.get(name)));
    }

    public static ItemClass getItemParsed(String name, int level) {
        return getItemDataParsed(name, level).getItem();
    }
    public static ItemData getItemDataParsed(String name, int level) {
        ItemData itemData = new ItemData(itemSaverData.get(name).clone());
        if(itemData.hasAttr("season")) itemData.setSeason(RukonPayment.inst().getPassManager().getSeason());
        itemData.setLevel(level);
        return itemData;
    }

    public static boolean isItemExist(String name) {
        return itemSaverData.containsKey(name);
    }

    public static void addNewItemSaver(String name, ItemStack item) {
        itemSaverData.put(name, item);
    }
    public static void removeItemSaver(String name) {
        itemSaverData.remove(name);
    }
    public static void saveAllFromCache() {
        Configure config = getConfig();
        config.delete();
        config = getConfig();
        for(String name : itemSaverData.keySet()) {
            config.getConfig().set(name, itemSaverData.get(name));
        }
        config.saveConfig();
    }

    public static boolean hasItem(Player player, String saverName) {
        return hasItem(player, saverName, 1);
    }

    public static boolean hasItem(Player player, String saverName, int amount) {
        String targetName = itemSaverData.get(saverName).getItemMeta().getDisplayName();
        int found = 0;
        for(ItemStack item : player.getInventory().getContents()) {
            if(item==null || !item.hasItemMeta()) continue;
            if(item.getItemMeta().getDisplayName().equals(targetName)) {
                found += item.getAmount();
                if(found >= amount) return true;
            }
        }


        return false;
    }

    /**
     * @param player player
     * @param saverName 아이템 세이버 이름
     * @return 아이템을 amount에 맞춰서 완전히 가져갔는가
     */
    public static boolean removeItem(Player player, String saverName) {
        return removeItem(player, saverName, 1);
    }

    /**
     * @param player player
     * @param saverName 아이템 세이버 이름
     * @param amount 가져갈 최대개수 (amount보다 가진 아이템이 적으면 없어질 때까지 최대한 가져감)
     * @return 아이템을 amount에 맞춰서 완전히 가져갔는가
     */
    public static boolean removeItem(Player player, String saverName, int amount) {
        String targetName = itemSaverData.get(saverName).getItemMeta().getDisplayName();
        int found = 0;
        for(ItemStack item : player.getInventory().getContents()) {
            if(item==null || !item.hasItemMeta()) continue;
            if(item.getItemMeta().getDisplayName().equals(targetName)) {
                int preAmount = item.getAmount();
                item.setAmount(Math.max(0, item.getAmount() - (amount - found)));
                found = Math.min(amount, preAmount + found);
                if(found >= amount) return true;
            }
        }
        return false;
    }


    /**
     * 변경된 아이템 세이버의 정보로 기존 아이템을 바꿈
     * @param item 바꿀 아이템
     * @return 바뀐 아이템
     */
    public static ItemStack reloadItem(ItemStack item) {
        try {
            ItemData original = new ItemData(item);
            ItemData reloaded = getItemDataParsed(Msg.uncolor(item.getItemMeta().getDisplayName()), original.getLevel());


            if(original.hasKey("enhanceLevel")) {
                reloaded.setEnhanceLevel(original.getEnhanceLevel());
            }
            if(original.hasKey("craftLevel")) {
                reloaded.setCraftLevel(original.getCraftLevel());
            }

            if(original.getDataMap().containsKey("exp")) {
                reloaded.setExp(original.getExp());
            }
            if(original.isEquipment()&&!original.getType().equals("사증")) {
                reloaded.setQuality(original.getQuality());
            }
            reloaded.setSeason(-1);

            for(String attrName : original.getAttrs()) {
                reloaded.setAttr(attrName, original.getAttrLevel(attrName));
            }
            reloaded.setAmount(original.getAmount());
            return reloaded.getItemStack();
        } catch (Exception e) {
            return item;
        }
    }

    //두 아이템이 비슷한 아이템인지 확인
    //두 아이템의 이름이 같으면 비슷한 아이템으로 인식
    //제작 재료 넣기에서 사용됨
    public static boolean isSimilar(ItemStack item, String name) {
        return item.getItemMeta().getDisplayName().equals(itemSaverData.get(name).getItemMeta().getDisplayName());
    }

    //COMMANDS
    public ItemSaver() {
        plugin.getCommand("item").setExecutor(this);
        plugin.getCommand("item").setTabCompleter(new ItemSaverCommandTabComp());
        reloadItemSaver();
    }

    private static final String[] arguments = {"설정", "자동설정", "삭제", "지급", "목록"};

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7아이템 세이버 리로드중...");
            reloadItemSaver();
            MsgUtil.cmdMsg(sender, "&a아이템 세이버 리로드 완료!");
            return true;
        }

        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }

        Configure config = new Configure("itemSaver.yml", FileUtil.getOuterPluginFolder().getPath());
        if(args[0].equals("설정")) {
            if(args.length<2) {
                usage(player, "설정", true);
                return true;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item==null||item.getType().equals(Material.AIR)) {
                Msg.send(player, "&c설정할 아이템을 손에 들어주세요.", pfix);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            item.setAmount(1);
            if(itemSaverData.containsKey(name)) {
                Msg.send(player, "&e기존에 있던 아이템을 돌려받고 새로운 아이템으로 변경하였습니다.", pfix);
                player.getInventory().setItemInMainHand((ItemStack) config.getConfig().get(name));
            } else {
                Msg.send(player, "새로운 아이템을 설정하였습니다.", pfix);
            }
            config.getConfig().set(name, item);
            config.saveConfig();
            reloadItemSaver();
            return true;
        }
        else if(args[0].equals("자동설정")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getType().equals(Material.AIR)) {
                Msg.send(player, "&c설정할 아이템을 손에 들어주세요.", pfix);
                return true;
            }
            String name = Msg.uncolor(item.getItemMeta().getDisplayName());
            item.setAmount(1);
            if(itemSaverData.containsKey(name)) {
                Msg.send(player, "&e기존에 있던 아이템을 돌려받고 새로운 아이템으로 변경하였습니다.", pfix);
                player.getInventory().setItemInMainHand((ItemStack) config.getConfig().get(name));
            } else {
                Msg.send(player, "새로운 아이템을 설정하였습니다.", pfix);
            }
            config.getConfig().set(name, item);
            config.saveConfig();
            reloadItemSaver();
            return true;
        }
        else if(args[0].equals("삭제")) {
            if(args.length<2) {
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!itemSaverData.containsKey(name)) {
                Msg.send(player, "&c해당 아이템은 존재하지 않는 아이템입니다.", pfix);
                return true;
            }
            if(!InvClass.hasEnoughSpace(player.getInventory(), (ItemStack) config.getConfig().get(name))) {
                Msg.send(player, "&c인벤토리가 꽉 찼습니다. 아이템을 돌려받을 공간을 확보해야합니다.", pfix);
                return true;
            }
            Msg.send(player, "&6기존 아이템을 삭제했습니다.", pfix);
            player.getInventory().addItem((ItemStack) config.getConfig().get(name));
            config.getConfig().set(name, null);
            config.saveConfig();
            reloadItemSaver();
            return true;
        }
        else if(args[0].equals("지급")) {
            if(args.length<2) {
                usage(player, "지급", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!itemSaverData.containsKey(name)) {
                Msg.send(player, "&c해당 아이템은 존재하지 않는 아이템입니다.", pfix);
                return true;
            }
            ItemStack item = (ItemStack) config.getConfig().get(name);
            if(!InvClass.hasEnoughSpace(player.getInventory(), item)) {
                Msg.send(player, "&c인벤토리에 공간이 부족합니다.", pfix);
                return true;
            }
            Msg.send(player, "아이템을 지급받았습니다.", pfix);
            player.getInventory().addItem((ItemStack) config.getConfig().get(name));
            return true;
        }
        else if(args[0].equals("목록")) {
            Msg.send(player, "&6서버에 존재하는 아이템의 목록입니다.", pfix);
            if(args.length>1) {
                String filter = args[1];
                for(String name : config.getConfig().getKeys(false)) {
                    if(!name.startsWith(filter)) continue;
                    Msg.send(player, name);
                }
                return true;
            }
            for(String name : config.getConfig().getKeys(false)) {
                Msg.send(player, name);
            }
        }
        else {
            usages(player);
        }
        return true;
    }
    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equalsIgnoreCase("설정")) {
            Msg.send(player, "&6/아이템 설정 <이름>");
            Msg.send(player, "&7    해당 이름의 아이템을 영구적으로 저장합니다.");
            Msg.send(player, "&7    이미 아이템이 설정돼있다면 기존 아이템을 지급받고 변경합니다.");
        }
        else if(arg.equalsIgnoreCase("삭제")) {
            Msg.send(player, "&6/아이템 삭제 <이름>");
            Msg.send(player, "&7    config에 저장된 아이템을 영구적으로 삭제합니다.");
            Msg.send(player, "&7    삭제된 아이템은 인벤토리에 돌아옵니다.");
        }
        else if(arg.equalsIgnoreCase("지급")) {
            Msg.send(player, "&6/아이템 지급 <이름>");
            Msg.send(player, "&7    config에 저장된 아이템을 지급 받습니다.");
        }
        else if(arg.equalsIgnoreCase("목록")) {
            Msg.send(player, "&6/아이템 목록 [<검색필터>]");
            Msg.send(player, "&7    config에 저장된 아이템들의 목록을 확인합니다.");
            Msg.send(player, "&7    검색 필터에 값을 입력하면 해당 문자열로 시작하는 아이템만 검색됩니다.");
        }
        Msg.send(player, " ");
        if (forone) Msg.send(player, "&e└────────────────────────┘");
    }
    private void usages(Player player) {
        Msg.send(player, "&e┌────────────────────────┐");
        Msg.send(player, " ");
        for(String s : arguments) {
            usage(player, s, false);
        }
        Msg.send(player, "&e└────────────────────────┘");
    }


}
