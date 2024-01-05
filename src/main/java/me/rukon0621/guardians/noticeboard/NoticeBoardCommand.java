package me.rukon0621.guardians.noticeboard;

import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.dialogquest.Quest;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.pfix;

public class NoticeBoardCommand implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"생성", "삭제", "등록", "수정", "목록", "퀘스트아이콘"};

    public NoticeBoardCommand() {
        plugin.getCommand("noticeboard").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }
        if(args[0].startsWith("생")) {
            if(args.length < 2 ){
                usage(player, "생성", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            NoticeBoardManager.createNewNoticeBoard(player, name);
        }
        else if(args[0].startsWith("삭")) {
            if(args.length < 2 ){
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            NoticeBoardManager.deleteNoticeBoard(player, name);
        }
        else if(args[0].startsWith("등")) {
            if(args.length < 2 ){
                usage(player, "등록", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            Block targetBlock = player.getTargetBlockExact(10);
            if(targetBlock==null) {
                Msg.send(player, "&c등록할 블럭을 정확히 바라봐주세요.", pfix);
                return true;
            }
            NoticeBoardManager.registerNoticeBoardAtBlock(player, name, targetBlock);
        }
        else if(args[0].startsWith("수")) {
            if(args.length < 2 ){
                usage(player, "수정", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            NoticeBoardManager.modifyNoticeBoardItems(player, name);
        }
        else if(args[0].startsWith("목")) {
            NoticeBoardManager.showNoticeBoardList(player);
        }
        else if(args[0].startsWith("퀘")) {
            if(args.length<2) {
                usage(player, "퀘스트아이콘", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            for(String quest : DialogQuestManager.getQuestData().keySet()) {
                if(quest.startsWith(name)) {
                    name = quest;
                }
            }
            if(!DialogQuestManager.getQuestData().containsKey(name)) {
                Msg.send(player ,"&c해당 이름의 퀘스트는 존재하지 않습니다.", prefix(name));
                return true;
            }
            Quest quest = DialogQuestManager.getQuestData().get(name);
            player.getInventory().addItem(quest.getIcon(player));
            Msg.send(player, "성공적으로 지급 받았습니다.", pfix);
        }

        return true;
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equals("생성")) {
            Msg.send(player, "&6/게시판 생성 <이름>");
            Msg.send(player, "&7   새로운 게시판을 생성합니다.");
        }
        else if(arg.equals("삭제")) {
            Msg.send(player, "&6/게시판 삭제 <이름>");
            Msg.send(player, "&7   게시판을 삭제합니다.");
        }
        else if(arg.equals("등록")) {
            Msg.send(player, "&6/게시판 등록 <이름>");
            Msg.send(player, "&7   바라보는 블럭에 해당 게시판을 등록합니다.");
            Msg.send(player, "&7   이미 등록되어 있다면 삭제합니다.");
        }
        else if(arg.equals("수정")) {
            Msg.send(player, "&6/게시판 수정 <이름>");
            Msg.send(player, "&7   해당 게시판 내부의 아이템들을 수정합니다.");
        }
        else if(arg.equals("목록")) {
            Msg.send(player, "&6/게시판 목록");
            Msg.send(player, "&7   서버에 존재하는 게시판의 목록을 확인합니다.");
        }
        else if(arg.equals("퀘스트아이콘")) {
            Msg.send(player, "&6/게시판 퀘스트아이콘 <퀘스트 이름>");
            Msg.send(player, "&7   해당 퀘스트의 아이콘을 지급 받습니다.");
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
    private String prefix(String name) {
        return "&7[ &e"+name+" &7] ";
    }
}
