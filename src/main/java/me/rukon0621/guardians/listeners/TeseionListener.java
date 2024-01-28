package me.rukon0621.guardians.listeners;

import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.dropItem.DropManager;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.teseion.Teseion;
import me.rukon0621.teseion.event.TeseionClearEvent;
import me.rukon0621.teseion.event.TeseionFailEvent;
import net.playavalon.avnparty.party.Party;
import net.playavalon.avnparty.player.AvalonPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class TeseionListener implements Listener {
    private static final main plugin = main.getPlugin();

    public TeseionListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onTeseionClear(TeseionClearEvent e) {
        if(e.isWithOutEffect()) return;
        Teseion teseion = e.getTeseion();
        Party party  = e.getInstance().getParty();
        int rewardLevel, leaderRewardLevel;
        String leaderReward = "null", reward = "null";
        Player leader = party.getLeader().getPlayer();
        String finalReward = reward;
        try {
            reward = teseion.getRewardName().split(":")[0];
            if(!reward.equals("null")) {
                rewardLevel = Integer.parseInt(teseion.getRewardName().split(":")[1]);
            }
            else rewardLevel = 1;
            if(!teseion.getLeaderReward().equals("null")) {
                leaderReward = teseion.getLeaderReward().split(":")[0];
                leaderRewardLevel = Integer.parseInt(teseion.getLeaderReward().split(":")[1]);
            }
            else {
                leaderRewardLevel = 1;
            }
        } catch (NumberFormatException er) {
            rewardLevel = 1;
            leaderRewardLevel = 1;
        }
        ArrayList<ItemStack> leaderDrops = DropManager.getDropList(leader, leaderReward, leaderRewardLevel ,1);
        for(AvalonPlayer avnP : party.getPlayers()) {
            Player player = avnP.getPlayer();
            ArrayList<ItemStack> drops = DropManager.getDropList(player, reward, rewardLevel, 1);
            Msg.send(player, " ");
            Msg.send(player, " ");
            Msg.send(player, "&7┌────────────────────────┐");
            Msg.send(player, " ");
            Msg.send(player, "&7[ &f"+teseion.getDisplayName()+" &7]");
            Msg.send(player, "  &f테세이온을 클리어했습니다!");

            if(!reward.equals("null")) {
                Msg.send(player, " ");
                Msg.send(player, "&7[ &f테세이온 보상 &7]");

                for(ItemStack item : drops) {
                    try {
                        Msg.send(player, String.format("  &7- %s &8(x%d)", item.getItemMeta().getDisplayName(), item.getAmount()));

                    } catch (NullPointerException er) {
                        Msg.send(player, String.format("  &7- %s &8(x%d)", item.getType(), item.getAmount()));
                    }
                }
            }

            if(!leaderReward.equals("null")) {
                Msg.send(player, " ");
                Msg.send(player, "  &e이 테세이온은 파티 리더 보상이 존재하는 테세이온으로 파티 리더에게 특별 보상이 지급되었습니다.");
                Msg.send(player, "  &7[ &f파티 리더 보상 &7]");
                for(ItemStack item : leaderDrops) {
                    try {
                        Msg.send(player, String.format("  &7- %s &8(x%d&)", item.getItemMeta().getDisplayName(), item.getAmount()));

                    } catch (NullPointerException er) {
                        Msg.send(player, String.format("  &7- %s &8(x%d&)", item.getType(), item.getAmount()));
                    }
                }
            }
            Msg.send(player, " ");
            Msg.send(player, "&7└────────────────────────┘");
            MailBoxManager.giveAllOrMailAll(player, drops);

            if(teseion.getName().equals("windroad")) {
                DialogQuestManager.completeCustomObject(player, "바람길목 테세이온을 격파하자");
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    StringBuilder sb = new StringBuilder(finalReward);
                    boolean first = true;
                    sb.append("[");
                    for(ItemStack item : drops) {
                        if(first) first = false;
                        else sb.append(", ");
                        sb.append(new ItemData(item));
                    }
                    sb.append("]");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            LogManager.log(player, "drop", sb.toString());
                            LogManager.log(player, "테세이온", e.getTeseion().getName());
                        }
                    }.runTask(plugin);
                }
            }.runTaskAsynchronously(plugin);
        }
        MailBoxManager.giveAllOrMailAll(leader, leaderDrops);
        LogManager.stat(leader, "테세이온", e.getTeseion().getName(), 1);


    }

    @EventHandler
    public void onTeseionFail(TeseionFailEvent e) {
        if(e.getTeseion().getName().equals("windroad")) {
            Player player = e.getInstance().getParty().getLeader().getPlayer();
            if(StoryManager.isRead(player, "벤터스_고블린실패")) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    StoryManager.readStory(player, "벤터스_고블린실패");
                }
            }.runTaskLater(main.getPlugin(), 40);
        }

    }

}
