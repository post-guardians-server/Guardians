package me.rukon0621.guardians.story;

import me.rukon0621.guardians.main;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CameraMoving extends BukkitRunnable {
    private ArmorStand stand;
    private int ticks;
    private final double x;
    private final double y;
    private final double z;
    private final float pitch;
    private final float yaw;
    private final Location loc;
    private final Player player;

    public CameraMoving(Player player, double x, double y, double z, double yaw, double pitch, int ticks, double XTick, double YTick, double ZTick, float yawTick, float pitchTick) {
        this.player = player;
        this.ticks = ticks - 1;
        this.x = XTick;
        this.y = YTick;
        this.z = ZTick;
        this.pitch = pitchTick;
        this.yaw = yawTick;
        loc = player.getLocation();
        loc.set(x, y, z);
        loc.setPitch((float) pitch);
        loc.setYaw((float) yaw);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(loc);
        new BukkitRunnable() {
            @Override
            public void run() {
                stand = player.getLocation().getWorld().spawn(player.getLocation(), ArmorStand.class);
                stand.getChunk().load();
                stand.setPersistent(true);
                stand.setGravity(false);
                stand.setVisible(false);
                CameraMoving.this.runTaskTimer(main.getPlugin(), 0, 1);
            }
        }.runTaskLater(main.getPlugin(), 1);

    }


    @Override
    public void run() {
        if (ticks <= 0) {
            stand.remove();
            cancel();
            return;
        }
        ticks--;
        loc.getWorld().loadChunk(loc.getChunk());
        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(stand);
        loc.setPitch(loc.getPitch() + pitch);
        loc.setYaw(loc.getYaw() + yaw);
        loc.setX(loc.getX() + x);
        loc.setY(loc.getY() + y);
        loc.setZ(loc.getZ() + z);
        stand.teleport(loc);
    }
}
