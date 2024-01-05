package me.rukon0621.guardians.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.Couple;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxWindow;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import static me.rukon0621.guardians.main.*;

public class ProxyListener implements PluginMessageListener {

    public ProxyListener() {
        main plugin = main.getPlugin();
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, chatChannel, this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, mainChannel, this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if(channel.equals(mainChannel)) {
            if(subChannel.equals("ptp")) {
                //player, targetPlayer (proxiedTeleport Map)
                LogInOutListener.getPtpMap().put(in.readUTF(), new Couple<>(in.readUTF(), Boolean.parseBoolean(in.readUTF())));
            }
            else if(subChannel.equals("mailReceived")) {
                Player target = Bukkit.getPlayer(in.readUTF());
                if(target == null) return;
                target.playSound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
                Msg.send(target, "메일에 새로운 아이템이 도착했습니다!", pfix);
                if(!MailBoxWindow.getMailBoxUsingPlayer().contains(target.getUniqueId().toString())) return;
                target.closeInventory();
            }
            return;
        }

        if(subChannel.equals("connectionCancelled")) {
            Msg.warn(player, "해당 채널로 이동할 수 없습니다.", pfix);
            PlayerData.setPlayerStun(player, false);
        }


    }
}
