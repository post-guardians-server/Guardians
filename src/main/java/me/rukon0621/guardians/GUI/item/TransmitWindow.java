package me.rukon0621.guardians.GUI.item;

import me.rukon0621.callback.speaker.SpeakerListenEvent;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.SystemEventsListener;
import me.rukon0621.gui.windows.util.ChestWindow;
import me.rukon0621.rkutils.bukkit.util.runnable.AsyncSwitcher;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TransmitWindow extends ChestWindow {

    private static final Set<UUID> blockedPlayer = new HashSet<>();

    public static void openTransmitWindow(Player player) {
        if(SystemEventsListener.transmitChestBlocked) {
            Msg.warn(player, "전송 상자로 아이템이 옮겨지고 있습니다! 잠시 기다려주세요.");
            return;
        }
        if(!blockedPlayer.add(player.getUniqueId())) {
            Msg.warn(player, "잠시 기다리고 다시 시도해주세요.");
            return;
        }
        new AsyncSwitcher() {
            private Map<Integer, ItemStack> items;

            @Override
            public void executeInAsync() {
                items = new PlayerData(player).getTransmitData();
            }

            @Override
            public void afterSync() {
                blockedPlayer.remove(player.getUniqueId());
                new TransmitWindow(player, items);
            }
        };
    }

    private TransmitWindow(Player player, @Nullable Map<Integer, ItemStack> items) {
        super(player, "&f\uF000", 3, items);
        reloadGUI();
        open();
    }

    @EventHandler
    public void onSpeakListen(SpeakerListenEvent e) {
        if(!(e.getMainAction().equalsIgnoreCase("transmitChestStart"))) return;
        Msg.warn(player, "전송 상자로 아이템이 옮겨지고 있습니다! 잠시 후 상자를 다시 열어주세요.");
        player.closeInventory();
    }

    @Override
    public void select(int slot) {
        if(slot >= 27) return;
        super.select(slot);
    }

    @Override
    public @NotNull Set<Integer> getBlockedSlots() {
        return null;
    }

    @Override
    public void closed(Map<Integer, ItemStack> map, boolean b) {
        blockedPlayer.add(player.getUniqueId());
        disable();
        if(b) player.closeInventory();
        new AsyncSwitcher() {
            @Override
            public void executeInAsync() {
                new PlayerData(player).setTransmitData(map);
            }

            @Override
            public void afterSync() {
                blockedPlayer.remove(player.getUniqueId());
            }
        };
    }
}
