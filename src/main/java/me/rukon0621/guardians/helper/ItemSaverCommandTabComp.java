package me.rukon0621.guardians.helper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemSaverCommandTabComp implements TabCompleter {
    private static List<String> argument0 = new ArrayList<>();

    //"설정", "삭제", "지급", "목록"
    public ItemSaverCommandTabComp() {
        argument0.add("자동설정");
        argument0.add("설정");
        argument0.add("삭제");
        argument0.add("지급");
        argument0.add("목록");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String str, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return TabCompleteUtils.searchAtList(argument0, args[0]);
        if(args[0].equals("지급")||args[0].equals("삭제")||args[0].equals("설정")) {
            return ItemSaver.searchItemSaverNames(args[1]);
        }
        return result;
    }
}
