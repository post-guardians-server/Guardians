package me.rukon0621.guardians.dialogquest;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Targeter;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.main.pfix;

public class QuestCommands implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"생성", "삭제", "목록", "보상설정", "아이템설정", "쿨타임", "아이콘설정","추적", "추적비활성화","리로드"};

    public QuestCommands() {
        plugin.getCommand("quest").setExecutor(this);
        plugin.getCommand("quest").setTabCompleter(new QuestCommandsTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            if(args.length==0) return false;
            if(args[0].equals("생성")) {
                if(args.length<4) return false;
                String name = ArgHelper.sumArg(args, 3);
                String sort = args[2];
                String path = args[1];
                int intSort;
                if(sort.equals("몹처치")) intSort = 1;
                else if(sort.equals("아이템")) intSort = 2;
                else if(sort.equals("방문")) intSort = 3;
                else if(sort.equals("커스텀")) intSort = 4;
                else {
                    plugin.getLogger().info("잘못된 퀘스트 종류를 입력했습니다..");
                    return true;
                }
                if(DialogQuestManager.createNewQuest(path, name, intSort, null, DialogQuestManager.QuestType.MAIN)) {
                    plugin.getLogger().info("성공적으로 새로운 퀘스트를 생성했습니다.");
                }
                else plugin.getLogger().info("퀘스트 생성에 실패했습니다.");
            }
            else if (args[0].equals("삭제")) {
                if(args.length<2) return false;
                String name = ArgHelper.sumArg(args, 1);
                if (DialogQuestManager.deleteQuest(name)) {
                    plugin.getLogger().info("퀘스트 삭제에 성공했습니다.");
                }
                else {
                    plugin.getLogger().info("퀘스트 삭제에 실패했습니다.");
                }
            }
            else if (args[0].equals("리로드")) {
                for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                    player.closeInventory();
                }
                CountDownLatch latch = new CountDownLatch(1);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getLogger().info("퀘스트 리로드중...");
                        DialogQuestManager.reloadQuests(latch);
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        plugin.getLogger().info("모든 퀘스트 데이터를 새로고침했습니다.");
                    }
                }.runTaskAsynchronously(plugin);
            }
            else if (args[0].equals("목록")) {
                for(String name : DialogQuestManager.getQuestData().keySet()) {
                    plugin.getLogger().info(name);
                }
            }
            else return false;
            return true;
        }
        if(args.length==0) {
            usages(player);
            return true;
        }
        if(args[0].equals("생성")) {
            if(args.length<5) {
                usage(player, "생성", true);
                return true;
            }
            String sortString = args[2];
            int sort;
            switch (sortString) {
                case "몹처치" -> sort = 1;
                case "아이템" -> sort = 2;
                case "방문" -> sort = 3;
                case "커스텀" -> sort = 4;
                default -> {
                    Msg.send(player, "&c올바른 퀘스트 종류를 입력해주세요.", pfix);
                    usage(player, "생성", true);
                    return true;
                }
            }
            try {
                DialogQuestManager.QuestType type = DialogQuestManager.QuestType.valueOf(args[3]);

                String name = ArgHelper.sumArg(args, 4);
                if(name.startsWith("!")) {
                    Msg.warn(player, "대화문 이름은 해당 특수문자로 시작할 수 없습니다.");
                    return true;
                }
                String path = args[1];
                Entity target = Targeter.getTargetedEntity(player, 20);
                if(DialogQuestManager.createNewQuest(path, name, sort, target, type)) {
                    Msg.send(player, "성공적으로 새로운 퀘스트를 생성하였습니다.", pfix);
                } else {
                    Msg.send(player, "&c해당 퀘스트는 이미 존재하는 퀘스트입니다.", pfix);
                }
            } catch (IllegalArgumentException e) {
                Msg.warn(player, "올바른 퀘스트 타입을 입력하십시오.");
                usage(player, "생성", true);
            }
        }
        else if (args[0].equals("삭제")) {
            if(args.length<2) {
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(DialogQuestManager.deleteQuest(name)) {
                Msg.send(player, "&6성공적으로 퀘스트를 삭제했습니다.", pfix);
            } else {
                Msg.send(player, "&c해당 퀘스트는 서버에 존재하지 않는 퀘스트입니다.", pfix);
            }
        }
        else if (args[0].equals("추적비활성화")) {
            if(args.length<2) {
                usage(player, "추적비활성화", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!DialogQuestManager.getQuestData().containsKey(name)) {
                Msg.send(player ,"&c해당 이름의 퀘스트는 존재하지 않습니다.", prefix(name));
                return true;
            }
            DialogQuestManager.setQuestNavigatable(name, false);
            DialogQuestManager.setQuestNavigatingTarget(name, null);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Msg.send(player, "&7변경사항을 적용하고 퀘스트를 리로드하는 중입니다...", prefix(name));
                    CountDownLatch latch = new CountDownLatch(1);
                    DialogQuestManager.reloadQuests(latch);
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Msg.send(player, "&f이제 해당 퀘스트는 더이상 위치 추적이 불가능합니다.", prefix(name));
                }
            }.runTaskAsynchronously(plugin);
        }
        else if (args[0].startsWith("추")) {
            if(args.length<2) {
                usage(player, "추적", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!DialogQuestManager.getQuestData().containsKey(name)) {
                Msg.send(player ,"&c해당 이름의 퀘스트는 존재하지 않습니다.", prefix(name));
                return true;
            }
            DialogQuestManager.setQuestNavigatable(name, true);
            DialogQuestManager.setQuestNavigatingTarget(name, player.getLocation());
            new BukkitRunnable() {
                @Override
                public void run() {
                    Msg.send(player, "&7추적 위치를 적용하고 퀘스트를 리로드하는 중입니다...", prefix(name));
                    CountDownLatch latch = new CountDownLatch(1);
                    DialogQuestManager.reloadQuests(latch);
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Msg.send(player, "&f성공적으로 설정되었습니다.", prefix(name));
                }
            }.runTaskAsynchronously(plugin);
        }
        else if (args[0].startsWith("목")) {
            Msg.send(player, "&6이 서버에 존재하는 퀘스트의 목록입니다.");
            for(String name : DialogQuestManager.getQuestPathConfig().getConfig().getKeys(false)) {
                if(args.length>1&&!name.startsWith(args[1])) continue;
                Msg.send(player, "&e"+name + " &7: &f" +DialogQuestManager.getQuestPathConfig().getConfig().getString(name));
            }
        }
        else if (args[0].startsWith("보")||args[0].startsWith("아이템")) {
            if(args.length<2) {
                usage(player, args[0], true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!DialogQuestManager.getQuestData().containsKey(name)) {
                Msg.send(player, "&c해당 퀘스트는 존재하지 않는 퀘스트입니다.", prefix(name));
                return true;
            }
            if(args[0].startsWith("아")) {
                if(DialogQuestManager.getQuestData().get(name).getSort()!=2) {
                    Msg.send(player, "&c아이템 설정은 아이템 퀘스트에서만 진행할 수 있습니다.", prefix(name));
                    return true;
                }
            }
            DialogQuestManager.openQuestDataList(player, name, args[0].startsWith("보"), false);
            Msg.send(player, "정보를 변경합니다.", prefix(name));
        }
        else if (args[0].startsWith("아이콘")) {
            if(args.length<2) {
                usage(player, "아이콘설정", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!DialogQuestManager.getQuestData().containsKey(name)) {
                Msg.send(player ,"&c해당 이름의 퀘스트는 존재하지 않습니다.", prefix(name));
                return true;
            }
            if(player.getInventory().getItemInMainHand()==null||player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                Msg.send(player, "&c손에 아이콘으로 등록할 아이템을 들어주세요.", prefix(name));
                return true;
            }
            DialogQuestManager.setQuestIcon(name, player.getInventory().getItemInMainHand());
            Msg.send(player, "성공적으로 해당 퀘스트의 아이콘을 변경했습니다.", prefix(name));
        }
        else if (args[0].startsWith("쿨타임")) {
            if(args.length<3) {
                usage(player, args[0], true);
                return true;
            }
            try {
                int sec = Integer.parseInt(args[1]);
                String name = ArgHelper.sumArg(args, 2);
                if(!DialogQuestManager.getQuestData().containsKey(name)) {
                    Msg.send(player ,"&c해당 이름의 퀘스트는 존재하지 않습니다.", prefix(name));
                    return true;
                }
                if(!DialogQuestManager.setQuestRepeatTimer(name, sec)) {
                    Msg.warn(player, "이 퀘스트는 반복 퀘스트가 아니기에 쿨타임을 설정할 수 없습니다.");
                    return true;
                }
                Msg.send(player, "성공적으로 해당 퀘스트의 쿨타임을 설정했습니다.", prefix(name));
            } catch (NumberFormatException e) {
                Msg.warn(player, "올바른 숫자를 입력해주세요.");
            }
        }
        else if (args[0].equals("리로드")) {
            for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                p.closeInventory();
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    Msg.send(player, "&7퀘스트를 리로드하는 중입니다...", pfix);
                    CountDownLatch latch = new CountDownLatch(1);
                    DialogQuestManager.reloadQuests(latch);
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Msg.send(player, "&a성공적으로 새로고침했습니다.", pfix);
                }
            }.runTaskAsynchronously(plugin);;
        }
        else {
            usages(player);
        }

        return true;
    }

    private String prefix(String name) {
        return "&7[ &e"+name+" &7] ";
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equals("생성")) {
            Msg.send(player, "&6/퀘스트 생성 <경로> <퀘스트종류> <퀘스트타입> <이름>");
            Msg.send(player, "&7   새로운 퀘스트를 해당 경로에 생성합니다.");
            Msg.send(player, "&7   예시 경로 : 대화1/테스트대화.yml");
            Msg.send(player, "&7   퀘스트 종류");
            Msg.send(player, "&7   - 몹처치");
            Msg.send(player, "&7   - 아이템");
            Msg.send(player, "&7   - 방문");
            Msg.send(player, "&7   - 커스텀");
            Msg.send(player, "&7   퀘스트 타입");
            Msg.send(player, "&7   - MAIN (반복 불가능, 커스텀모델 100, 메인퀘)");
            Msg.send(player, "&7   - REPEATABLE (반복 가능, 커스텀모델 102, 퀘스트 포기가능, 서브반복퀘)");
            Msg.send(player, "&7   - SUB_MAIN (반복 불가능, 커스텀모델 101, 서브메인)");
            Msg.send(player, "&7   - HIDDEN (반복 불가능, 커스텀모델 104, 히든퀘)");
        }
        else if (arg.equals("삭제")) {
            Msg.send(player, "&6/퀘스트 삭제 <이름>");
            Msg.send(player, "&7   해당 퀘스트를 서버에서 영구적으로 삭제합니다.");
        }
        else if (arg.equals("목록")) {
            Msg.send(player, "&6/퀘스트 목록 [<검색필터>]");
            Msg.send(player, "&7   서버에 존재하는 퀘스트의 목록을 확인합니다.");
            Msg.send(player, "&7   필터에 내용을 입력하면 해당 문자열로 시작하는 퀘스트만 검색합니다.");
        }
        else if (arg.equals("보상설정")) {
            Msg.send(player, "&6/퀘스트 보상설정 <이름>");
            Msg.send(player, "&7   해당 퀘스트의 보상을 GUI를 통해 설정합니다.");
        }
        else if (arg.equals("아이템설정")) {
            Msg.send(player, "&6/퀘스트 아이템설정 <이름>");
            Msg.send(player, "&7   아이템 퀘스트의 가져올 아이템을 GUI를 통해 설정합니다.");
        }
        else if (arg.equals("쿨타임")) {
            Msg.send(player, "&6/퀘스트 쿨타임설정 <시간(초)> <이름>");
            Msg.send(player, "&7   퀘스트의 쿨타임을 설정합니다.");
        }
        else if (arg.equals("리로드")) {
            Msg.send(player, "&6/퀘스트 리로드");
            Msg.send(player, "&7   서버에 존재하는 퀘스트 정보를 새로고침합니다.");
        }
        else if (arg.equals("아이콘설정")) {
            Msg.send(player, "&6/퀘스트 아이콘[설정] <이름>");
            Msg.send(player, "&7   손에 들고 있는 아이템을 퀘스트의 아이콘으로 설정합니다.");
        }
        else if (arg.equals("추적")) {
            Msg.send(player, "&6/대화문 추적[기능][활성화] <퀘스트 이름>");
            Msg.send(player, "&7   해당 퀘스트에 도움을 줄 수 있는 추적 시스템을 추가하고");
            Msg.send(player, "&7   추적 대상 위치를 현재 서있는 위치로 갱신합니다.");
        }
        else if (arg.equals("추적비활성화")) {
            Msg.send(player, "&6/대화문 추적비활성화 <퀘스트 이름>");
            Msg.send(player, "&7   해당 퀘스트에 도움을 줄 수 있는 추적 시스템을 추가하고");
            Msg.send(player, "&7   추적 대상 위치를 현재 서있는 위치로 갱신합니다.");
        }
        Msg.send(player, " ");
        if (forone) Msg.send(player, "&e└────────────────────────┘");
    }

    private void usages(Player player) {
        Msg.send(player, "&e┌────────────────────────┐");
        Msg.send(player, " ");
        for(String s : arguments) {
            usage(player, s, false);
        }
        Msg.send(player, "&e└────────────────────────┘");
    }
}
