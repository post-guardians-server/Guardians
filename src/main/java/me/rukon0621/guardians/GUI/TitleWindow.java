package me.rukon0621.guardians.GUI;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.DateUtil;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.windows.Window;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static me.rukon0621.guardians.main.getPlugin;
import static me.rukon0621.guardians.main.pfix;

public class TitleWindow extends Window {

    public static final String timeSpliter = "!tm!";
    public static final String timeSpliterAbb = "!!";

    public static void addTitle(@Nullable Player executor, Player target, String pureTitle, double day) {
        PlayerData pdc = new PlayerData(target);

        for(String loopTitle : pdc.getTitles()) {
            if(Msg.uncolor(Msg.color(TitleWindow.getPureTitle(loopTitle))).equals(Msg.uncolor(Msg.color(pureTitle)))) {
                if(executor == null) Msg.warn(target, "이미 이 칭호를 가지고 있습니다.");
                else Msg.warn(executor, "해당 플레이어는 이미 이 칭호를 가지고 있습니다.");
                return;
            }
        }

        if(day == -1) {
            pdc.getTitles().add(pureTitle);
        }
        else {
            long due = (long) (day * 86400 * 1000L + System.currentTimeMillis());
            String fullTitle = pureTitle + timeSpliter + due;
            pdc.getTitles().add(fullTitle);
        }
        pdc.getTitles().sort(String::compareTo);
        if(executor != null) {
            Msg.send(executor, "성공적으로 새로운 칭호를 추가하였습니다.", pfix);
        }
    }
    public static void removeTitle(@Nullable Player executor, Player target, String title) {
        String titleName = Msg.uncolor(Msg.color(StringEscapeUtils.unescapeJava(title)));
        PlayerData pdc = new PlayerData(target);
        for(String loopTitle : pdc.getTitles()) {
            if(!Msg.uncolor(Msg.color(loopTitle)).startsWith(titleName)) continue;
            pdc.getTitles().remove(loopTitle);
            if(loopTitle.equals(pdc.getTitle())) pdc.setTitle(null);
            if(executor != null) Msg.send(executor, getPureTitle(loopTitle) + " - 이 칭호를 성공적으로 삭제했습니다.", pfix);
            return;
        }
        if(executor != null) Msg.warn(executor, "해당 플레이어는 " + titleName + "(으)로 시작되는 칭호를 가지고 있지 않습니다.");
    }

    public static void reloadTitleOfPlayer(Player player) {
        PlayerData pdc = new PlayerData(player);
        String fullTitle = pdc.getTitle();
        if(!isExpired(fullTitle)) return;
        pdc.setTitle(null);
        Msg.send(player, getPureTitle(fullTitle) + " &c - 이 칭호의 기간이 만료되어 자동으로 삭제되었습니다.", pfix);
        player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1.5f);
    }


    /**
     *
     * @param title 칭호 full name
     * @return 칭호의 기간을 제외한 순수 이름을 불러옴
     */
    public static String getPureTitle(String title) {
        return title.split(timeSpliter)[0];
    }

    /**
     *
     * @param title 칭호 full name
     * @return 만료되는 밀리초를 반환 기간제 칭호가 아니면 현재 시각을 반환
     */
    public static long getDueMillis(String title) {
        if(!isPeriodic(title)) return System.currentTimeMillis();
        return Long.parseLong(title.split(timeSpliter)[1]);
    }
    /**
     *
     * @param title 칭호 full name
     * @return 만료까지 남은 밀리초를 반환, 기간제 칭호가 아니면 0 반환
     */
    public static long getRemainMillis(String title) {
        return getDueMillis(title) - System.currentTimeMillis();
    }

    /**
     *
     * @param title 칭호 full name
     * @return 해당 칭호가 기간제 칭호인지 반환
     */
    public static boolean isPeriodic(String title) {
        return title.contains(timeSpliter);
    }

    /**
     *
     * @param title 칭호 full name
     * @return 해당 칭호가 만료되었는지 반환, 기간제 칭호가 아니면 무조건 false 반환
     */
    public static boolean isExpired(String title) {
        if(isPeriodic(title)) return getRemainMillis(title) < 0;
        return false;
    }

    private TitleButton equippedTitle;

    public TitleWindow(Player player) {
        super(player, "&f\uF000", 6);
        PlayerData pdc = new PlayerData(player);
        int slot = 0;

        Iterator<String> itr = pdc.getTitles().iterator();
        while(itr.hasNext()) {
            String title = itr.next();
            //만료된 기간제 칭호는 자동 삭제
            if(isExpired(title)) {
                itr.remove();
                continue;
            }
            map.put(slot, new TitleButton(title, title.equals(pdc.getTitle())));
            slot++;
        }
        reloadGUI();
        open();
    }

    class TitleButton extends Button {
        private boolean isEquipped;
        private final String fullTitle;
        public TitleButton(String fullTitle, boolean isEquipped) {
            this.fullTitle = fullTitle;
            this.isEquipped = isEquipped;
            if(isEquipped) equippedTitle = this;
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            if(isExpired(fullTitle)) {
                Msg.warn(player, "이 칭호는 만료되었습니다.");
                reloadGUI();
                return;
            }

            if(isEquipped) {
                this.isEquipped = false;
                equippedTitle = null;
            }
            else {
                if(clickType.equals(ClickType.SHIFT_RIGHT)) {
                    player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1.5f);
                    Msg.send(player, "&c칭호를 삭제했습니다.", pfix);
                    new PlayerData(player).getTitles().remove(fullTitle);
                    player.closeInventory();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            new TitleWindow(player);
                        }
                    }.runTaskLater(getPlugin(), 2);
                    return;
                }
                if(equippedTitle!=null) equippedTitle.isEquipped = false;
                isEquipped = true;
                equippedTitle = this;
            }
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
            reloadGUI();
        }

        @Override
        public ItemStack getIcon() {
            ItemClass item = new ItemClass(new ItemStack(Material.BOOK), getPureTitle(fullTitle));
            if(isEquipped){
                item.addLore("&c장착을 해제하려면 클릭하십시오.");
                if(isPeriodic(fullTitle)) {
                    item.addLore("&f남은 유효 기간: " + DateUtil.formatDate(getRemainMillis(fullTitle) / 1000L));
                }
            }
            else {
                item.addLore("&7이 칭호를 장착하려면 클릭하십시오.");
                if(isPeriodic(fullTitle)) {
                    item.addLore("&f남은 유효 기간: " + DateUtil.formatDate(getRemainMillis(fullTitle) / 1000L));
                }
                item.addLore(" ");
                item.addLore("&4쉬프트+우클릭&c으로 칭호를 삭제합니다.");
                item.addLore("&c삭제한 칭호는 복구할 수 없습니다.");
            }
            return item.getItem();
        }
    }

    @Override
    public void close(boolean b) {
        PlayerData pdc = new PlayerData(player);
        if(equippedTitle==null) pdc.setTitle(null);
        else pdc.setTitle(equippedTitle.fullTitle);
        disable();
        if(b) {
            new MenuWindow(player, 2);
        }
    }
}
