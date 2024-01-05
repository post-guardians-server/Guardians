package me.rukon0621.guardians.party;


import me.rukon0621.dungeonwave.RukonWave;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.ChatEventListener;
import me.rukon0621.guardians.main;
import me.rukon0621.rinstance.RukonInstance;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.playavalon.avnparty.AvNParty;
import net.playavalon.avnparty.party.Party;
import net.playavalon.avnparty.player.AvalonPlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

import static me.rukon0621.guardians.main.pfix;

public class PartyManager implements Listener {
    private static final AvNParty partyPlugin = AvNParty.plugin;
    private static HashMap<Player, Long> partyInviteWaiting;
    private static HashMap<Player, Party> invitedParty;

    public PartyManager() {
        main plugin = main.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        partyInviteWaiting = new HashMap<>();
        invitedParty = new HashMap<>();
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            partyInviteWaiting.put(player, 0L);
        }
    }

    /**
     * @param player player
     * @return 플레이어에게 적용중인 기여도 보너스를 보여줌 (인당 5%) (0~1사이의 값으로 반환, 파티가 없으면 -1 반환)
     */
    public static double getPartyBonus(Player player) {
        Party party = partyPlugin.getParty(player);
        if(party==null) {
            return -1;
        }
        return (party.getPlayers().size()-1) * 0.05;
    }

    public static void openPartyGUI(Player player) {
        Party party = partyPlugin.getParty(player);
        if(party==null) {
            new PartyCreateWindow(player);
        }
        else {
            new PartyWindow(player, party);
        }
    }

    /**
     *
     * @param player playerr
     * @return 현재 파티 초대를 대기받은 상황인지 반환
     */
    private static boolean isBeingInviting(Player player) {
        return partyInviteWaiting.get(player) > System.currentTimeMillis();
    }

    /**
     * 특정 플레이어에게 파티 요청을 보냄
     * @param inviter 초대자
     * @param target 초대를 받을 사람
     * @param party 초대자의 파티
     */
    public static void inviteParty(Player inviter, Player target, Party party) {
        if(party.getPlayers().size()==4) {
            Msg.send(inviter, "&c이미 파티원이 가득 찼습니다.", pfix);
            return;
        }
        if(isBeingInviting(target)) {
            Msg.send(inviter, "&c해당 플레이어는 이미 다른 요청을 받은 상태입니다.", pfix);
            return;
        }
        AvalonPlayer avnTarget = partyPlugin.getAvalonPlayer(target);
        if(avnTarget.getParty()!=null) {
            Msg.send(inviter, "&c해당 플레이어는 이미 다른 파티에 속한 상태입니다.", pfix);
            return;
        }

        partyInviteWaiting.put(target, System.currentTimeMillis() + 15000L);
        invitedParty.put(target, party);
        Msg.send(inviter, "&e성공적으로 파티 요청을 보냈습니다.",pfix);
        Msg.send(target, new ComponentBuilder(Msg.color(pfix + "&e"+inviter.getName()+"&6님에게서 파티 요청이 왔습니다. "))
                .append(Msg.color("&a&l이곳&e을 클릭하거나 &a/파티수락 &e명령어를 입력&6하여 파티를 받으세요!"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Msg.color("&e클릭하여 파티 초대를 수락합니다.")).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/파티수락"))
                .create());
        target.playSound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
        inviter.playSound(inviter, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
    }

    public static void acceptParty(Player player) {
        if(!isBeingInviting(player)) {
            Msg.send(player, "존재하는 파티 요청이 없거나 만료되었습니다.", pfix);
            return;
        }
        Party party = partyPlugin.getParty(player);
        if(party!=null) {
            Msg.warn(player, "이미 파티에 가입되어 있습니다.");
            return;
        }
        party = invitedParty.get(player);

        Player leader = party.getLeader().getPlayer();

        for(AvalonPlayer p : party.getPlayers()) {
            if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(p.getPlayer())) {
                Msg.warn(player, "해당 파티는 현재 테세이온을 플레이하고 있습니다.", pfix);
                partyInviteWaiting.put(player, 0L);
                return;
            }
        }

        if(RukonWave.inst().getFieldWaveManager().isPlayingFieldWave(leader)) {
            Msg.warn(player, "해당 파티는 현재 필드 웨이브를 플레이하고 있습니다.", pfix);
            partyInviteWaiting.put(player, 0L);
            return;
        }

        if(party.getPlayers().size()==4) {
            Msg.warn(player, "이미 해당 파티의 인원이 꽉 찼습니다.", pfix);
            partyInviteWaiting.put(player, 0L);
            return;
        }
        joinParty(partyPlugin.getAvalonPlayer(player), party);
        openPartyGUI(player);
    }

    public static void joinParty(AvalonPlayer avnP, Party party) {
        avnP.setParty(party);
        party.getPlayers().add(avnP);
        for(AvalonPlayer loopAvnP : party.getPlayers()) {
            Player player = loopAvnP.getPlayer();
            Msg.send(player, "&f새로운 파티원 &e"+avnP.getPlayer().getName()+"&f님이 파티에 참가했습니다.", pfix);
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.3f);
            PartyWindow.reloadIfUsing(player);
        }
        Player player = avnP.getPlayer();
        ChatEventListener.getPlayerChatCache(player).add(ChatEventListener.ChatChannel.PARTY.getStr());

        PartyRecruitListener listener = PartyRecruitListener.getPlayerListener(player);
        if(listener!=null) {
            listener.end();
            Msg.warn(player, "다른 파티에 들어가 모집이 중단되었습니다.");
        }
    }

    private static void quitParty(AvalonPlayer avnP) {
        quitParty(avnP, false);
    }
    public static void quitParty(AvalonPlayer avnP, boolean kicked) {
        //파티에 1명밖에 없으면 해산
        Player player = avnP.getPlayer();
        ChatEventListener.getPlayerChatCache(player).remove("party");
        if(ChatEventListener.getPlayerChatChannel(player).equals(ChatEventListener.ChatChannel.PARTY)) ChatEventListener.setPlayerChatChannel(avnP.getPlayer(), ChatEventListener.ChatChannel.ALL);
        Party party = avnP.getParty();
        boolean leader = party.getLeader().equals(avnP);

        if(party.getPlayers().size()==1) {
            avnP.setParty(null);
            party.getPlayers().remove(avnP);
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
            Msg.send(player, "&e파티를 해산하였습니다.",pfix);
            new PartyCreateWindow(player);
            return;
        }

        //파티 리더 변경
        avnP.setParty(null);
        party.getPlayers().remove(avnP);
        if(leader) {
            for(AvalonPlayer loopAvnP : party.getPlayers()) {
                party.setLeader(loopAvnP.getPlayer());
                break;
            }
        }

        //파티 추방
        if(kicked) {
            Msg.send(player, "&c파티 리더에 의해 파티에서 추방 당했습니다.",pfix);
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
            for(AvalonPlayer loopAvnP : party.getPlayers()) {
                Player loopPlayer = loopAvnP.getPlayer();
                Msg.send(loopPlayer, "&6"+player.getName()+"&e님이 파티 리더에 의해 파티에서 추방 당했습니다.",pfix);
                player.playSound(loopPlayer, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
                PartyWindow.reloadIfUsing(loopPlayer);
            }
        }
        //파티 탈퇴
        else {
            Msg.send(player, "&e파티를 탈퇴하였습니다.",pfix);
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
            openPartyGUI(player);
            for(AvalonPlayer loopAvnP : party.getPlayers()) {
                Player loopPlayer = loopAvnP.getPlayer();
                Msg.send(loopPlayer, "&6"+player.getName()+"&e님이 파티를 탈퇴 하였습니다.",pfix);
                if(leader) {
                    Msg.send(loopPlayer, "&6"+party.getLeader().getPlayer().getName()+"&e님이 새로운 파티장이 되었습니다.",pfix);
                }
                player.playSound(loopPlayer, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
                PartyWindow.reloadIfUsing(loopPlayer);
            }
        }

        PartyRecruitListener listener = PartyRecruitListener.getPlayerListener(player);
        if(listener!=null) {
            listener.end();
            Msg.warn(player, "파티가 사라져 모집이 중단되었습니다.");
        }
    }

    public static void changeLeader(Party party, Player newLeader) {
        party.setLeader(newLeader);
        for(AvalonPlayer avnP : party.getPlayers()) {
            Player player = avnP.getPlayer();
            Msg.send(player, "&e파티의 리더가 &6"+newLeader.getName()+"&e님으로 교체되었습니다.", pfix);
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.3f);
            PartyWindow.reloadIfUsing(player);
        }
    }

    public static void createNewParty(Player player) {
        AvalonPlayer avnP = partyPlugin.getAvalonPlayer(player);
        avnP.setParty(new Party(player));
        openPartyGUI(player);
        ChatEventListener.getPlayerChatCache(player).add(ChatEventListener.ChatChannel.PARTY.getStr());
        Msg.send(player, "새로운 파티를 생성했습니다!", pfix);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        AvalonPlayer avnP = partyPlugin.getAvalonPlayer(e.getPlayer());
        if(avnP.getParty()!=null) {
            quitParty(avnP);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        partyInviteWaiting.put(player, 0L);
    }

}
