package me.rukon0621.guardians.events;

import me.rukon0621.guardians.GUI.TitleWindow;
import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.dialogquest.Quest;
import me.rukon0621.guardians.dialogquest.QuestInProgress;
import me.rukon0621.guardians.helper.Broadcaster;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.LogInOutListener;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.offlineMessage.OfflineMessageManager;
import me.rukon0621.pay.PaymentData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static me.rukon0621.guardians.main.pfix;

public class WorldPeriodicEvent {
    private static final main plugin = main.getPlugin();

    public WorldPeriodicEvent() {

        new BukkitRunnable() {

            private int turn = 0;

            @Override
            public void run() {
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    BarManager.reloadBar(player);
                }
                turn++;
                if(turn % 4 == 0) {
                    turn = 0;
                    ZonedDateTime date = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                    secondEvent(date.getSecond());

                    if(date.getSecond()==0) {
                        minuteEvent();
                        //pvpEvent(date);
                        if(date.getMinute()==0) {
                            if(date.getHour()==0) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        for(Player player : plugin.getServer().getOnlinePlayers()) {
                                            dailyEvent(player);
                                        }
                                    }
                                }.runTask(plugin);
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 5);
    }

    private static void minuteEvent() {
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            OfflineMessageManager.readOfflineMessage(player);
        }
    }

    private static void secondEvent(int second) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    try {
                        if(player.getGameMode().equals(GameMode.SPECTATOR)) continue;
                        if(LogInOutListener.getLoadingPlayers().contains(player.getName())||LogInOutListener.getSavingPlayers().contains(player.getName())) continue;
                        if(player.isDead()) continue;
                        //10초 주기로 칭호 리로드
                        if(second % 10 == 0) TitleWindow.reloadTitleOfPlayer(player);
                        if(second % 2 == 0) {
                            double regen = Stat.REGEN.getTotal(player);
                            player.setHealth(Math.min(player.getHealth() + regen, player.getMaxHealth()));
                        }
                        player.setFoodLevel(20);
                    } catch (NullPointerException ignore) {
                    } catch (Exception e) {
                        e.printStackTrace();
                        Bukkit.getLogger().warning(player.getName() + " : Second Event 오류 발생" );
                    }
                }
            }
        }.runTask(plugin);
    }

    public static void dailyEvent(Player player) {
        PlayerData pdc = new PlayerData(player);
        PaymentData pyd = new PaymentData(player);
        pyd.setChangeLimit(0);
        if(pyd.getRemainOfJackBlessing() > 0) {
            if(pdc.getUnlearnChance() < 6) pdc.setUnlearnChance(6);
        }
        else {
            if(pdc.getUnlearnChance() < 3) pdc.setUnlearnChance(3);
        }

        Set<String> quests = DialogQuestManager.getCompletedQuests(player);
        Iterator<String> i = quests.iterator();
        while(i.hasNext()) {
            try {
                Quest q = DialogQuestManager.getQuestData().get(i.next());
                if(q.isGuardianQuest()) {
                    System.out.println("삭제된 퀘스트: " + q.getName());
                    i.remove();
                }
            } catch (Exception ignored) {
            }
        }
        ArrayList<QuestInProgress> qips = DialogQuestManager.getQuestsInProgress(player);
        //qips.removeIf(qip -> DialogQuestManager.getQuestData().get(qip.getName()).isGuardianQuest());

        Iterator<QuestInProgress> itr = qips.iterator();
        while (itr.hasNext()) {
            QuestInProgress q = itr.next();
            if(DialogQuestManager.getQuestData().get(q.getName()).isGuardianQuest()) {
                Bukkit.getLogger().severe("삭제된 QIP: " + q.getName());
                itr.remove();
            }
        }


        pdc.setFatigue(Math.min(0, pdc.getFatigue()));
        pdc.setEnergyCore(Math.max(pdc.getMaxEnergyCore(), pdc.getEnergyCore()));
        player.closeInventory();
        DialogQuestManager.setQuestInProgress(player, qips);
        DialogQuestManager.setCompletedQuests(player, quests);
        pdc.setDeathCount(0);
        pyd.setChangeLimit(0);
        Msg.send(player, " ");
        Msg.send(player, "&a하루가 지나 에너지 코어, 환전 가능 루나르, 스킬 초기화 횟수, 데스패널티 방지 횟수가 충전되고 피로도가 사라졌습니다!", pfix);
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    private static void pvpEvent(ZonedDateTime time) {
        return;
        /*
        if(time.getHour() < 11) return;
        if(time.getHour()%2==0) {
            if(time.getMinute()==0) {
                pvpEnable = true;
                Broadcaster.broadcastSyncWithSound("&cPVP가 시작되었습니다! &ePVP는 20분 동안 진행되며 특정 마을에 있는 NPC &b반격해&e에게서 진행할 수 있습니다!");
            }
            else if (time.getMinute()==10) {
                Broadcaster.broadcastSyncWithSound("&6PVP가 10분 뒤에 종료됩니다.");
            }
            else if (time.getMinute()==15) {
                Broadcaster.broadcastSyncWithSound("&6PVP가 5분 뒤에 종료됩니다.");
            }
            else if (time.getMinute()==17) {
                Broadcaster.broadcastSyncWithSound("&6PVP가 3분 뒤에 종료됩니다.");
            }
            else if (time.getMinute()==19) {
                Broadcaster.broadcastSyncWithSound("&6PVP가 1분 뒤에 종료됩니다.");
            }
            else if (time.getMinute()==20) {
                pvpEnable = false;
                Broadcaster.broadcastSyncWithSound("&6PVP가 종료되었습니다!");
            }
        }
        else {
            if (time.getMinute()==0) {
                Broadcaster.broadcast("&61시간 뒤에 PVP가 시작됩니다!");
            }
            else if (time.getMinute()==30) {
                Broadcaster.broadcast("&630분 뒤에 PVP가 시작됩니다!");
            }
            else if (time.getMinute()==50) {
                Broadcaster.broadcast("&610분 뒤에 PVP가 시작됩니다!");
            }
            else if (time.getMinute()==55) {
                Broadcaster.broadcastSyncWithSound("&65분 뒤에 PVP가 시작됩니다!");
            }
            else if (time.getMinute()==57) {
                Broadcaster.broadcastSyncWithSound("&63분 뒤에 PVP가 시작됩니다!");
            }
            else if (time.getMinute()==59) {
                Broadcaster.broadcastSyncWithSound("&c1분 뒤에 PVP가 시작됩니다!!");
            }
        }

         */

    }

}
