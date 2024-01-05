package me.rukon0621.guardians.story;

import me.rukon0621.guardians.helper.Rand;
import me.rukon0621.guardians.main;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CameraShaking extends BukkitRunnable {
    private final Player player;
    private int ticks;
    private final double strength;

    public CameraShaking(Player player, int ticks, double strength) {
        this(player, ticks, strength, true);
    }
    public CameraShaking(Player player, int ticks, double strength, boolean spectator) {
        if(spectator) player.setGameMode(GameMode.SPECTATOR);
        this.player = player;
        this.ticks = ticks;
        this.strength = strength;
        this.runTaskTimer(main.getPlugin(), 0, 1);
    }

    @Override
    public void run() {
        if (ticks <= 0) {
            cancel();
            return;
        }
        ticks--;
        Location loc = player.getLocation();
        loc.setPitch(loc.getPitch() + Rand.randFloat(-1 * strength, strength));
        loc.setYaw(loc.getYaw() + Rand.randFloat(-1 * strength, strength));
        player.teleportAsync(loc);
    }
}
