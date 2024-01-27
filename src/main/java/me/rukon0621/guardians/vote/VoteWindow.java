package me.rukon0621.guardians.vote;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.gui.buttons.Icon;
import me.rukon0621.gui.windows.Window;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static me.rukon0621.guardians.main.pfix;

public class VoteWindow extends Window {

    private static final VoteManager manager = main.getPlugin().getVoteManager();

    public VoteWindow(Player player, boolean get) {
        super(player, "&f\uF000", 4);
        for(Integer i : manager.getItems().keySet()) {
            map.put(i - 1, new Icon() {
                @Override
                public ItemStack getIcon() {
                    ItemClass item = new ItemClass(manager.getItem(i).clone());
                    item.setName(item.getName() + " &e[ " + i + "일차 ]");
                    return item.getItem();
                }
            });
        }
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1.3f);
        new BukkitRunnable() {
            @Override
            public void run() {
                if(get) {
                    PlayerData pdc = new PlayerData(player);
                    ItemStack item = manager.getItem(pdc.getVoteDays());

                    if(item == null) {
                        Msg.send(player, "금일은 받을 수 있는 아이템이 없습니다.", pfix);
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                        return;
                    }

                    MailBoxManager.giveOrMail(player, item);
                    Msg.send(player, String.format("%d일차의 출석 선물을 받았습니다. &7- &f%s &7(/출석체크 명령어로 이 창을 다시 확인할 수 있습니다.)", pdc.getVoteDays(), new ItemClass(item).getName()), pfix);
                    player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.5f);
                }
            }
        }.runTaskLater(main.getPlugin(), 5);
        reloadGUI();
        open();
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) player.closeInventory();
    }
}
