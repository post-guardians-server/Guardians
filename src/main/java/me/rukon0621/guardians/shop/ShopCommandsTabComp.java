package me.rukon0621.guardians.shop;

import me.rukon0621.guardians.helper.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShopCommandsTabComp implements TabCompleter {

    private static List<String> argument0 = new ArrayList<>();

    public ShopCommandsTabComp() {
        //{"생성", "삭제", "목록", "열기", "아이템추가", "아이템삭제"}
        argument0.add("생성");
        argument0.add("삭제");
        argument0.add("목록");
        argument0.add("열기");
        argument0.add("아이템추가");
        argument0.add("아이템삭제");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return TabCompleteUtils.searchAtList(argument0, args[0]);

        if(args[0].equals("삭제")||args[0].equals("열기")) return TabCompleteUtils.searchAtSet(ShopManager.getShopData().keySet(), args[1]);
        if(args[0].equals("생성")) {
            if(args.length==2) {
                result.add("구매");
                result.add("판매");
                return TabCompleteUtils.searchAtList(result, args[1]);
            }
        }
        if(args[0].equals("아이템추가")||args[0].equals("아이템삭제")) {
            if(args.length>=3) return TabCompleteUtils.searchAtSet(ShopManager.getShopData().keySet(), args[2]);
        }
        return result;
    }
}
