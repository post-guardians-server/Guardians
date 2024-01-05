package me.rukon0621.guardians.listeners;

import me.rukon0621.guardians.GUI.TitleWindow;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.events.ItemClickEvent;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import static me.rukon0621.guardians.main.pfix;

public class TitleListener implements Listener {

    public TitleListener() {
        main.getPlugin().getServer().getPluginManager().registerEvents(this, main.getPlugin());
    }
    public static ItemStack getTitleItem(String title) {
        return getTitleItem(title, 0);
    }

    public static ItemStack getTitleItem(String title, double durationDay) {
        ItemClass item = new ItemClass(new ItemStack(Material.BOOK), title);
        item.addLore("&7칭호를 획득하려면 들고 우클릭하십시오.");
        if(durationDay > 0) {
            item.addLore("&7이 칭호는 기간제 칭호입니다.");
            item.addLore("&7수치는 지속되는 일 수를 의미합니다.");
        }
        ItemData itemData = new ItemData(item);
        itemData.setType("칭호");
        if(durationDay > 0) itemData.setValue(durationDay);
        return itemData.getItemStack();
    }

    @EventHandler
    public void onUseTitle(ItemClickEvent e) {
        if(!e.getItemData().getType().equals("칭호")) return;
        Player player = e.getPlayer();
        PlayerData pdc = new PlayerData(player);
        if(pdc.getTitles().size()==54) {
            Msg.warn(player, "칭호 창이 꽉 찼습니다. 더이상 칭호를 얻을 수 없습니다.");
            return;
        }
        if(e.getItemData().hasKey("value")) {
            TitleWindow.addTitle(null, player, e.getItemData().getName(), e.getItemData().getValue());
        }
        else TitleWindow.addTitle(null, player, e.getItemData().getName(), -1);
        e.consume();
        new TitleWindow(player);
        Msg.send(player, "새로운 칭호를 획득했습니다.", pfix);
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
    }

}
