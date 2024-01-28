package me.rukon0621.guardians.GUI;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.main;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.Window;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.listeners.LogInOutListener.dataCategories;
import static me.rukon0621.guardians.main.getPlugin;
import static me.rukon0621.guardians.main.mainChannel;

public class ChannelWindow extends Window implements PluginMessageListener {
    private final int maxPerChannel;

    public ChannelWindow(Player player) {
        super(player, "&f\uF000", 3);
        maxPerChannel = Bukkit.getMaxPlayers();
        main.getPlugin().getServer().getMessenger().registerIncomingPluginChannel(main.getPlugin(), mainChannel, this);
        player.closeInventory();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("getServers");
        out.writeUTF(player.getName());
        player.sendPluginMessage(main.getPlugin(), mainChannel, out.toByteArray());

    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
            player.closeInventory();
        }
    }

    @Override
    public void disable() {
        super.disable();
        main.getPlugin().getServer().getMessenger().unregisterIncomingPluginChannel(main.getPlugin(), mainChannel, this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String s, @NotNull Player useless, @NotNull byte[] bytes) {
        if(!s.equalsIgnoreCase(mainChannel)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        if(!in.readUTF().equalsIgnoreCase("showServers")) return;
        if(!this.player.getName().equalsIgnoreCase(in.readUTF())) return;
        int slot = 0;
        while (true) {
            String data = in.readUTF();
            if(data.equalsIgnoreCase("end")) break;
            if(!(data.contains("ch") || data.contains("testServer"))&&!this.player.isOp()) continue;
            map.put(slot, new ServerButton(data));
            slot++;
        }
        if(LogInOutListener.getLoadingPlayers().contains(player.getName())||LogInOutListener.getSavingPlayers().contains(player.getName())) {
            Msg.warn(player, "몇 초 후에 다시 시도해주세요.");
            disable();
            player.closeInventory();
            return;
        }
        CountDownLatch latch = new CountDownLatch(dataCategories);
        new BukkitRunnable() {
            @Override
            public void run() {
                LogInOutListener.saveAllDataToDB(player, latch);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        reloadGUI();
                        open();
                    }
                }.runTask(getPlugin());
            }
        }.runTaskAsynchronously(main.getPlugin());

    }

    class ServerButton extends Button {
        private final String serverName;
        private final int players;

        public ServerButton(String s) {
            serverName = s.split(":")[0];
            players = Integer.parseInt(s.split(":")[1]);
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            if(LogInOutListener.getLoadingPlayers().contains(player.getName())||LogInOutListener.getSavingPlayers().contains(player.getName())) {
                Msg.warn(player, "몇 초 후에 다시 시도해주세요.");
                disable();
                player.closeInventory();
                return;
            }

            if(players>=maxPerChannel) {
                Msg.warn(player, "이 채널은 이미 꽉 찼습니다. 다른 채널을 이용해주세요.");
                return;
            }

            if(serverName.contains("test")) {
                if(new PlayerData(player).getLevel() < 24) {
                    Msg.warn(player, "24레벨 이상만 테스트 서버를 진행할 수 있습니다.");
                    return;
                }
            }

            disable();
            player.closeInventory();

            //이동 시도
            PluginMessageListener listener = new PluginMessageListener() {
                @Override
                public void onPluginMessageReceived(@NotNull String s, @NotNull Player player, @NotNull byte[] bytes) {
                    main.getPlugin().getServer().getMessenger().unregisterIncomingPluginChannel(main.getPlugin(), mainChannel, this);
                    ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
                    String subCh = in.readUTF();
                    if(!player.getName().equals(in.readUTF())) return;
                    if(subCh.equalsIgnoreCase("serverEmptyCheck")) {
                        player.setHealth(player.getMaxHealth());
                        LogInOutListener.addLogoutBlock(player);
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("connect");
                        out.writeUTF(player.getName());
                        out.writeUTF(serverName);
                        player.sendPluginMessage(main.getPlugin(), mainChannel, out.toByteArray());
                    }
                    else if (subCh.equalsIgnoreCase("serverEmptyNoCheck")) {
                        Msg.warn(player, "서버가 가득차서 이동할 수 없습니다.");
                    }
                }
            };
            main.getPlugin().getServer().getMessenger().registerIncomingPluginChannel(main.getPlugin(), mainChannel, listener);
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("serverEmptyAsk");
            out.writeUTF(serverName);
            out.writeUTF(String.valueOf(maxPerChannel));
            out.writeUTF(player.getName());
            player.sendPluginMessage(main.getPlugin(), mainChannel, out.toByteArray());
        }

        @Override
        public ItemStack getIcon() {
            ItemClass item = new ItemClass(new ItemStack(Material.OAK_SIGN), "&e 『 &6" + serverName.replaceAll("ch","").trim() + " 채널 &e』");
            item.addLore("&f이 채널의 인원: " + players + " / " + maxPerChannel);
            return item.getItem();
        }
    }
}
