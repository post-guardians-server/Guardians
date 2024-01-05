package me.rukon0621.guardians.listeners;

import im.benta.minecraft.benta.event.EvtDepositSuccess;
import me.rukon0621.guardians.helper.Broadcaster;
import me.rukon0621.guardians.main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PaymentListener implements Listener {
    public PaymentListener() {
        main plugin = main.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPay(EvtDepositSuccess e) {
        Broadcaster.broadcast("&c" + e.getIdentifier() + "님이 " + e.getPaidAmount() + "만큼 흑우가 되셨습니다!!!!");
    }
}
