package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static me.rukon0621.guardians.main.pfix;

public class LocationSaver implements CommandExecutor {

    private static main plugin = main.getPlugin();
    public static HashMap<String, Location> locationData;

    public LocationSaver() {
        plugin.getCommand("location").setTabCompleter(new LocationSaverCommandTabComp());
        plugin.getCommand("location").setExecutor(this);
        reloadAllLocationData();
    }

    private static Configure getConfig() {
        return new Configure("locationSaver.yml", FileUtil.getOuterPluginFolder().getPath());
    }

    private static void reloadAllLocationData() {
        locationData = new HashMap<>();
        Configure config = getConfig();
        for (String name : config.getConfig().getKeys(false)) {
            locationData.put(name, config.getConfig().getLocation(name));
        }
    }

    private static String[] arguments = {"설정", "삭제", "이동"};

    //해당 플레이어를 해당 location으로 이동
    //존재하지 않는 위치라면 false return
    public static boolean tpToLoc(Player player, String locationName) {
        if(!locationData.containsKey(locationName)) return false;
        player.teleportAsync(locationData.get(locationName));
        return true;
    }
    public static Location getLocation(String locationName) {
        return locationData.get(locationName);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7위치 세이버 리로드중...");
            reloadAllLocationData();
            MsgUtil.cmdMsg(sender, "&a위치 세이버 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) return false;
        if(args.length<1) {
            usages(player);
            return true;
        }
        if (args[0].equals("설정")) {
            if(args.length < 2) {
                usage(player, "설정", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);

            if(locationData.containsKey(name)) {
                Msg.send(player, "&c성공적으로 기존 위치를 변경했습니다.", pfix);
            } else {
                Msg.send(player, "&a성공적으로 새로운 위치를 생성하셨습니다.", pfix);
            }
            Configure config = getConfig();
            config.getConfig().set(name, player.getLocation());
            config.saveConfig();
            reloadAllLocationData();
        }
        else if (args[0].equals("삭제")) {
            if(args.length < 2) {
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);

            if(!locationData.containsKey(name)) {
                Msg.send(player, "&c해당 이름의 위치는 존재하지 않는 위치입니다.", pfix);
                return true;
            }
            Configure config = getConfig();
            config.getConfig().set(name, null);
            config.saveConfig();
            reloadAllLocationData();
            Msg.send(player, "&6성공적으로 기존 위치를 삭제하셨습니다.", pfix);
        }
        else if (args[0].equals("이동")) {
            if(args.length < 2) {
                usage(player, "이동", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!tpToLoc(player, name)) {
                Msg.send(player, "&c해당 이름의 위치는 존재하지 않는 위치입니다.", pfix);
                return true;
            }
            Msg.send(player, "&a성공적으로 해당 위치로 이동하였습니다.", pfix);
        }
        else if (args[0].equals("목록")) {
            if(locationData.size()==0) {
                Msg.send(player, "&c아직 서버에 존재하는 위치 데이터가 없습니다.", pfix);
                return true;
            }
            Msg.send(player, "&a현재 서버에 존재하는 위치의 목록입니다.", pfix);
            for(String name : locationData.keySet()) {
                Msg.send(player, "&c" + name + "&7 : &f" + locationData.get(name).toString());
            }
        }
        else {
            usages(player);
        }
        return true;
    }
    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equalsIgnoreCase("설정")) {
            Msg.send(player, "&6/위치 설정 <위치이름>");
            Msg.send(player, "&7    해당 위치를 영구적으로 서버에 저장합니다.");
        }
        else if(arg.equalsIgnoreCase("삭제")) {
            Msg.send(player, "&6/위치 삭제 <위치이름>");
            Msg.send(player, "&7    해당 위치를 영구적으로 서버에서 삭제합니다.");
        }
        else if(arg.equalsIgnoreCase("이동")) {
            Msg.send(player, "&6/위치 이동 <위치이름>");
            Msg.send(player, "&7    해당 위치로 즉시 텔레포트합니다.");
        }
        else if(arg.equalsIgnoreCase("목록")) {
            Msg.send(player, "&6/위치 목록");
            Msg.send(player, "&7    서버에 존재하는 위치의 목록을 표시합니다.");
        }
        Msg.send(player, " ");
        if (forone) Msg.send(player, "&e└────────────────────────┘");
    }
    private void usages(Player player) {
        Msg.send(player, "&e┌────────────────────────┐");
        Msg.send(player, " ");
        for(String s : arguments) {
            usage(player, s, false);
        }
        Msg.send(player, "&e└────────────────────────┘");
    }
}
