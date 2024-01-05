package me.rukon0621.guardians.region;

import me.rukon0621.guardians.helper.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RegionCommandsTabComp implements TabCompleter {
    private static List<String> argument0 = new ArrayList<>();

    public RegionCommandsTabComp() {
        argument0.add("생성");
        argument0.add("삭제");
        argument0.add("수정");
        argument0.add("목록");
        argument0.add("리로드");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return TabCompleteUtils.searchAtList(argument0, args[0]);
        if(args.length>1) {
            if(args[0].equals("삭제")||args[0].equals("수정")) return TabCompleteUtils.searchAtSet(RegionManager.getRegions().keySet(), args[1]);
        }

        return result;
    }
}
