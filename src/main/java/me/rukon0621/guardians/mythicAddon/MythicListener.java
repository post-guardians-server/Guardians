package me.rukon0621.guardians.mythicAddon;

import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.mythicAddon.machanics.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicListener implements Listener {

    public MythicListener() {
        main plugin = main.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLoadMechanics(MythicMechanicLoadEvent e) {
        if(e.getMechanicName().equalsIgnoreCase("ExecuteSpell")) {
            e.register(new ExecuteSpellMechanic(e.getConfig()));
        }
        else if(e.getMechanicName().equalsIgnoreCase("dam")) {
            e.register(new DamMechanic(e.getContainer().getManager(), e.getConfig().getLine(), e.getConfig()));
        }
        else if(e.getMechanicName().equalsIgnoreCase("damper")) {
            e.register(new DamPerMechanic(e.getContainer().getManager(), e.getConfig().getLine(), e.getConfig()));
        }
        else if(e.getMechanicName().equalsIgnoreCase("shake")) {
            e.register(new ShakeMechanic(e.getContainer().getManager(), e.getConfig().getLine(), e.getConfig()));
        }
        else if(e.getMechanicName().equalsIgnoreCase("flashbang")) {
            e.register(new FlashbangMechanic(e.getContainer().getManager(), e.getConfig().getLine(), e.getConfig()));
        }
    }

}
