package me.rukon0621.guardians.blueprint;

import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.main;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.buttons.Icon;
import me.rukon0621.gui.windows.ScrollableWindow;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlueprintWindow extends ScrollableWindow {
    private static final BluePrintManager manager = main.getPlugin().getBluePrintManager();

    public BlueprintWindow(Player player) {
        super(player, "&f\uF000\uF023", 6, 0, getHeight(player));
        PlayerData pdc = new PlayerData(player);
        List<String> bls = new ArrayList<>(pdc.getBlueprintsData());
        List<String> conBls = new ArrayList<>(pdc.getConsumableBlueprintsData().keySet());
        bls.sort(String::compareTo);
        conBls.sort(String::compareTo);
        bls.addAll(conBls);
        int dx = 0;
        int dy = 0;
        for(String name : bls) {
            if(!manager.getAllBlueprintName().contains(name)) continue;
            icons[dy][dx] = new BluePrintIcon(name);
            dx++;
            if(dx==8) {
                dx = 0;
                dy++;
            }
        }
        ItemClass item = new ItemClass(new ItemStack(Material.SCUTE));
        item.setCustomModelData(7);
        map.put(26, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                y--;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                reloadGUI();
            }

            @Override
            public ItemStack getIcon() {
                item.setName("&6↑");
                return item.getItem();
            }
        });
        map.put(35, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                y++;
            }

            @Override
            public ItemStack getIcon() {
                item.setName("&6↓");
                return item.getItem();
            }
        });
        reloadGUI();
        open();
    }

    private static int getHeight(Player player) {
        PlayerData pdc = new PlayerData(player);
        int size = pdc.getBlueprintsData().size() + pdc.getConsumableBlueprintsData().size();
        return Math.max(0, (size - 49) / 8 + 1);
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) new MenuWindow(player, 2);
    }

    class BluePrintIcon extends Icon {
        int num = -1;
        ItemStack item;

        public BluePrintIcon(String name) {
            PlayerData pdc = new PlayerData(player);
            if(pdc.getConsumableBlueprintsData().containsKey(name)) {
                num = pdc.getConsumableBlueprintsData().get(name);
            }
            item = manager.getBlueprintItem(name);

            if(num > -1) {
                ItemClass ic = new ItemClass(item);
                ic.addLore(" ");
                ic.setAmount(Math.min(64, num));
                ic.addLore("&7가진 청사진 수: " + num);
                item = ic.getItem();
            }
        }

        @Override
        public ItemStack getIcon() {
            return item;
        }
    }
}
