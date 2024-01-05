package me.rukon0621.guardians.noticeboard;

import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class NoticeBoardManager implements Listener {
    private static final main plugin = main.getPlugin();
    private static final String guiName = "&f\uF000\uF00F";
    private static HashMap<String, NoticeBoard> noticeBoardData; //서버에 존재하는 게시판
    private static HashMap<Block, NoticeBoard> noticeBoardRegisteredData; //각 블럭에 등록된 게시판
    private static HashMap<Player, NoticeBoard> openingNoticeBoard; //현재 플레이어가 수정중인 게시판

    public static String getGuiName() { return guiName; }

    private static Configure getConfig() {
        return new Configure("noticeBoard.yml", FileUtil.getOuterPluginFolder().getPath());
    }

    //Constructor
    public NoticeBoardManager() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reloadNoticeBoard();
    }

    public static void reloadNoticeBoard() {
        noticeBoardData = new HashMap<>();
        noticeBoardRegisteredData = new HashMap<>();
        openingNoticeBoard = new HashMap<>();
        Configure config = getConfig();
        for(String key : config.getConfig().getKeys(false)) {
            NoticeBoard noticeBoard = (NoticeBoard) config.getConfig().get(key);
            noticeBoardData.put(key, noticeBoard);
            for(Block block : noticeBoard.getRegisteredBlocks()) {
                noticeBoardRegisteredData.put(block, noticeBoard);
            }
        }
    }

    public static void createNewNoticeBoard(Player player, String name) {
        if(noticeBoardData.containsKey(name)) {
            Msg.send(player, "&c해당 게시판은 이미 존재하는 게시판입니다.", pfix);
            return;
        }
        Configure config = getConfig();
        NoticeBoard board = new NoticeBoard(name, new HashMap<>(), new ArrayList<>());
        config.getConfig().set(name, board);
        config.saveConfig();
        noticeBoardData.put(name, board);
        Msg.send(player, "성공적으로 생성했습니다.", pfix);
    }
    public static void deleteNoticeBoard(Player player, String name) {
        if(!noticeBoardData.containsKey(name)) {
            Msg.send(player, "&c해당 게시판은 존재하지 않는 게시판입니다.", pfix);
            return;
        }
        Configure config = getConfig();
        config.getConfig().set(name, null);
        config.saveConfig();
        reloadNoticeBoard();
        Msg.send(player, "성공적으로 삭제했습니다.", pfix);
    }
    public static void registerNoticeBoardAtBlock(Player player, String name, Block targetBlock) {
        if(!noticeBoardData.containsKey(name)) {
            Msg.send(player, "&c해당 게시판은 존재하지 않는 게시판입니다.", pfix);
            return;
        }
        NoticeBoard board = noticeBoardData.get(name);
        if(board.registeredBlock(targetBlock)) {
            Msg.send(player, "성공적으로 등록했습니다.", pfix);
        }
        else {
            Msg.send(player, "성공적으로 등록을 해제했습니다.", pfix);
        }
        Configure config = getConfig();
        config.getConfig().set(name, board);
        config.saveConfig();
        reloadNoticeBoard();
    }
    public static void modifyNoticeBoardItems(Player player, String name) {
        if(!noticeBoardData.containsKey(name)) {
            Msg.send(player, "&c해당 게시판은 존재하지 않는 게시판입니다.", pfix);
            return;
        }
        NoticeBoard board = noticeBoardData.get(name);
        openingNoticeBoard.put(player, board);
        board.modifyBoard(player);
    }
    public static void showNoticeBoardList(Player player) {
        Msg.send(player, "서버에 존재하는 게시판 이름 목록입니다.", pfix);
        for(String key : noticeBoardData.keySet()) {
            Msg.send(player, key);
        }
    }

    //게시판 클릭 방지
    @EventHandler
    public void onClickNoticeBoard(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals(guiName)) return;
        if(e.getRawSlot()==-999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            return;
        }
        e.setCancelled(true);
        if(e.getCurrentItem()==null) return;
        List<String> lores = e.getCurrentItem().getItemMeta().getLore();
        for(String lore : lores) {
            lore = Msg.uncolor(lore);
            if(lore.startsWith("X")) {
                String[] data = lore.split(" ");
                double x = Double.parseDouble(data[1]);
                double z = Double.parseDouble(data[3]);

                Location loc = new Location(player.getWorld(), x, 0, z);
                if(loc.distanceSquared(player.getLocation())>1500*1500) {
                    Msg.warn(player, "추적 대상이 다른 섬에 있거나 너무 멀리 떨어져 있습니다.");
                    return;
                }

                if(player.getInventory().getItemInOffHand().getType().equals(Material.COMPASS)) {
                    player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                }
                if(!player.getInventory().getItemInOffHand().getType().equals(Material.AIR)) {
                    Msg.warn(player, "왼손을 비워야 추적 나침반을 사용할 수 있습니다.");
                    return;
                }
                ItemClass it = new ItemClass(new ItemStack(Material.COMPASS), "&6게시판 추적");
                it.addLore("#aaccff나침반의 방향을 따라가면 위치에 도달할 수 있습니다.");
                it.addLore("&f");
                it.addLore(String.format("&7X: %.0f | &7Z: %.0f", loc.getX(), loc.getZ()));
                it.addLore("&f");
                it.addLore("&c위치 안내를 종료하려면 클릭하십시오.");
                player.setCompassTarget(loc);
                player.getInventory().setItemInOffHand(it.getItem());
                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,1, 1);
                player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE,1, 0.7f);
                player.closeInventory();
                return;
            }
        }
    }

    //블럭 우클릭시 게시판 열기
    @EventHandler
    public void onOpenNoticeBoard(PlayerInteractEvent e) {
        if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Block block = e.getClickedBlock();
        if(!noticeBoardRegisteredData.containsKey(block)) return;
        Player player = e.getPlayer();
        NoticeBoard noticeBoard = noticeBoardRegisteredData.get(block);
        noticeBoard.openBoard(player);
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);

        if(noticeBoard.getName().equals("벤터스")) {
            if(DialogQuestManager.completeCustomObject(player, "벤터스에 있는 게시판 확인하기")) {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
            }
        }
        //Custom Object
        if(DialogQuestManager.completeCustomObject(player, "게시판 찾기")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Msg.send(player, " ");
                    Msg.send(player, "&e게시판에 있는 여러 의뢰를 진행하며 레벨 5를 달성하십시오!", pfix);
                    Msg.send(player, " ");
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
                }
            }.runTaskLater(plugin, 20);
        }

        if(DialogQuestManager.completeCustomObject(player, noticeBoard.getName() + " 게시판에 사람을 찾는 전단지 붙이기")) {
            Msg.send(player, "&a게시판에 전단지를 붙였다. 다시 돌아가보자!", pfix);
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
        }
        else if(noticeBoard.getName().equals("루테티아")) {
            if(DialogQuestManager.completeCustomObject(player, "게시판에 전단지 붙이기")) {
                Msg.send(player, "&a게시판에 전단지를 붙였다.", pfix);
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
            }
        }

    }

    //게시판 수정 후 저장
    @EventHandler
    public void onSaveNoticeBoard(InventoryCloseEvent e) {
        if(!(e.getPlayer() instanceof Player player)) return;
        if(!e.getView().getTitle().endsWith("\uE206\uE201\uE201")) return;
        HashMap<Integer, ItemStack> items = new HashMap<>();
        for(int i = 0 ; i < 45 ; i++) {
            if(e.getInventory().getItem(i)==null) continue;
            items.put(i, e.getInventory().getItem(i));
        }
        NoticeBoard board = openingNoticeBoard.get(player);
        board.modifyItems(items);
        Configure config = getConfig();
        config.getConfig().set(board.getName(), board);
        config.saveConfig();
        Msg.send(player, "성공적으로 게시판을 수정하였습니다.", pfix);
    }

}
