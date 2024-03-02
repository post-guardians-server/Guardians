package me.rukon0621.guardians.spawnerUtils;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.chat.ColorString;
import io.lumine.mythic.bukkit.utils.numbers.RandomInt;
import io.lumine.mythic.core.spawning.spawners.MythicSpawner;
import io.lumine.mythic.core.spawning.spawners.SpawnerManager;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import static me.rukon0621.guardians.main.pfix;

public class SpawnUtilCommand implements CommandExecutor {
    public String[] arguments = {"생성","삭제","복사","설정"};
    private final MythicBukkit mythicPlugin = MythicBukkit.inst();
    private final SpawnUtilCommandTabComp spawnUtilCommandTabComp = new SpawnUtilCommandTabComp();

    public SpawnUtilCommand() {
        Objects.requireNonNull(main.getPlugin().getCommand("spawnutil")).setExecutor(this);
        Objects.requireNonNull(main.getPlugin().getCommand("spawnutil")).setTabCompleter(spawnUtilCommandTabComp);

        SpawnerManager spawnerManager = mythicPlugin.getSpawnerManager();
        for(MythicSpawner spawner : spawnerManager.getSpawners()) {
            spawner.setLeashRange(40);
        }
        spawnerManager.saveSpawners();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }

        SpawnerManager spawnerManager = mythicPlugin.getSpawnerManager();

        if(args[0].equals("생성")) {
            if(args.length < 6) {
                usage(player, "생성", true);
                return true;
            }

            String spawnerName = args[1];

            if(spawnerManager.getSpawnerByName(spawnerName)!=null) {
                Msg.warn(player, "이미 존재하는 이름의 스포너입니다.");
                return true;
            }

            String mobName = args[2];
            String levelRange = args[3];
            int maxMobs, radius;
            try {
                maxMobs = Integer.parseInt(args[5]);
                radius = Integer.parseInt(args[4]);
            } catch (Exception e) {
                Msg.warn(player, "제대로된 정수 값을 입력해주세요.");
                return true;
            }

            RandomInt level;
            try {
                if(levelRange.contains("to")) {
                    level = new RandomInt(args[3]);
                }
                else if(levelRange.contains(":")) {
                    levelRange = levelRange.replaceAll(":","to");
                    level = new RandomInt(levelRange);
                }
                else level = new RandomInt(1);
            } catch (Exception e) {
                Msg.warn(player, "&c레벨 정보가 잘못 입력되었습니다. 레벨 형식은 다음과 같습니다. test:1to10 (1레벨에서 10레벨 사이의 test를 스폰) 또는 test:3 (3레벨의 test를 스폰)");
                return true;
            }

            if(mythicPlugin.getMobManager().getMythicMob(mobName).isEmpty()) {
                Msg.warn(player, "해당 몹은 존재하지 않는 미스틱몹입니다.", pfix);
                return true;
            }
            Location loc = player.getLocation();
            MythicSpawner spawner = spawnerManager.createSpawner(spawnerName, loc, mobName);
            spawner.setMobLevel(level);
            spawnerManager.setSpawnerAttribute(spawner, "maxmobs", String.valueOf(maxMobs));
            spawnerManager.setSpawnerAttribute(spawner, "radius", String.valueOf(radius));
            spawnerManager.setSpawnerAttribute(spawner, "leashrange", "-1");
            spawnerManager.setSpawnerAttribute(spawner, "resetthreatonleash", "true");
            spawnerManager.setSpawnerAttribute(spawner, "healonleash", "true");
            spawnUtilCommandTabComp.reload();
            Msg.send(player, "성공적으로 스포너를 생성하였습니다.", pfix);
        }
        else if (args[0].equals("삭제")) {
            if(args.length < 2) {
                usage(player, "삭제",true);
                return true;
            }
            String spawnerName = args[1];
            MythicSpawner spawner = spawnerManager.getSpawnerByName(spawnerName);
            if(spawner==null) {
                Msg.warn(player, "해당 이름의 스포너는 존재하지 않습니다.");
                return true;
            }
            spawnerManager.removeSpawner(spawner);
            spawnUtilCommandTabComp.reload();
            Msg.send(player, "성공적으로 스포너를 삭제했습니다.", pfix);
        }
        else if (args[0].equals("설정")) {
            if(args.length < 4) {
                usage(player, "설정",true);
                return true;
            }
            String spawnerName = args[1];
            MythicSpawner spawner = spawnerManager.getSpawnerByName(spawnerName);
            if(spawner==null) {
                Msg.warn(player, "해당 이름의 스포너는 존재하지 않습니다.");
                return true;
            }
            try {
                if(spawnerManager.setSpawnerAttribute(spawner, args[2], args[3])) {
                    Msg.send(player, "성공적으로 설정하였습니다.", pfix);
                    return true;
                }
                else {
                    showSpawnerAttributes(player);
                    Msg.warn(player, "해당 속성은 존재하지 않는 속성입니다.");
                    return true;
                }

            } catch (Exception e) {
                Msg.warn(player, "해당 속성에 해당 값을 넣을 수 없습니다.");
                return true;
            }
        }
        else if (args[0].equals("다중설정")) {
            if(args.length < 4) {
                usage(player, args[0],true);
                return true;
            }

            ArrayList<MythicSpawner> spawners = new ArrayList<>();

            for(MythicSpawner spawner : spawnerManager.getSpawners()) {
                if(spawner.getName().startsWith(args[1])) {
                    spawners.add(spawner);
                }
            }

            if(spawners.size() == 0) {
                Msg.warn(player, "해당 문자열로 시작하는 스포너가 존재하지 않습니다.");
                return true;
            }
            try {
                if(spawnerManager.setSpawnerAttribute(spawners.get(0), args[2], args[3])) {
                    for(MythicSpawner spawner : spawners) {
                        spawnerManager.setSpawnerAttribute(spawner, args[2], args[3]);
                        Msg.send(player, "&7 - " + spawner.getName());
                    }
                    Msg.send(player, "성공적으로 설정하였습니다.", pfix);
                }
                else {
                    showSpawnerAttributes(player);
                    Msg.warn(player, "해당 속성은 존재하지 않는 속성입니다.");
                }
                return true;

            } catch (Exception e) {
                Msg.warn(player, "해당 속성에 해당 값을 넣을 수 없습니다.");
                return true;
            }
        }
        else if (args[0].equals("복사")) {
            if(args.length < 3) {
                usage(player, "복사", true);
                return true;
            }
            String spawnerName = args[1];
            MythicSpawner spawner = spawnerManager.getSpawnerByName(spawnerName);
            if(spawner==null) {
                Msg.warn(player, "해당 이름의 스포너는 존재하지 않습니다.");
                return true;
            }
            String newName = args[2];
            if(spawnerManager.getSpawnerByName(newName)!=null) {
                Msg.warn(player, "복사될 스포너의 이름이 이미 존재합니다.");
                return true;
            }
            if(spawnerManager.copySpawner(spawnerName, newName, BukkitAdapter.adapt(player.getLocation()))) {
                Msg.send(player, "성공적으로 복사했습니다.",pfix);
            }
            else {
                Msg.warn(player, "스포너 복사에 실패했습니다.", pfix);
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
        if(arg.equals("생성")) {
            Msg.send(player, "&6/스포너 생성 <이름> <몹이름> <레벨> <범위> <최대몹>");
            Msg.send(player, "&7   기본적인 스포너를 자신의 위치에 생성합니다..");
        }
        else if(arg.equals("설정")) {
            Msg.send(player, "&6/스포너 설정 <이름> <속성> <값>");
            Msg.send(player, "&7   스포너의 속성을 설정합니다.");
            if(forone) {
                Msg.send(player,   " ");
                showSpawnerAttributes(player);
            }
        }
        else if(arg.equals("다중설정")) {
            Msg.send(player, "&6/스포너 다중설정 <이름> <속성> <값>");
            Msg.send(player, "&7   해당 이름으로 시작하는 모든 스포너의 속성을 변경합니다.");
            if(forone) {
                Msg.send(player,   " ");
                showSpawnerAttributes(player);
            }
        }
        else if(arg.equals("삭제")) {
            Msg.send(player, "&6/스포너 삭제 <이름>");
            Msg.send(player, "&7   스포너의 특징을 설정합니다.");
        }
        else if(arg.equals("복사")) {
            Msg.send(player, "&6/스포너 복사 <원본이름> <사본이름>");
            Msg.send(player, "&7   현재 위치에 기존 스포너를 새로운 이름으로 복제합니다.");
        }
        Msg.send(player, " ");
        if (forone) Msg.send(player, "&e└────────────────────────┘");
    }

    private void showSpawnerAttributes(Player player) {
        Msg.send(player, ColorString.get("&6--====|||| &c&lMythicMobs &6||||====--"));
        Msg.send(player, "&eCommand: &b/mm spawners set [name] [attribute] [value]");
        Msg.send(player, "&e&lAvailable Attributes:");
        Msg.send(player, ChatColor.GOLD + "activationrange" + ChatColor.GREEN + ChatColor.ITALIC + " - The max range a player can be for the spawner to activate.");
        Msg.send(player, ChatColor.GOLD + "cooldown" + ChatColor.GREEN + ChatColor.ITALIC + " - The time (in seconds) between mob spawns.");
        Msg.send(player, ChatColor.GOLD + "group" + ChatColor.GREEN + ChatColor.ITALIC + " - The name of a group to organize the spawner into.");
        Msg.send(player, ChatColor.GOLD + "healonleash" + ChatColor.GREEN + ChatColor.ITALIC + " - (true/false) Whether or not a mob will heal to full health upon being leashed.");
        Msg.send(player, ChatColor.GOLD + "leashrange" + ChatColor.GREEN + ChatColor.ITALIC + " - The max range a mob can be from the spawner before it is teleported back (0=none).");
        Msg.send(player, ChatColor.GOLD + "maxmobs" + ChatColor.GREEN + ChatColor.ITALIC + " - The maximum number of mobs this spawner can have active at once.");
        Msg.send(player, ChatColor.GOLD + "moblevel" + ChatColor.GREEN + ChatColor.ITALIC + " - The level of the mobs spawned by the spawner.");
        Msg.send(player, ChatColor.GOLD + "mobsperspawn" + ChatColor.GREEN + ChatColor.ITALIC + " - How many mobs will spawn per spawner cycle.");
        Msg.send(player, ChatColor.GOLD + "resetthreatonleash" + ChatColor.GREEN + ChatColor.ITALIC + " - (true/false) Whether or not the mob's target should be reset upon being leashed.");
        Msg.send(player, ChatColor.GOLD + "showflames" + ChatColor.GREEN + ChatColor.ITALIC + " - (true/false) whether to always show spawner flames on the block.");
        Msg.send(player, ChatColor.GOLD + "usetimer" + ChatColor.GREEN + ChatColor.ITALIC + " - (true/false) whether the spawner should spawn on a timer or not..");
        Msg.send(player, ChatColor.GOLD + "warmup" + ChatColor.GREEN + ChatColor.ITALIC + " - The time (in seconds) until a mob spawns after a mob dies, when the max number of mobs has been reached.");
        Msg.send(player, " ");
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
