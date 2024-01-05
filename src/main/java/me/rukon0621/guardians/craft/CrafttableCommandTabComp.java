package me.rukon0621.guardians.craft;

import me.rukon0621.guardians.craft.craft.CraftManager;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CrafttableCommandTabComp implements TabCompleter {
    private static List<String> argument0 = new ArrayList<>();

    public CrafttableCommandTabComp() { //{"생성", "삭제", "목록", "리로드", "열기"}
        argument0.add("생성");
        argument0.add("삭제");
        argument0.add("목록");
        argument0.add("리로드");
        argument0.add("열기");
        argument0.add("즉시완료");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        else if(args.length==1) return TabCompleteUtils.searchAtList(argument0, args[0]);
        if(args[0].equals("생성")||args[0].equals("삭제")||args[0].equals("열기")) return TabCompleteUtils.searchAtSet(CraftManager.craftTableData.keySet(), args[1]);
        if(args[0].equals("즉시완료")&&args.length==2) return null;
        return result;
    }
}
