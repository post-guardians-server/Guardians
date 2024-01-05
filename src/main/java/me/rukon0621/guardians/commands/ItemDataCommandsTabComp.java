package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.ItemGrade;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemDataCommandsTabComp implements TabCompleter {
    
    private static final List<String> argument1 = new ArrayList<>();
    private static final List<String> itemGrades  = new ArrayList<>();
    private static final List<String> stats  = new ArrayList<>();

    public ItemDataCommandsTabComp() {
        //{"기본값", "퀘스트아이템", "거래불가", "무기설정","방어구설정","행운력설정","레벨", "세이버", "속성설정", "속성목록", "타입목록", "리로드"}
        argument1.add("기본값");
        argument1.add("퀘스트아이템");
        argument1.add("중요한물건");
        argument1.add("요구무기타입");
        argument1.add("거래불가");
        argument1.add("가공불가");
        argument1.add("영혼추출불가");
        argument1.add("무기설정");
        argument1.add("방어구설정");
        argument1.add("행운력설정");
        argument1.add("장신구설정");
        argument1.add("스텟설정");
        argument1.add("요구레벨설정");
        argument1.add("지속시간");
        argument1.add("레벨");
        argument1.add("강화");
        argument1.add("등급");
        argument1.add("수치");
        argument1.add("품질");
        argument1.add("유효시즌");
        argument1.add("가공가능횟수");
        argument1.add("세이버");
        argument1.add("속성설정");
        argument1.add("속성목록");
        argument1.add("타입목록");
        argument1.add("리로드");

        for(ItemGrade grade : ItemGrade.values()) {
            itemGrades.add(grade.toString());
        }

        for(Stat stat : Stat.values()) {
            stats.add(stat.toString());
        }

        Collections.sort(argument1);
    }
    
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return argument1;
        if(args.length==2) {
            if (args[0].equals("기본값")) {
                return TabCompleteUtils.searchAtSet(TypeData.getTypeMap().keySet(), args[1]);
            }
            else if (args[0].equals("스텟설정")) {
                return TabCompleteUtils.searchAtList(stats, args[1].toUpperCase());
            }
            else if (args[0].equals("등급")) {
                return TabCompleteUtils.searchAtList(itemGrades, args[1].toUpperCase());
            }
            else if (args[0].equals("요구무기타입")) {
                return TabCompleteUtils.searchAtSet(TypeData.getTypeMap().keySet(), args[1]);
            }
            else if (args[0].equals("세이버")) {
                return ItemSaver.searchItemSaverNames(args[1]);
            }
        }
        if(args[0].equals("속성설정")) {
            if(args.length==3) return TabCompleteUtils.searchAtSet(ItemData.getAttrList(), args[2]);
        }

        return result;
    }
}
