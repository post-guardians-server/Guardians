package me.rukon0621.guardians.GUI;

import me.rukon0621.backpack.BackPackGUI;
import me.rukon0621.buff.RukonBuff;
import me.rukon0621.guardians.blueprint.BlueprintWindow;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.DamagingListener;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.party.PartyManager;
import me.rukon0621.guardians.region.Region;
import me.rukon0621.guardians.region.RegionManager;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.pay.blessing.BlessingWindow;
import me.rukon0621.pay.pass.PassWindow;
import me.rukon0621.rinstance.RukonInstance;
import me.rukon0621.rukonmarket.RukonMarket;
import me.rukon0621.sampling.windows.SamplingPacksWindow;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static me.rukon0621.guardians.main.pfix;

public class MenuWindow implements Listener {
    private static final String[] guiName = {"&f\uF000\uF001", "&f\uF000\uF021"};
    private static final int maxPage = 2;

    private final Player player;
    private InvClass inv;
    private int page;
    private boolean blockCloseEvent = false;

    public MenuWindow(Player player) {
        this(player, 1);
    }

    public MenuWindow(Player player, int page) {
        this.player = player;
        this.page = page;
        if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(player)) {
            Msg.warn(player, "이곳에서는 메뉴를 사용할 수 없습니다.", pfix);
            return;
        }
        for(Region region : RegionManager.getRegionsOfPlayer(player)) {
            if(region.getSpecialOptions().contains("blockMenu")) {
                Msg.warn(player, "이곳에서는 메뉴를 사용할 수 없습니다.", pfix);
                return;
            }
        }

        if(DamagingListener.getRemainCombatTime(player)!=-1) {
            Msg.warn(player, String.format("전투 중에는 메뉴를 사용할 수 없습니다! 전투 종료까지 &e%.1f초 &c남았습니다!", DamagingListener.getRemainCombatTime(player)));
            return;
        }
        RukonBuff.inst().getBuffManager().reloadBuffStats(player);
        openPage();
        main.getPlugin().getServer().getPluginManager().registerEvents(this, main.getPlugin());
    }

    private void setMenuItems(int slot, String name, @Nullable List<String> lore) {
        ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), name);
        item.setCustomModelData(7);
        if(lore!=null) {
            for(String s : lore) {
                item.addLore(s);
            }
        }
        inv.setslot(slot, item.getItem());
    }

    private void setMenuItems(int slot, String name) {
        setMenuItems(slot, name, null);
    }

    private void openPage() {
        inv = new InvClass(5, guiName[page - 1]);
        ItemClass item;
        item = new ItemClass(new ItemStack(Material.SCUTE), String.format("&9이전 페이지 &7( &f%d &7/ &f%d &7)", page, maxPage));
        item.setCustomModelData(7);
        inv.setslot(36, item.getItem());
        item.setName(String.format("&c다음 페이지 &7( &f%d &7/ &f%d &7)", page, maxPage));
        inv.setslot(44, item.getItem());

        if(page==1) {
            List<String> lores = new ArrayList<>();
            lores.add("&7레벨, 디나르, 루나르, 세부 능력치 및 장비를 확인합니다.");
            lores.add("&7내정보 창에서 좌측 &6? 아이콘&7을 클릭하여");
            lores.add("&7더 많은 정보를 조회할 수 있습니다.");
            if(!RukonBuff.inst().getBuffManager().getBuffs(player).isEmpty()) {
                lores.add(" ");
                lores.add("&e\uE011\uE00C\uE00C현재 적용중인 버프가 있습니다.");
            }
            setMenuItems(10, "#83bcfd내정보 및 장비", lores);
            setMenuItems(12, "#83bcfd퀘스트");
            setMenuItems(14, "#83bcfd스킬 트리");
            setMenuItems(16, "#83bcfd스킬 관리");

            PlayerData pdc = new PlayerData(player);
            lores = new ArrayList<>();
            lores.add("&7인벤토리가 꽉차면 드롭 아이템이 전리품 가방에 저장됩니다.");
            lores.add("&c쉬프트 좌클릭&6으로 가져올 수 있는 모든 아이템을 한 번에 가져옵니다.");
            lores.add(" ");
            lores.add(String.format("&7남은 용량: &f%d &7/ &e%d", pdc.getBackpackData().size(), pdc.getBackpackSlot()));
            setMenuItems(28, "#83bcfd전리품 가방", lores);
            setMenuItems(30, "#83bcfd메일함");
            setMenuItems(32, "#83bcfd장터");

            lores = new ArrayList<>();
            double partyBonus = PartyManager.getPartyBonus(player);
            if(partyBonus<-0.5) lores.add("&f파티를 맺고 파티 보너스를 받아보세요!");
            else {
            lores.add("&f파티 보너스 적용중!");
            lores.add(String.format("&7- 처치 기여도 배율: %.0f%%", partyBonus * 100));
            lores.add(" ");
            lores.add("&7파티의 인원이 많을수록 보너스가 커집니다!");
            }
            setMenuItems(34, "#83bcfd파티", lores);

        }
        else if (page==2) {
            PlayerData pdc = new PlayerData(player);
            List<String> lores = new ArrayList<>();
            if(pdc.getTitle()==null) lores.add("&7현재 장착된 칭호: 없음");
            else lores.add("&7현재 장착된 칭호: " + TitleWindow.getPureTitle(pdc.getTitle()));
            setMenuItems(10, "#83bcfd칭호", lores);
            setMenuItems(12, "#83bcfd신의 가호");
            setMenuItems(14, "#83bcfd가디언 패스");
            setMenuItems(16, "#83bcfd청사진 (설계도)");
            setMenuItems(28, "#83bcfd채팅 설정");
            setMenuItems(30, "#83bcfd샘플링");
            setMenuItems(32, "#83bcfd길드");
            setMenuItems(34, "#83bcfd무기 스킨");
        }
        blockCloseEvent = true;
        player.openInventory(inv.getInv());
        blockCloseEvent = false;
    }

    private void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if(!e.getWhoClicked().equals(player)) return;
        e.setCancelled(true);
        if(e.getRawSlot()==-999) {
            player.closeInventory();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
            return;
        }

        if(e.getCurrentItem()==null) return;

        if(e.getClick().equals(ClickType.DOUBLE_CLICK)) return;
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);

        if(e.getRawSlot()==36) {
            page--;
            if(page < 1) page = maxPage;
            openPage();
            return;
        }
        else if (e.getRawSlot()==44) {
            page++;
            if(page > maxPage) page = 1;
            openPage();
            return;
        }

        if(page==1) {
            if(e.getRawSlot()==10) EquipmentManager.openEquipmentGUI(player);
            else if(e.getRawSlot()==12) DialogQuestManager.openQuestList(player);
            else if(e.getRawSlot()==14) {

                if(EquipmentManager.getWeapon(player).getType().equals(Material.AIR)) {
                    Msg.warn(player, "스킬 트리를 열려면 무기를 장착해야합니다.");
                    return;
                }

                main.getPlugin().getSkillTreeManager().openSkillTree(player);
            }
            else if(e.getRawSlot()==16) SkillManager.openSkillEquipGUI(player);
            else if(e.getRawSlot()==28) {

                if(e.getClick().equals(ClickType.SHIFT_LEFT)) {
                    ListIterator<ItemStack> itr = new PlayerData(player).getBackpackData().listIterator();
                    while(itr.hasNext()) {
                        ItemStack item = itr.next();
                        if(!InvClass.hasEnoughSpace(player.getInventory(), item)) continue;
                        itr.remove();
                        player.getInventory().addItem(item);
                        new MenuWindow(player, 1);
                    }
                    player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
                    Msg.send(player, "아이템을 모두 꺼냈습니다.", pfix);
                }
                else new BackPackGUI(player);
            }
            else if(e.getRawSlot()==30) MailBoxManager.openMail(player);
            else if(e.getRawSlot()==32) {
                RukonMarket.inst().getMarketManager().openMarket(player);
            }
            else if(e.getRawSlot()==34) PartyManager.openPartyGUI(player);

        }
        else if (page==2) {
            if(e.getRawSlot()==10) new TitleWindow(player);
            else if(e.getRawSlot()==12) new BlessingWindow(player);
            else if(e.getRawSlot()==14) PassWindow.openPassWindow(player);
            else if(e.getRawSlot()==16) new BlueprintWindow(player);
            else if(e.getRawSlot()==28) {
                try {
                    new ChatSettingWindow(player);
                } catch (NoSuchFieldException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else if(e.getRawSlot()==30) {
                new SamplingPacksWindow(player);
            }
            else if(e.getRawSlot()==32) {
                Msg.warn(player, "준비중인 기능입니다.");
            }
            else if(e.getRawSlot()==34) {
                new WeaponSkinWindow(player);
            }
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
        if(!e.getPlayer().equals(player)) return;
        if(blockCloseEvent) return;
        if(!List.of(guiName).contains(Msg.recolor(e.getView().getTitle()))) return;
        disable();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if(e.getPlayer().equals(player)) disable();
    }

}
