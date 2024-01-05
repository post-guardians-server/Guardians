package me.rukon0621.guardians.dialogquest;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Targeter;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.main.pfix;

public class DialogCommands implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"생성", "삭제", "등록", "등록목록", "목록", "리로드"};

    public DialogCommands() {
        plugin.getCommand("dialog").setExecutor(this);
        plugin.getCommand("dialog").setTabCompleter(new DialogCommandsTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            if(args.length==4) return false;
            if(args[0].equals("생성")) {
                if(args.length<3) return false;
                String path = args[1];
                String name = ArgHelper.sumArg(args, 2);
                if(DialogQuestManager.createNewDialog(path, name)) {
                    plugin.getLogger().info("대화문 생성을 성공했습니다.");
                }
                else {
                    plugin.getLogger().info("대화문 생성에 실패했습니다.");
                }
            }
            else if (args[0].equals("삭제")) {
                if(args.length<2) return false;
                String name = ArgHelper.sumArg(args, 1);
                if(DialogQuestManager.deleteDialog(name)) {
                    plugin.getLogger().info("대화문 삭제를 성공했습니다.");
                } else {
                    plugin.getLogger().info("대화문 삭제에 실패했습니다.");
                }
            }
            else if (args[0].equals("리로드")) {
                for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                    p.closeInventory();
                }
                CountDownLatch latch = new CountDownLatch(1);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getLogger().info("대화문 리로드중...");
                        DialogQuestManager.reloadDialogs(latch);
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        plugin.getLogger().info("대화문 리로드를 성공했습니다.");
                    }
                }.runTaskAsynchronously(plugin);
            }
            else if (args[0].equals("목록")) {
                for(String name : DialogQuestManager.getDialogData().keySet()) {
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
            if(args.length<3) {
                usage(player, "생성", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 2);
            if(name.startsWith("!")||name.startsWith(":")) {
                Msg.warn(player, "대화문 이름은 해당 특수문자로 시작할 수 없습니다.");
                return true;
            }
            String path = args[1];
            if(DialogQuestManager.createNewDialog(path, name)) {
                Msg.send(player, "성공적으로 대화문을 생성했습니다.", pfix);
            } else {
                Msg.send(player, "&c해당 이름의 대화문은 이미 존재합니다.", pfix);
                return true;
            }

            Entity target = Targeter.getTargetedEntity(player, 20);
            if(target==null) return true;
            if(DialogQuestManager.registerDialog(name, target)) {
                Msg.send(player, target.getName()+"&7에게 성공적으로 대화문을 등록했습니다.",prefix(name));
            }
            else {
                Msg.send(player, target.getName()+"&6에게 등록된 대화문을 삭제했습니다.", prefix(name));
            }

        }
        else if (args[0].equals("삭제")) {
            if(args.length < 2) {
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(DialogQuestManager.deleteDialog(name)) {
                Msg.send(player, "성공적으로 대화문을 삭제하였습니다", pfix);
            } else {
                Msg.send(player, "&c해당 이름의 대화문은 존재하지 않습니다.", pfix);
            }
        }
        else if (args[0].equals("등록")) {
            if(args.length<2) {
                usage(player, "등록", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!DialogQuestManager.getDialogData().containsKey(name)) {
                Msg.send(player, "&c해당 대화문은 존재하지 않는 대화문입니다.", prefix(name));
                return true;
            }
            Entity target = Targeter.getTargetedEntity(player, 20);
            if(target==null) {
                Msg.send(player, "&c대화문을 등록할 대상을 바라봐주세요.", prefix(name));
                return true;
            }
            if(DialogQuestManager.registerDialog(name, target)) {
                Msg.send(player, target.getName()+"&7에게 성공적으로 대화문을 등록했습니다.",prefix(name));
            }
            else {
                Msg.send(player, target.getName()+"&6에게 등록된 대화문을 삭제했습니다.", prefix(name));
            }
        }
        else if (args[0].equals("목록")) {
            Msg.send(player, "&6서버에 존재하는 대화문 목록입니다.", pfix);
            for(String name : DialogQuestManager.getDialogPathConfig().getConfig().getKeys(false)) {
                if(args.length>1) {
                    if(!name.startsWith(args[1])) continue;
                }
                Msg.send(player, name + " : " + DialogQuestManager.getDialogPathConfig().getConfig().getString(name));
            }
            return true;
        }
        else if (args[0].equals("등록목록")) {
            Entity target = Targeter.getTargetedEntity(player, 20);
            if(target==null) {
                Msg.send(player, "&c목록을 확인할 등록 대상을 바라봐주세요.", pfix);
                return true;
            }
            Msg.send(player, target.getName()+"에게 등록된 대화문 목록입니다.", pfix);
            for(String name : DialogQuestManager.getRegisteredDialog(target)) {
                Msg.send(player, "&e"+name);
            }
        }
        else if (args[0].equals("리로드")) {
            for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                p.closeInventory();
            }
            CountDownLatch latch = new CountDownLatch(1);
            DialogQuestManager.reloadDialogs(latch);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Msg.send(player, "&7대화문 리로드중...", pfix);
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Msg.send(player, "&a성공적으로 새로고침했습니다.", pfix);
                }
            }.runTaskAsynchronously(plugin);
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
            Msg.send(player, "&6/대화문 생성 <경로> <이름>");
            Msg.send(player, "&7   새로운 대화문을 해당 경로에 생성합니다.");
            Msg.send(player, "&7   예시 경로 : 대화1/테스트대화.yml");
        }
        else if (arg.equals("삭제")) {
            Msg.send(player, "&6/대화문 삭제 <이름>");
            Msg.send(player, "&7   서버에서 해당 대화문을 영구적으로 삭제합니다.");
        }
        else if (arg.equals("등록")) {
            Msg.send(player, "&6/대화문 등록 <이름>");
            Msg.send(player, "&7   해당 대화문을 바라보는 엔티티에게 등록합니다.");
            Msg.send(player, "&7   다시 입력하면 등록된 대화문을 삭제합니다.");
        }
        else if (arg.equals("목록")) {
            Msg.send(player, "&6/대화문 목록 [<검색필터>]");
            Msg.send(player, "&7   서버에 등록된 대화문과 그 경로를 보여줍니다.");
            Msg.send(player, "&7   필터에 내용을 입력하면 해당 문자열로 시작하는 대화문만 검색합니다.");
        }
        else if (arg.equals("등록목록")) {
            Msg.send(player, "&6/대화문 등록목록");
            Msg.send(player, "&7   바라보고 있는 엔티티에게 등록된 대화문 목록을 보여줍니다.");
        }
        else if (arg.equals("리로드")) {
            Msg.send(player, "&6/대화문 리로드");
            Msg.send(player, "&7   서버에 존재하는 대화문 데이터를 새로고침합니다.");
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
