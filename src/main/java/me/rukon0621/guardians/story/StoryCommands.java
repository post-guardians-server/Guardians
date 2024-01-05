package me.rukon0621.guardians.story;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.main.pfix;

public class StoryCommands implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"생성","중단", "삭제", "목록", "리로드", "진행", "데이터"};

    public StoryCommands() {
        plugin.getCommand("story").setExecutor(this);
        plugin.getCommand("story").setTabCompleter(new StoryCommandsTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7스토리 데이터 리로드중...");
            StoryManager.reloadStory(new CountDownLatch(1));
            MsgUtil.cmdMsg(sender, "&a스토리 데이터 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) return false;
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
            String path = args[1];
            StoryManager.createNewStory(player, name, path);
        }
        else if (args[0].equals("삭제")) {
            if(args.length < 2) {
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            StoryManager.deleteStory(player, name);
        }
        else if (args[0].equals("중단")) {
            StoryManager.stopStory(player, true);
        }
        else if (args[0].equals("목록")) {
            if(args.length>1) {
                String name = ArgHelper.sumArg(args, 1);
                for(String story : StoryManager.getStoryData().keySet()) {
                    if(story.startsWith(name)) {
                        Msg.send(player, story);
                    }
                }
                return true;
            }
            StoryManager.sayStoryList(player);
        }
        else if (args[0].equals("리로드")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Msg.send(player, "&7스토리 정보를 리로드하는 중입니다...", pfix);
                    CountDownLatch latch = new CountDownLatch(1);
                    StoryManager.reloadStory(latch);
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Msg.send(player, "&a성공적으로 새로고침했습니다.", pfix);
                }
            }.runTaskAsynchronously(plugin);
        }
        else if (args[0].equals("진행")) {
            if(args.length < 2) {
                usage(player, "진행", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);

            if(!StoryManager.getStoryData().containsKey(name)) {
                Msg.warn(player, "해당 이름의 스토리는 존재하지 않습니다.");
                return true;
            }

            StoryManager.readStory(player, name);
        }
        else if (args[0].equals("데이터")) {
            if(args.length<4) {
                usage(player, "데이터", true);
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[1]);
            if(target==null) {
                Msg.send(player, "&c제대로된 이름을 입력해주세요.", pfix);
                return true;
            }
            String name = ArgHelper.sumArg(args, 3);

            if(!name.startsWith("flag_")&&!StoryManager.getStoryData().containsKey(name)) {
                Msg.warn(player, "해당 이름의 스토리는 존재하지 않습니다.");
                return true;
            }

            if(args[2].equals("완료")) {
                if(StoryManager.addStory(target, name)) Msg.send(player, "&f데이터를 수정했습니다.", pfix);
                else Msg.send(player, "&f해당 플레이어는 이미 그 스토리를 읽었습니다.", pfix);
            }
            else if (args[2].equals("미완료")) {
                if(StoryManager.removeStory(target, name)) Msg.send(player, "&f데이터를 수정했습니다.", pfix);
                else Msg.send(player, "&f해당 플레이어는 아직 그 스토리를 읽지 않았습니다.", pfix);
            }
            else usages(player);
        }
        else {
            usages(player);
        }

        return true;
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equals("생성")) {
            Msg.send(player, "&6/스토리 생성 <경로> <이름>");
            Msg.send(player, "&7   새로운 스토리를 새로운 경로에 생성합니다.");
            Msg.send(player, "&7   예시 경로 : 스토리00/newStory.yml");
        }
        else if (arg.equals("삭제")) {
            Msg.send(player, "&6/스토리 삭제 <이름>");
            Msg.send(player, "&7   영구적으로 해당 스토리를 서버에서 삭제합니다.");
        }
        else if (arg.equals("목록")) {
            Msg.send(player, "&6/스토리 목록 [<검색필터>]");
            Msg.send(player, "&7   서버에 존재하는 스토리의 목록을 보여줍니다.");
            Msg.send(player, "&7   필터에 내용을 입력하면 해당 문자열로 시작하는 스토리만 검색합니다.");
        }
        else if (arg.equals("리로드")) {
            Msg.send(player, "&6/스토리 리로드");
            Msg.send(player, "&7   서버에 있는 스토리를 리로드합니다.");
        }
        else if (arg.equals("진행")) {
            Msg.send(player, "&6/스토리 진행 <이름>");
            Msg.send(player, "&7   스토리를 진행합니다.");
        }
        else if (arg.equals("데이터")) {
            Msg.send(player, "&6/스토리 데이터 <플레이어> <완료/미완료> <이름>");
            Msg.send(player, "&7   플레이어가 해당 스토리를 읽었는지 설정합니다.");
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
