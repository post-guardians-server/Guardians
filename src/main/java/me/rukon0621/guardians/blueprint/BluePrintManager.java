package me.rukon0621.guardians.blueprint;

import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.ItemGrade;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.events.ItemClickEvent;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static me.rukon0621.guardians.main.pfix;

public class BluePrintManager implements Listener {
    private final main plugin = main.getPlugin();
    private final HashMap<String, ItemStack> blueprintData = new HashMap<>();
    private final HashMap<String, ItemStack> consumableBlueprintData = new HashMap<>();

    private Configure getConfig() {
        return new Configure("blueprint.yml", FileUtil.getOuterPluginFolder().getPath());
    }

    public Set<String> getAllBlueprintName() {
        Set<String> set = new HashSet<>();
        set.addAll(blueprintData.keySet());
        set.addAll(consumableBlueprintData.keySet());
        return set;
    }

    public HashMap<String, ItemStack> getBlueprintData() {
        return blueprintData;
    }

    public HashMap<String, ItemStack> getConsumableBlueprintData() {
        return consumableBlueprintData;
    }

    public BluePrintManager() {
        reloadBlueprints();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void reloadBlueprints() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Configure config = getConfig();
                blueprintData.clear();
                consumableBlueprintData.clear();
                for(String name : config.getConfig().getKeys(false)) {
                    ItemStack item = config.getConfig().getItemStack(name);
                    ItemData id = new ItemData(item);
                    if(id.getType().equals("청사진")) blueprintData.put(name, config.getConfig().getItemStack(name));
                    else if(id.getType().equals("일회성 청사진")) consumableBlueprintData.put(name, config.getConfig().getItemStack(name));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     *
     * @param player player
     * @param name name
     * @param grade grade
     * @param level level
     * @param untradable 거래불가 여부
     */
    public void createNewBlueprint(Player player, String name, ItemGrade grade, int level, boolean untradable, boolean consumable) {
        if(getAllBlueprintName().contains(name)) {
            Msg.warn(player, "이미 존재하는 이름의 청사진입니다.");
            return;
        }
        ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&7청사진: "+ grade.getColor() + name);
        item.addLore("&7아이템을 제작하기 위해 필요한 청사진이다.");
        item.setCustomModelData(grade.getBlueprintCMD());
        ItemData itemData = new ItemData(item);
        if (consumable) itemData.setType("일회성 청사진");
        else itemData.setType("청사진");
        itemData.setLevel(level);
        itemData.setGrade(grade);
        itemData.setUntradable(untradable);

        Configure config = getConfig();
        config.getConfig().set(name, itemData.getItemStack());
        config.saveConfig();
        reloadBlueprints();
        Msg.send(player, "새로운 청사진을 생성했습니다.", pfix);
    }

    public void deleteBlueprint(Player player, String name) {
        if(!getAllBlueprintName().contains(name)) {
            Msg.warn(player, "존재하지 않는 이름의 청사진입니다.");
            return;
        }
        Configure config = getConfig();
        config.getConfig().set(name, null);
        config.saveConfig();
        reloadBlueprints();
        Msg.send(player, "청사진을 삭제했습니다.", pfix);
    }

    public void setBlueprint(Player player, String name) {
        if(!getAllBlueprintName().contains(name)) {
            Msg.warn(player, "존재하지 않는 이름의 청사진입니다.");
            return;
        }
        Configure config = getConfig();
        ItemStack item = player.getInventory().getItemInMainHand();
        player.getInventory().setItemInMainHand(getBlueprintItem(name));
        config.saveConfig();
        reloadBlueprints();
        player.getInventory().addItem(item);
        Msg.send(player, "청사진을 설정하고 기존 아이템을 되돌려 받았습니다.", pfix);
    }

    public void giveBlueprint(Player player, String name) {
        if(!blueprintData.containsKey(name)) {
            if(!consumableBlueprintData.containsKey(name)) {
                Msg.warn(player, "존재하지 않는 이름의 청사진입니다.");
                return;
            }
        }
        if(consumableBlueprintData.containsKey(name)) {
            player.getInventory().addItem(consumableBlueprintData.get(name));
        }
        else {
            player.getInventory().addItem(blueprintData.get(name));
        }
        Msg.send(player, "새로운 청사진을 지급 받았습니다.", pfix);
    }

    public ItemStack getBlueprintItem(String name) {
        if(consumableBlueprintData.containsKey(name)) return consumableBlueprintData.get(name).clone();
        return blueprintData.get(name).clone();
    }

    @EventHandler
    public void onRightClickBluePrint(ItemClickEvent e) {
        ItemData data = e.getItemData();
        if(!(data.getType().equals("청사진") || data.getType().equals("일회성 청사진"))) return;
        Player player = e.getPlayer();
        if(data.getLevel()>new PlayerData(player).getLevel()) {
            Msg.warn(player, "청사진의 레벨이 플레이어보다 높아 사용할 수 없습니다");
            return;
        }
        String name = Msg.uncolor(data.getName()).split(": ")[1].trim();
        PlayerData pdc = new PlayerData(player);
        if (data.getType().equals("청사진")) {
            if(pdc.addBlueprint(name)) {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.7f);
                player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
                player.playSound(player, Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1, 1);
                Msg.send(player, "&e새로운 청사진을 습득했습니다!", String.format("&7[ %s%s &7] ", data.getGrade().getColor(), name));
                e.consume();
            }
            else {
                Msg.warn(player, "이미 이 청사진을 습득하였습니다.");
            }
        }
        else {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.7f);
            player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
            player.playSound(player, Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1, 1);
            pdc.addConsumableBlueprint(name);
            Msg.send(player, String.format("&e일회성 청사진을 사용했습니다. 아이템을 제작하면 청사진이 1개 사라집니다. &7(현재 가진 이 청사진: %d개)", pdc.getConsumableBlueprintsData().get(name)), String.format("&7[ %s%s &7] ", data.getGrade().getColor(), name));
            e.consume();
        }
        LogManager.log(player, "blueprintConsumption", name + (data.getType().equals("일회성 청사진") ? "(일회성)" : ""));
    }
}
