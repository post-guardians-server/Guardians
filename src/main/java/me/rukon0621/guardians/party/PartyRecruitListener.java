package me.rukon0621.guardians.party;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.Couple;
import me.rukon0621.guardians.helper.DateUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.playavalon.avnparty.AvNParty;
import net.playavalon.avnparty.party.Party;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static me.rukon0621.guardians.main.getPlugin;
import static me.rukon0621.guardians.main.pfix;

public class PartyRecruitListener implements Listener {
    private static final AvNParty partyPlugin = AvNParty.plugin;
    private static final Map<Player, PartyRecruitListener> playerListeners = new HashMap<>();
    private static final Map<Player, Long> recruitCooltimeMap = new HashMap<>();

    public static PartyRecruitListener getPlayerListener(Player player) {
        return playerListeners.get(player);
    }

    private int stage = 1;
    private final Player player;
    private final Party party;
    private String partyTitle;
    private Couple<Integer, Integer> level;
    private boolean isEnd = false;
    private int timer = 19;
    private final Set<Player> waitingPlayers = new HashSet<>();

    public PartyRecruitListener(Player player) {
        this.player = player;
        if(partyPlugin.getParty(player)==null) {
            party = null;
            Msg.warn(player, "먼저 파티를 생성해야합니다.");
            new PartyCreateWindow(player);
            return;
        }
        party = partyPlugin.getParty(player);
        if(recruitCooltimeMap.getOrDefault(player, 0L) > System.currentTimeMillis()) {
            Msg.warn(player, "다음 모집 공고를 보내려면 &e" + DateUtil.formatDate((recruitCooltimeMap.get(player) - System.currentTimeMillis())/1000) + "&c를 기다려야합니다.");
            return;
        }
        playerListeners.put(player, this);
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
        Msg.send(player, " ");
        Msg.send(player, "&a파티의 소개 또는 목적&e을 간략히 입력해주세요.", pfix);
        Msg.send(player, "    &7\uE011\uE00C\uE00C&c/모집중단 &7명령어를 이용해 파티 모집을 중단할 수 있습니다.");
        Msg.send(player, "    &7\uE011\uE00C\uE00C파티모집은 3분 뒤 자동으로 종료됩니다.");
        main.getPlugin().getServer().getPluginManager().registerEvents(this, main.getPlugin());

    }

    @EventHandler
    public void onChatReceived(AsyncPlayerChatEvent e) {
        if(!e.getPlayer().equals(player)) return;
        e.setCancelled(true);

        if(stage==1) {
            if(e.getMessage().length()>30) {
                Msg.warn(player, "소개글이 너무 깁니다. 30자 이하로 기입해주세요.");
                return;
            }
            partyTitle = e.getMessage();
            Msg.send(player, " ");
            Msg.send(player, "&e원하는 레벨대를 입력해주세요. &7ex) 10~20", pfix);
            stage++;
        }
        else if (stage==2){
            try {
                if(!e.getMessage().contains("~")) {
                    throw new IllegalArgumentException();
                }
                 int num1, num2;
                 num1 = Integer.parseInt(e.getMessage().split("~")[0].trim());
                 num2 = Integer.parseInt(e.getMessage().split("~")[1].trim());
                 level = new Couple<>(Math.min(num1, num2), Math.max(num1, num2));
                 if(num1 < 0 || num1 > LevelData.maxLevel || num2 < 0 || num2 > LevelData.maxLevel) {
                     Msg.warn(player, "레벨 범위는 0에서 " + LevelData.maxLevel + " 사이로 지정해야합니다.");
                     return;
                 }
                 stage++;
                 send();
            } catch (NumberFormatException er) {
                Msg.warn(player, "올바른 숫자를 입력해주세요.");
            } catch (Exception er) {
                Msg.warn(player, "\"10~20\"과 같은 형식으로 작성해야합니다.");
            }
        }
    }

    public void join(Player target) {
        if(isEnd) {
            Msg.warn(target, "이미 모집이 종료되었습니다.");
            return;
        }
        if(party.getPlayers().size()==4) {
            Msg.warn(target, "이미 파티가 꽉 찼습니다.");
            return;
        }
        if(waitingPlayers.contains(target)) {
            Msg.warn(target, "이미 해당 플레이어에게 참가 신청을 보냈습니다.");
            return;
        }
        ItemData weapon = new ItemData(EquipmentManager.getItem(target, "무기"));
        if(weapon.getType()==null || weapon.getType().equals("null")) {
            Msg.warn(target, "파티 참가를 신청하려면 자신의 무기 타입 정보가 필요합니다. 무기를 장착해주세요.");
            return;
        }
        waitingPlayers.add(target);
        target.playSound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
        Msg.send(target, player.getName() + "님에게 파티 참가 신청을 보냈습니다!", pfix);

        int level = new PlayerData(target).getLevel();
        player.playSound(player, Sound.ENTITY_ENDER_DRAGON_HURT, 1, 0.5f);
        Msg.send(player, " ");
        Msg.send(player, String.format("&eLv.%d (%s) %s님&f에게서 파티 참가 신청이 들어왔습니다!", level, TypeData.getWeaponType(target), target.getName()), pfix);

//        //TODO: https://webui.advntr.dev/
//        Component component = MiniMessage.miniMessage().deserialize("<hover:show_text:'<yellow>클릭</yellow>하여 " + target.getName() + "님의 파티 참가 신청을 받습니다. <gray>/파티참가수락</gray> " + target.getName() + " <gray>명령어를 이용하셔도 됩니다.</gray>'><click:run_command:'/파티참가수락 " + target.getName() + "'><red><b>이곳</b></red>을 <yellow>클릭</yellow>하여 참가 요청을 수락하세요!</click></hover>");
//        Component component1 = MiniMessage.miniMessage().deserialize()

        Msg.send(player, new ComponentBuilder(Msg.color("&a&l이곳을 클릭&e하여 참가 요청을 수락하세요!"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Msg.color("&e클릭하여 " + target.getName() + "님의 파티 참가 신청을 받습니다. &7/파티참가수락 " + target.getName() + " 명령어를 이용하셔도 됩니다.")).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/파티참가수락 " + target.getName()))
                .create());
    }

    public void accepted(Player target) {
        if(isEnd) {
            Msg.warn(player, "파티 모집이 이미 만료되었습니다.");
            return;
        }
        else if(!waitingPlayers.contains(target)) {
            Msg.warn(player, "해당 플레이어에게서 온 참가 신청이 없습니다.");
            return;
        }
        else if(partyPlugin.getParty(target)!=null) {
            Msg.warn(player, target.getName() + "님은 이미 다른 파티에 속해 있습니다.");
            return;
        }
        int size = party.getPlayers().size();
        if(size==4) {
            Msg.warn(player, "이미 파티가 가득 찼습니다.");
            return;
        }
        if(size==3) end();
        waitingPlayers.remove(target);
        PartyManager.joinParty(partyPlugin.getAvalonPlayer(target), party);
    }

    private void send() {
        disable();
        recruitCooltimeMap.put(player, System.currentTimeMillis() + 180*1000L);
        for(Player lp : getPlugin().getServer().getOnlinePlayers()) {
            if(StoryManager.getPlayingStory(lp)!=null) continue;
            lp.playSound(lp, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);
            Msg.send(lp, " ");
            Msg.send(lp, "&7──────────────────────────");
            Msg.send(lp, "&6" + player.getName() + "&7님이 파티를 모집하고 있습니다!", pfix);
            Msg.send(lp, "&7   모집 내용: " + partyTitle);
            Msg.send(lp, "&7   권장 레벨: " + level.getFirst() + " ~ " + level.getSecond());
            Msg.send(lp, new ComponentBuilder(Msg.color("    &a&l이곳&7을 클릭하여 파티 참여를 요청하세요! 또는 &e/파티참여 " + player.getName() + " &7명령어를 이용해 파티에 참여할 수 있습니다."))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Msg.color("&e클릭하여 참여를 요청합니다.")).create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/파티참여 " + player.getName()))
                    .create());
            Msg.send(lp, "&7──────────────────────────");
        }
        Msg.send(player, " ");
        Msg.send(player, "&c/모집중단 &e명령어를 이용해 언제든지 파티 모집을 중단할 수 있습니다. &7(파티모집은 3분 뒤 자동으로 종료됩니다.)", pfix);
        new BukkitRunnable() {
            @Override
            public void run() {
                timer--;
                if(isEnd) {
                    cancel();
                    return;
                }
                if(timer==0) {
                    cancel();
                    disable();
                    end();
                    Msg.warn(player, "시간이 만료되어 파티 모집이 중단되었습니다.");
                }
            }
        }.runTaskTimerAsynchronously(main.getPlugin(), 0, 200);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if(!e.getPlayer().equals(player)) return;
        end();
        disable();
    }

    public void end() {
        isEnd = true;
        waitingPlayers.clear();
        playerListeners.remove(player);
    }

    public void disable() {
        HandlerList.unregisterAll(this);
    }

}
