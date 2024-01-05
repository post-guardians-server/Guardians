package me.rukon0621.guardians.afk;

import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.main;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.Window;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.listeners.LogInOutListener.dataCategories;

public class AfkConfirmWindow extends Window {
    public AfkConfirmWindow(Player player) {
        super(player, "&f\uF000", 1);
        map.put(3, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                player.closeInventory();
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 0.5f);
                LogInOutListener.getLogoutEventBlocked().add(player);
                CountDownLatch latch = new CountDownLatch(dataCategories);
                LogInOutListener.saveAllDataToDB(player, latch);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                AfkManager.startAFK(player);
                            }
                        }.runTask(main.getPlugin());
                    }
                }.runTaskAsynchronously(main.getPlugin());
            }

            @Override
            public ItemStack getIcon() {
                ItemClass it = new ItemClass(new ItemStack(Material.GREEN_WOOL), "&a[ 잠수 시작하기 ]");
                it.addLore("&c※ 주의 ※");
                it.addLore("&f잠수를 중단하면 &c대기열 서버&f로 들어가게 됩니다.");
                it.addLore("&f잠수 도중 &c움직이면 잠수가 중단&f되니 미리 숙지하고 입장하시기 바랍니다.");
                it.addLore(" ");
                it.addLore("&e잠수를 시작하려면 클릭하십시오.");
                return it.getItem();
            }
        });

        map.put(5, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                player.closeInventory();
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 0.5f);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass it = new ItemClass(new ItemStack(Material.RED_WOOL), "&c[ 취소 ]");
                it.addLore("&e클릭하여 이 창을 닫습니다.");
                return it.getItem();
            }
        });
        reloadGUI();
        open();
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) player.closeInventory();
    }
}
