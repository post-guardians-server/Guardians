package me.rukon0621.guardians.mobType;

import me.rukon0621.guardians.commands.AbstractCommand;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MobTypeCommand extends AbstractCommand {
    private final MobTypeManager mobTypeManager = main.getPlugin().getMobTypeManager();

    public MobTypeCommand() {
        super("mobType", main.getPlugin());
        arguments.add("생성");
        arguments.add("삭제");
        arguments.add("등록");
        arguments.add("등록해제");
    }

    @Override
    protected void usage(Player player, String usage) {
        if(usage.equals("생성")) {
            Msg.send(player, "&6/몹타입 생성 <타입이름>");
        }
        else if(usage.equals("삭제")) {
            Msg.send(player, "&6/몹타입 삭제 <타입이름>");
        }
        else if(usage.equals("등록")) {
            Msg.send(player, "&6/몹타입 등록 <타입이름> <몹이름>");
        }
        else if(usage.equals("등록해제")) {
            Msg.send(player, "&6/몹타입 등록해제 <몹이름>");
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }
        if(args[0].equals("생성")) {
            if(args.length < 2) {
                usage(player, args[0]);
                return true;
            }
            mobTypeManager.createNewType(player, args[1]);
        }
        else if(args[0].equals("삭제")) {
            if(args.length < 2) {
                usage(player, args[0]);
                return true;
            }
            mobTypeManager.deleteType(player, args[1]);
        }

        else if(args[0].equals("등록")) {
            if(args.length < 3) {
                usage(player, args[0]);
                return true;
            }
            mobTypeManager.registerMobType(player, ArgHelper.sumArg(args, 2), args[1]);
        }
        else if(args[0].equals("등록해제")) {
            if(args.length < 2) {
                usage(player, args[0]);
                return true;
            }
            mobTypeManager.unregisterMobType(player, ArgHelper.sumArg(args, 1));
        }

        else {
            usages(player);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length==1) return TabCompleteUtils.searchAtList(arguments, args[0]);
        else if(args[0].equals("삭제")||args[0].equals("등록")) {
            if(args.length==2) return TabCompleteUtils.searchAtList(mobTypeManager.getTypes(), args[1]);
            else if (args.length==3 && args[0].equals("등록")) return TabCompleteUtils.searchAtList(mobTypeManager.getMobNames(), args[2]);
        }
        else if (args[0].equals("등록해제")) {
            if(args.length == 2) return TabCompleteUtils.searchAtSet(mobTypeManager.getMobTypeData().keySet(), args[1]);
        }
        return new ArrayList<>();
    }
}
