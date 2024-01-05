package me.rukon0621.guardians.spawnerUtils;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.core.spawning.spawners.MythicSpawner;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnUtilCommandTabComp implements TabCompleter, Listener {
    private List<String> argument0 = new ArrayList<>();
    private MythicBukkit mythicPlugin = MythicBukkit.inst();
    private List<String> mobs = new ArrayList<>();
    private List<String> spawners = new ArrayList<>();
    private List<String> attrs = new ArrayList<>();

    public SpawnUtilCommandTabComp() {
        main.getPlugin().getServer().getPluginManager().registerEvents(this, main.getPlugin());
        reload();
        attrs.add("resetthreatonleash");
        attrs.add("healonleash");
        attrs.add("leashrange");
        attrs.add("breakable");
        attrs.add("checkforplayers");
        attrs.add("flames");
        attrs.add("moblevel");
        attrs.add("radius");
        attrs.add("warmup");
        attrs.add("usetimer");
        attrs.add("cooldown");
        attrs.add("yaw");
        attrs.add("pitch");
        attrs.add("group");
        attrs.add("leash");
        attrs.add("mobsperspawn");
        attrs.add("scalingrange");
        attrs.add("mobtype");
        attrs.add("maxmobs");
        attrs.add("activationrange");
        attrs.add("showflames");
    }

    @EventHandler
    public void onReloaded(MythicReloadedEvent e) {
        mythicPlugin = e.getInstance();
    }

    public void reload() {
        argument0.add("생성");
        argument0.add("삭제");
        argument0.add("다중설정");
        argument0.add("설정");
        argument0.add("복사");
        mobs = mythicPlugin.getMobManager().getMobNames().stream().toList();
        spawners = new ArrayList<>();
        for(MythicSpawner spawner : mythicPlugin.getSpawnerManager().getSpawners()) {
            spawners.add(spawner.getName());
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if(args.length==0) return result;
        if(args.length==1) return TabCompleteUtils.searchAtList(argument0, args[0]);
        if(args[0].equals("생성")) {
            if(args.length==2) return TabCompleteUtils.searchAtList(spawners, args[1]);
            if(args.length==3) return TabCompleteUtils.searchAtList(mobs, args[2]);
        }
        else if(args[0].equals("삭제")) {
            if(args.length==2) return TabCompleteUtils.searchAtList(spawners, args[1]);
        }
        else if(args[0].endsWith("설정")) {
            if(args.length==2) return TabCompleteUtils.searchAtList(spawners, args[1]);
            if(args.length==3) return TabCompleteUtils.searchAtList(attrs, args[2]);
        }
        else if(args[0].equals("복사")) {
            return TabCompleteUtils.searchAtList(spawners, args[1]);
        }
        return result;
    }
}
