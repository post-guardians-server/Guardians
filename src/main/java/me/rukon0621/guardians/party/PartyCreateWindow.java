package me.rukon0621.guardians.party;

import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.Window;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PartyCreateWindow extends Window {
    private static final String partyGenerateGuiName = "&f\uF000\uF01E";
    public PartyCreateWindow(Player player) {
        super(player, partyGenerateGuiName, 4);
        map.put(13, new Button() {
            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&7[ &e파티 생성하기 &7]");
                item.setCustomModelData(7);
                item.addLore("&f파티를 생성하여 1짱을 먹어보세요!");
                return item.getItem();
            }

            @Override
            public void execute(Player player, ClickType clickType) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.3f);
                PartyManager.createNewParty(player);
            }
        });
        reloadGUI();
        open();
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) new MenuWindow(player, 1);
    }
}
