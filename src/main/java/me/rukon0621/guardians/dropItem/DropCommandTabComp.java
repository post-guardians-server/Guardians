package me.rukon0621.guardians.dropItem;

import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DropCommandTabComp implements TabCompleter {
    private static final List<String> argument0 = new ArrayList<>();

    public DropCommandTabComp() {
        argument0.add("생성");
        argument0.add("삭제");
        argument0.add("목록");
        argument0.add("얻기");
        argument0.add("리로드");
        argument0.add("배율");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return TabCompleteUtils.searchAtList(argument0, args[0]);
        if(args[0].equals("생성")) {
            if(args.length==2) {
                return TabCompleteUtils.searchAtList(DropManager.getDropPathList(), args[1]);
            }
            else if (args.length==3) {
                return TabCompleteUtils.searchAtList(main.getPlugin().getMobTypeManager().getMobNames(), args[2]);
            }
        }
        if(args[0].equals("삭제")) {
            if(args.length==2) return TabCompleteUtils.searchAtSet(DropManager.getDropNames(), args[1]);
        }
        else if(args[0].equals("얻기")) {
            if(args.length==4) return TabCompleteUtils.searchAtSet(DropManager.getDropNames(), args[3]);
        }
        return result;
    }
}
