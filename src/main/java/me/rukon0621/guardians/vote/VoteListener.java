package me.rukon0621.guardians.vote;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.Broadcaster;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static me.rukon0621.guardians.main.pfix;

public class VoteListener implements PluginMessageListener {

    private Set<String> voteBlock = new HashSet<>();

    public VoteListener() {
        main plugin = main.getPlugin();
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, main.mainChannel, this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String s, @NotNull Player p, @NotNull byte[] bytes) {
        if(!s.equals(main.mainChannel)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subChannel = in.readUTF();
        if(!subChannel.equals("vote")) return;
        String user = in.readUTF();
        if(voteBlock.contains(user)) return;
        voteBlock.add(user);
        new BukkitRunnable() {
            @Override
            public void run() {
                voteBlock.remove(user);
            }
        }.runTaskLaterAsynchronously(main.getPlugin(), 100);
        System.out.println(user + "님이 서버를 추천했습니다.");
        Broadcaster.broadcast(user + "님이 서버를 추천해주셨습니다! 감사합니다!", false, false);
        Player player = Bukkit.getPlayerExact(user);
        if(player==null) return;
        ItemStack item = ItemSaver.getItem("추천상자").getItem().clone();
        MailBoxManager.giveOrMail(player, item);
        Msg.send(player, "추천 보상을 획득하셨습니다!", pfix);
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
        PlayerData pdc = new PlayerData(player);
        pdc.setVoteDays(pdc.getVoteDays() + 1);
        new VoteWindow(player, true);
    }
}
