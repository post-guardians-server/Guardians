package me.rukon0621.guardians.dialogquest;

import me.rukon0621.guardians.helper.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DialogCommandsTabComp implements TabCompleter {

    private static List<String> argument1;

    public DialogCommandsTabComp() {
        argument1 = new ArrayList<>();
        argument1.add("생성");
        argument1.add("삭제");
        argument1.add("등록");
        argument1.add("목록");
        argument1.add("등록목록");
        argument1.add("리로드");
     }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String str, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) {
            return TabCompleteUtils.searchAtList(argument1, args[0]);
        }
        if(args[0].equals("삭제")||args[0].equals("등록")) {
            return getDialogNamesStartedWith(args[1]);
        }
        if (args[0].equals("생성")) {
            if(args.length==2) {
                return TabCompleteUtils.searchAtList(DialogQuestManager.getDialogFileNames(), args[1]);
            }
            if(args.length==3) {
                return getDialogNamesStartedWith(args[2]);
            }
        }
        return result;
    }

    private List<String> getDialogNamesStartedWith(String startWith) {
        List<String> result = new ArrayList<>();
        for(String s : DialogQuestManager.getDialogData().keySet()) {
            if(s.toLowerCase().contains(startWith.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
