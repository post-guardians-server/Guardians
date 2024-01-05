package me.rukon0621.guardians.dialogquest;

import me.rukon0621.guardians.helper.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DQDataCommandsTabComp implements TabCompleter {
    private static final List<String> argument1 = new ArrayList<>();
    private static final List<String> argumentDialog3 = new ArrayList<>();
    private static final List<String> argumentQuest3 = new ArrayList<>();

    public DQDataCommandsTabComp() {
        //{"대화문완료", "대화문미완료", "퀘스트완료", "퀘스트미완료", "쿨타임초기화", "목표완료", "강제포기"};
        argument1.add("대화문");
        argument1.add("퀘스트");
        argumentDialog3.add("완료");
        argumentDialog3.add("미완료");
        argumentQuest3.add("완료");
        argumentQuest3.add("미완료");
        argumentQuest3.add("쿨타임");
        argumentQuest3.add("강제포기");
        argumentQuest3.add("클리어");
        argumentQuest3.add("목표완료");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return null;
        if(args.length==2) return TabCompleteUtils.searchAtList(argument1, args[1]);
        if(args.length==3) {
            if(args[1].equals("대화문")) return argumentDialog3;
            else if(args[1].equals("퀘스트")) return argumentQuest3;
        }
        if(args.length==4) {
            if(args[2].equals("목표완료")) return result;
            if(args[1].equals("대화문")) return TabCompleteUtils.searchAtSet(DialogQuestManager.getDialogData().keySet(), args[3]);
            else if (args[1].equals("퀘스트")) return TabCompleteUtils.searchAtSet(DialogQuestManager.getQuestData().keySet(), args[3]);
        }
        return result;
    }
}
