package me.rukon0621.guardians.mailbox;

import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.Window;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class MailBoxWindow extends Window {
    private static final main plugin = main.getPlugin();
    private final static Set<String> mailBoxUsingPlayer = new HashSet<>();

    public static Set<String> getMailBoxUsingPlayer() {
        return mailBoxUsingPlayer;
    }

    private boolean blockClicking = false;
    private int page = 1;
    private int maxPage = 1;

    public MailBoxWindow(Player player) {
        super(player, "&f\uF000\uF016", 6);
        mailBoxUsingPlayer.add(player.getUniqueId().toString());
        new BukkitRunnable() {
            @Override
            public void run() {
                CountDownLatch latch = new CountDownLatch(1);
                reloadGUI(latch);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        open();
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }


    @Override
    protected void reloadGUI() {
        reloadGUI(null);
    }

    /**
     * Manual Async and open gui with sync
     */
    protected void reloadGUI(@Nullable CountDownLatch latch) {
        map.clear();
        List<ItemStack> list = MailBoxManager.getMailData(player);
        maxPage = (list.size()-1)/45 + 1;
        if(page>maxPage) page = maxPage;
        int slot = 0;
        int iMax = Math.min(45*page, list.size());
        for(int i = 45*(page-1); i < iMax; i++) {
            map.put(slot, new MailItemButton(list.get(i), i));
            slot++;
        }
        map.put(48, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(blockClicking) return;
                page--;
                if(page==0) page = maxPage;
                blockClicking = true;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            reloadGUI();
                        } catch (Exception ignored) {}
                    }
                }.runTaskAsynchronously(plugin);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), String.format("&9이전 페이지 &7[ &f%d &8/ &e%d &7]", page, maxPage));
                item.setCustomModelData(7);
                return item.getItem();
            }
        });
        map.put(50, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(blockClicking) return;
                page++;
                if(page>maxPage) page = 1;
                blockClicking = true;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            reloadGUI();
                        } catch (Exception ignored) {}
                    }
                }.runTaskAsynchronously(plugin);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), String.format("&c다음 페이지 &7[ &f%d &8/ &e%d &7]", page, maxPage));
                item.setCustomModelData(7);
                return item.getItem();
            }
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                MailBoxWindow.super.reloadGUI();
                if(latch!=null) latch.countDown();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        blockClicking = false;
                    }
                }.runTaskLater(plugin, 5);
            }
        }.runTask(plugin);
    }

    class MailItemButton extends Button {
        private final ItemStack item;
        private final int index;
        public MailItemButton(ItemStack item, int index) {
            this.item = item;
            this.index = index;
        }

        @Override
        public ItemStack getIcon() {
            return item;
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            if(blockClicking) return;
            blockClicking = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(!InvClass.hasEnoughSpace(player.getInventory(), item)) {
                        Msg.send(player, "&c인벤토리에 공간이 부족하여 아이템을 수령할 수 없습니다.");
                        blockClicking = false;
                        return;
                    }
                    List<ItemStack> list = MailBoxManager.getMailData(player);
                    list.remove(index);
                    MailBoxManager.setMailData(player, list);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.getInventory().addItem(item);
                            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.2f);
                        }
                    }.runTask(plugin);

                    reloadGUI();
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    @Override
    public void disable() {
        mailBoxUsingPlayer.remove(player.getUniqueId().toString());
        super.disable();
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) {
            new MenuWindow(player, 1);
        }
    }
}
