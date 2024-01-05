package me.rukon0621.guardians.story;

import me.rukon0621.guardians.helper.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StoryCommandsTabComp implements TabCompleter {

    private static final List<String> argument0 = new ArrayList<>();
    private static final List<String> argument2 = new ArrayList<>();

    public StoryCommandsTabComp() {
        argument0.add("생성");
        argument0.add("삭제");
        argument0.add("중단");
        argument0.add("진행");
        argument0.add("데이터");
        argument0.add("목록");
        argument0.add("리로드");
        argument2.add("완료");
        argument2.add("미완료");

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return TabCompleteUtils.searchAtList(argument0, args[0]);
        if(args[0].equals("삭제")||args[0].equals("진행")) {
            return TabCompleteUtils.searchAtSet(StoryManager.getStoryData().keySet(), args[1]);
        }
        if(args[0].equals("생성")) {
            if(args.length==2) {
                return TabCompleteUtils.searchAtList(StoryManager.getPathList(), args[1]);
            }
        }
        if(args[0].equals("데이터")) {
            if(args.length==2) return null;
            if(args.length==3) return TabCompleteUtils.searchAtList(argument2, args[2]);
            if(args.length==4) return TabCompleteUtils.searchAtSet(StoryManager.getStoryData().keySet(), args[3]);
        }
        return result;
    }
}
