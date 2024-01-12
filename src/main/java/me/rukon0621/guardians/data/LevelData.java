package me.rukon0621.guardians.data;

import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

import static me.rukon0621.guardians.main.pfix;

public class LevelData {
    public static Map<Integer, Long> expAtLevel;
    //Level, exp - Level레벨에서 다음 레벨 까지의 필요 경험치의 양

    public static final String EXP_BOOK_TYPE_NAME = "경험치 책";
    public static final String EQUIPMENT_EXP_BOOK_TYPE_NAME = "장비 경험치 책";

    public static final int maxLevel = 50;
    public static final int runeMaxLevel = 20;

    //레벨 데이터 재생성
    public static void resetLevelData() {
        expAtLevel = new HashMap<>();
        int maxLevel = 50;
        expAtLevel.put(1, 3L);
        expAtLevel.put(2, 4L);
        expAtLevel.put(3, 10L);
        expAtLevel.put(4, 10L);
        expAtLevel.put(5, 12L);
        expAtLevel.put(6, 15L);
        expAtLevel.put(7, 20L);
        expAtLevel.put(8, 25L);
        expAtLevel.put(9, 30L);
        expAtLevel.put(10, 40L);
        long exp = 45;
        for(int i = 11 ; i < 250 ; i++) {
            expAtLevel.put(i, exp);
            if(i<17) exp = Math.round(exp * 1.12f);
            else if(i<50) exp = Math.round(exp * 1.08f);
            else if(i<100) exp = Math.round(exp * 1.08f);
            else if(i<150) exp = Math.round(exp * 1.04f);
            else if(i<200) exp = Math.round(exp * 1.02f);
            else exp = (Math.round(exp * 1.015f));
        }
    }

    //경험치 추가
    public static void addExp(Player player, long addingValue) {
        PlayerData pdc = new PlayerData(player);
        //최대 레벨은 경험치를 얻을 수 없음.
        pdc.setExp(pdc.getExp()+addingValue);
        if(pdc.getLevel()== maxLevel) return;

        boolean levelUp = false;
        while(pdc.getExp()>= expAtLevel.get(pdc.getLevel())) {
            levelUp = true;
            pdc.setExp(pdc.getExp()-expAtLevel.get(pdc.getLevel()));
            LevelUp(player);
        }
        reloadIndicator(player);
        if(levelUp) {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.8f);
            Msg.send(player, " ");
            Msg.send(player, "축하합니다! " + pdc.getLevel() + "레벨에 도달하셨습니다!", pfix);
            Msg.send(player, "&e레벨업을 하여 3만큼의 스킬 포인트를 획득하였습니다.");
            Msg.send(player, "&6레벨업을 하여 1만큼의 제작 스킬 포인트를 획득하였습니다.");
            Msg.send(player, " ");
        }
    }

    //레벨 업 (변수만 바꾸고 스텟포인트 추가등의 시스템을 다룸)
    public static void LevelUp(Player player) {
        PlayerData pdc = new PlayerData(player);
        //최대 레벨 패스
        if(pdc.getLevel()== maxLevel) return;
        pdc.setLevel(pdc.getLevel()+1);

        if(pdc.getLevel()==3) {
            StoryManager.readStory(player, "스킬트리찍기");
        }
        else if(pdc.getLevel()==5) {
            DialogQuestManager.completeCustomObject(player, "레벨 5 달성하기");
            StoryManager.readStory(player, "레벨5");
        }
        else if(pdc.getLevel()==17) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Msg.send(player, " ");
                    Msg.send(player, "&e레벨 17을 달성했다. 한 번 &b켈트&e에게 찾아가보자.", pfix);
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
                    player.playSound(player, Sound.ENTITY_ENDER_DRAGON_HURT, 1, 0.5f);
                }
            }.runTaskLater(main.getPlugin(), 60);
        }
        else if(pdc.getLevel()==26) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Msg.send(player, " ");
                    Msg.send(player, "&e레벨 26을 달성했다. &b루테티아에 있는 도서관&e에 찾아가보자.", pfix);
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
                    player.playSound(player, Sound.ENTITY_ENDER_DRAGON_HURT, 1, 0.5f);
                }
            }.runTaskLater(main.getPlugin(), 60);
        }

        //로깅
        LogManager.log(player, "levelUp", pdc.getLevel() + "레벨 달성");
        pdc.setSkillPoint(pdc.getSkillPoint() + 3);

    }

    /**
     * 해당 경험치 책의 경험치 양을 반환
     * @param book 경험치 책
     * @return 해당 경험치 책의 경험치 양
     */
    public static long getExpOfBook(ItemStack book) {
        return Long.parseLong(Msg.uncolor(book.getItemMeta().getDisplayName()).split(" ")[3]);
    }
    /**
     * 해당 경험치 책의 경험치 양을 반환
     * @param name 경험치 책의 이름
     * @return 해당 경험치 책의 경험치 양
     */
    public static long getExpOfBook(String name) {
        return Long.parseLong(Msg.uncolor(name).split(" ")[3]);
    }

    /**
     * 해당 양의 경험치 책 아이템 객체를 반환
     * @param amount 경험치 책의 경험치 양
     * @return 해당 양의 경험치 책 아이템 객체를 반환
     */
    public static ItemStack getExpBook(long amount) {
        ItemData itemData = new ItemData(new ItemStack(Material.ENCHANTED_BOOK));
        itemData.setName(String.format("&e지식의 서 &7[ &b%d &7]", amount));
        itemData.setLevel(0);
        itemData.setType(EXP_BOOK_TYPE_NAME);
        itemData.extraLore.add("&7우클릭하여 경험치를 획득하여 &e플레이어 레벨업&7에 사용할 수 있다.");
        return itemData.getItemStack();
    }
    /**
     * 해당 양의 경험치 책 아이템 객체를 반환
     * @param amount 경험치 책의 경험치 양
     * @return 해당 양의 경험치 책 아이템 객체를 반환
     */
    public static ItemStack getEquipmentExpBook(int amount) {
        ItemData itemData = new ItemData(new ItemStack(Material.ENCHANTED_BOOK));
        itemData.setName(String.format("&e강화의 서 &7[ &b%d &7]", amount));
        itemData.setLevel(0);
        itemData.setType("장비 경험치 책");
        itemData.extraLore.add("&e장비의 레벨업&7에 사용되는 경험치 책이다.");
        ItemClass item = itemData.getItem();
        item.setCustomModelData(1);
        return item.getItem();
    }

    /**
     * 해당 양의 디나르를 지급합니다
     * @param amount 수표
     * @return 해당 양의 경험치 책 아이템 객체를 반환
     */
    public static ItemStack getDinarItem(int amount) {
        ItemData itemData = new ItemData(new ItemStack(Material.PAPER));
        itemData.setName("&a" + amount + " &2디나르");
        itemData.setLevel(0);
        itemData.setType("디나르");
        itemData.extraLore.add("&7우클릭하여 디나르를 지급 받습니다.");
        ItemClass item = itemData.getItem();
        item.setCustomModelData(0);
        return item.getItem();
    }


    //화면에 표시되는 수치를 변경함
    public static void reloadIndicator(Player player) {
        player.setLevel(0);
        BarManager.reloadBar(player);
    }

}
