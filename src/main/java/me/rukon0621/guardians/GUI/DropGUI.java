package me.rukon0621.guardians.GUI;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static me.rukon0621.guardians.data.LevelData.EXP_BOOK_TYPE_NAME;

@Deprecated
public class DropGUI implements Listener {
    private static final String guiName = "\uE200\uE200\uE200\uE210";

    public DropGUI() {
        Bukkit.getPluginManager().registerEvents(this, main.getPlugin());
    }

    public void openDropGUI(Player player) {
        player.playSound(player, Sound.BLOCK_CHAIN_PLACE, 1, 0.5f);
        InvClass inv = new InvClass(1, guiName);
        inv.setslot(4, player.getInventory().getItemInMainHand());
        ItemClass it = new ItemClass(new ItemStack(Material.BARRIER), "&c취소");
        it.addLore("&f아이템 버리기를 취소합니다.");
        inv.setslot(2, it.getItem());
        it = new ItemClass(new ItemStack(Material.LAVA_BUCKET), "&4버리기");
        it.addLore("&f손에 들고 있는 아이템을 버립니다.");
        it.addLore("&c버린 아이템은 &4영구적으로 삭제&c됩니다.");
        inv.setslot(6, it.getItem());
        player.openInventory(inv.getInv());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if(e.getItemDrop().getItemStack().equals(EquipmentManager.getEquipment(e.getPlayer(),"무기"))) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                ItemData itemData = new ItemData(e.getPlayer().getInventory().getItemInMainHand());
                if(itemData.isQuestItem()) {
                    Msg.warn(e.getPlayer(), "퀘스트 아이템과 중요한 아이템은 버릴 수 없습니다!");
                    return;
                }
                else if(itemData.getType().equals(EXP_BOOK_TYPE_NAME)) {
                    Msg.warn(e.getPlayer(), "지식의 서는 버릴 수 없습니다.");
                    return;
                }
                openDropGUI(e.getPlayer());
            }
        }.runTaskLater(main.getPlugin(), 1);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(!e.getView().getTitle().equals(guiName)) return;
        if(!(e.getWhoClicked() instanceof Player player)) return;
        e.setCancelled(true);
        if(e.getRawSlot()==6) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            player.playSound(player, Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
            player.closeInventory();
        }
        else if(e.getRawSlot()==2||e.getRawSlot()==-999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
        }
    }

}
