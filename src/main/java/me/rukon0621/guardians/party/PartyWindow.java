package me.rukon0621.guardians.party;

import me.rukon0621.guardians.GUI.ChatSettingWindow;
import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.Window;
import net.playavalon.avnparty.AvNParty;
import net.playavalon.avnparty.party.Party;
import net.playavalon.avnparty.player.AvalonPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.rukon0621.guardians.main.pfix;

public class PartyWindow extends Window {
    private static final String partyManagementGuiName = "&f\uF000\uF01F";
    private static final AvNParty partyPlugin = (AvNParty) Bukkit.getPluginManager().getPlugin("DungeonParties");
    private static final Map<Player, PartyWindow> usingPartyWindow = new HashMap<>();

    public static boolean isUsingPartyWindow(Player player) {
        return usingPartyWindow.containsKey(player);
    }

    /**
     * 해당 플레이어가 파티창을 키고 있으면 party window reload
     * @param player player
     */
    public static void reloadIfUsing(Player player) {
        if(!usingPartyWindow.containsKey(player)) return;
        usingPartyWindow.get(player).reloadGUI();
    }

    private final Party party;
    private boolean isLeader;

    public PartyWindow(Player player, Party party) {
        super(player, partyManagementGuiName, 4);
        this.party = party;
        usingPartyWindow.put(player, this);
        reloadGUI();
        open();
    }

    @Override
    protected void reloadGUI() {
        inv.getInv().clear();
        this.isLeader = party.getLeader().equals(partyPlugin.getAvalonPlayer(player));
        List<AvalonPlayer> pls = party.getPlayers();
        for(int i = 0; i < 4; i++) {
            if(pls.size() - 1 < i) map.put(10 + i*2, new PartyMemberButton(null));
            else map.put(10 + i*2, new PartyMemberButton(pls.get(i).getPlayer()));
        }

        map.put(28, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                try {
                    new ChatSettingWindow(player);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&f파티 채팅 설정");
                item.setCustomModelData(7);
                item.addLore("&f채팅 설정 창을 엽니다.");
                return item.getItem();
            }
        });
        map.put(34, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(clickType.equals(ClickType.SHIFT_LEFT) && isLeader) {
                    player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1.5f);
                    List<AvalonPlayer> avnPs = new ArrayList<>(party.getPlayers());
                    for(AvalonPlayer avnP : avnPs) {
                        Player avp = avnP.getPlayer();
                        avnP.setParty(null);
                        party.getPlayers().remove(avnP);
                        Msg.send(avp, "&c파티의 리더가 파티를 해산시켰습니다.", pfix);
                        avp.playSound(avp, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0.5f);
                        if(!usingPartyWindow.containsKey(avp)) continue;
                        new PartyCreateWindow(avp);
                    }
                    return;
                }

                PartyManager.quitParty(partyPlugin.getAvalonPlayer(player), false);
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);

            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&c파티 나가기");
                item.setCustomModelData(7);
                item.addLore("&f클릭하여 파티에서 나갑니다.");
                if(isLeader) {
                    item.addLore(" ");
                    item.addLore("&4쉬프트+좌클릭&c으로 파티를 해산시킵니다.");
                }
                return item.getItem();
            }
        });


        super.reloadGUI();
    }

    class PartyMemberButton extends Button  {
        private final Player member;
        private final boolean isLeader;

        public PartyMemberButton(@Nullable Player member) {
            this.member = member;
            if(member==null) {
                isLeader = false;
                return;
            }
            isLeader = party.getLeader().equals(partyPlugin.getAvalonPlayer(member));
        }

        @Override
        public void execute(Player player, ClickType clickType) {

            if(member==null) return;

            if(player.equals(member)) return;

            if(!PartyWindow.this.isLeader) return;
            if(clickType.equals(ClickType.SHIFT_LEFT)) {
                PartyManager.quitParty(partyPlugin.getAvalonPlayer(member),true);
                player.playSound(player, Sound.BLOCK_FIRE_EXTINGUISH, 1, 1.3f);
            }
            else if (clickType.equals(ClickType.SHIFT_RIGHT)) {
                PartyManager.changeLeader(party, member);
            }

        }

        @Override
        public ItemStack getIcon() {
            if(member==null) {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&e/파티초대 <플레이어> &f명령어로 친구를 초대하거나");
                item.addLore("&c/파티모집 &f명령어로 파티원을 찾아보세요!");
                item.setCustomModelData(29);
                return item.getItem();
            }

            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(member);
            if(isLeader) meta.setDisplayName(Msg.color("&a"+member.getName()+" &7 [ LEADER ]"));
            else {
                meta.setDisplayName(Msg.color("&f"+member.getName()));
                if(PartyWindow.this.isLeader) {
                    List<String> lores = new ArrayList<>();
                    lores.add(Msg.color("&c쉬프트+좌클릭&6으로 플레이어를 추방합니다."));
                    lores.add(Msg.color("&b쉬프트+우클릭&3으로 플레이어를 파티 리더 권한을 위임합니다."));
                    meta.setLore(lores);
                }
            }
            item.setItemMeta(meta);
            return item;
        }
    }

    @Override
    public void disable() {
        usingPartyWindow.remove(player);
        super.disable();
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) new MenuWindow(player,1);
    }
}
