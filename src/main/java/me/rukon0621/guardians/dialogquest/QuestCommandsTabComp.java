package me.rukon0621.guardians.dialogquest;

import me.rukon0621.guardians.helper.EnumUtils;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuestCommandsTabComp implements TabCompleter {
    private static final List<String> argument1 = new ArrayList<>();
    private static final List<String> useNameAtArg1 = new ArrayList<>();
    private static final List<String> questSorts = new ArrayList<>();
    private static final List<String> enums = new ArrayList<>();

    public QuestCommandsTabComp() {
        //{"생성", "삭제", "목록", "보상설정", "아이템설정","아이콘설정","추적", "추적비활성화","리로드"}
        useNameAtArg1.add("삭제");
        useNameAtArg1.add("보상설정");
        useNameAtArg1.add("아이템설정");
        useNameAtArg1.add("아이콘설정");
        useNameAtArg1.add("추적");
        useNameAtArg1.add("추적비활성화");
        argument1.addAll(useNameAtArg1);
        argument1.add("생성");
        argument1.add("목록");
        argument1.add("리로드");

        questSorts.add("몹처치");
        questSorts.add("아이템");
        questSorts.add("방문");
        questSorts.add("커스텀");
        argument1.add("쿨타임");
        enums.clear();
        enums.addAll(EnumUtils.getEnumStringList(DialogQuestManager.QuestType.class));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
        if(args.length==0) return new ArrayList<>();
        if(args.length==1) {
            return TabCompleteUtils.searchAtList(argument1, args[0]);
        }
        if(useNameAtArg1.contains(args[0])) {
            return TabCompleteUtils.searchAtSet(DialogQuestManager.getQuestData().keySet(), args[1]);
        }
        if(args[0].equals("쿨타임")) {
            if (args.length==3) return TabCompleteUtils.searchAtSet(DialogQuestManager.getQuestData().keySet(), args[2]);
        }
        else if(args[0].equals("생성")) {
            if(args.length==2) {
                return TabCompleteUtils.searchAtList(DialogQuestManager.getQuestFileNames(), args[1]);
            }
            else if (args.length==3) {
                return TabCompleteUtils.searchAtList(questSorts, args[2]);
            }
            else if (args.length==4) return TabCompleteUtils.searchAtList(enums, args[3].toUpperCase(Locale.ROOT));
        }

        return new ArrayList<>();
    }
}
