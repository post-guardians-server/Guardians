package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.data.PlayerData;
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

import static me.rukon0621.guardians.main.pfix;

public class PlayerDataCommand extends AbstractCommand {
    private static final main plugin = main.getPlugin();

    public PlayerDataCommand() {
        super("modifypdc", main.getPlugin());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;

        if(args.length==0) {
            usages(player);
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[0]);
        if(target==null) {
            Msg.send(player, "&c플레이어의 이름을 제대로 입력해주세요.", pfix);
            return true;
        }
        if(args.length==1) {
            usages(player);
            return true;
        }
        if(args.length<4) {
            usage(player, "설정", true);
            return true;
        }
        String dataType = args[1];
        if(dataType.equalsIgnoreCase("int")) {
            try {
                int value = Integer.parseInt(args[3]);
                PlayerData.getPlayerDataMap(target).put(args[2], value);
                Msg.send(player, "값을 설정했습니다.", pfix);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c제대로된 숫자를 입력해주세요.", pfix);
                return true;
            }
        }
        else if(dataType.equalsIgnoreCase("double")) {
            try {
                double value = Double.parseDouble(args[3]);
                PlayerData.getPlayerDataMap(target).put(args[2], value);
                Msg.send(player, "값을 설정했습니다.", pfix);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c제대로된 숫자를 입력해주세요.", pfix);
                return true;
            }
        }
        else if(dataType.equalsIgnoreCase("float")) {
            try {
                float value = Float.parseFloat(args[3]);
                PlayerData.getPlayerDataMap(target).put(args[2], value);
                Msg.send(player, "값을 설정했습니다.", pfix);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c제대로된 숫자를 입력해주세요.", pfix);
                return true;
            }
        }
        else if(dataType.equalsIgnoreCase("string")) {
            String value = args[3];
            PlayerData.getPlayerDataMap(target).put(args[2], value);
            Msg.send(player, "값을 설정했습니다.", pfix);
        }
        else {
            Msg.send(player, "&c올바른 데이터 타입을 입력해주세요.", pfix);
            Msg.send(player, "  &7데이터 종류");
            Msg.send(player, "  &7 - int");
            Msg.send(player, "  &7 - double");
            Msg.send(player, "  &7 - float");
            Msg.send(player, "  &7 - string");
            return true;
       }
        return true;
    }

    @Override
    protected void usage(Player player, String usage) {
        if (usage.equals("설정")) {
            Msg.send(player, "&6/modifyPdc <플레이어> <데이터 종류> <keyName> <value>");
            Msg.send(player, "  &7데이터 종류");
            Msg.send(player, "  &7 - int");
            Msg.send(player, "  &7 - double");
            Msg.send(player, "  &7 - float");
            Msg.send(player, "  &7 - string");
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length==1) return null;
        else if(args.length==2) {
            List<String> list = List.of(new String[]{"int", "double", "float", "string"});
            return TabCompleteUtils.search(list, args[1]);
        }
        else if (args.length==3) {
            if(sender instanceof Player player) {
                return TabCompleteUtils.search(PlayerData.getPlayerDataMap(player).keySet(), args[2]);
            }
        }
        return new ArrayList<>();
    }
}
