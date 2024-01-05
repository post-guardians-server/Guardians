package me.rukon0621.guardians.areawarp;

import me.rukon0621.guardians.helper.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AreaCommandTabComp implements TabCompleter {
    private static List<String> argument0 = new ArrayList<>();

    //{"생성", "삭제", "위치수정", "아이콘수정", "이동", "리로드"};

    public AreaCommandTabComp() {
        argument0.add("생성");
        argument0.add("삭제");
        argument0.add("위치수정");
        argument0.add("귀환설정");
        argument0.add("아이콘수정");
        argument0.add("이동");
        argument0.add("리로드");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return TabCompleteUtils.searchAtList(argument0, args[0]);
        if(args[0].equals("리로드")||args[0].equals("목록")) return result;
        return TabCompleteUtils.searchAtSet(AreaManger.getAreaData().keySet(), args[1]);
    }
}
