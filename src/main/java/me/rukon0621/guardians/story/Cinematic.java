package me.rukon0621.guardians.story;

import me.rukon0621.guardians.main;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Cinematic {
    private static final main plugin = main.getPlugin();
    private ArmorStand stand;
    private final Player player;
    private int tick;
    private double dx, dy, dz;
    private final float dw, dp;

    public Cinematic(Player player, int tick, double speed) {
        this(player, tick, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                player.getLocation().getYaw(), player.getLocation().getPitch(),
                speed, speed, speed, 0, 0);
    }

    public Cinematic(Player player, int tick, double speed, double dw, double dp) {
        this(player, tick, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                player.getLocation().getYaw(), player.getLocation().getPitch(),
                speed, speed, speed, dw, dp);
    }

    public Cinematic(Player player, int tick, double dx, double dy, double dz, double dw, double dp) {
        this(player, tick, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                player.getLocation().getYaw(), player.getLocation().getPitch(),
                dx, dy, dz, dw, dp);
    }

    public Cinematic(Player player, int tick, double x, double y, double z, double w, double p, double speed) {
        this(player, tick, x, y, z, w, p, speed, speed, speed, 0, 0);
    }

    public Cinematic(Player player, int tick, double x, double y, double z, double w, double p, double speed, double dw, double dp) {
        this(player, tick, x, y, z, w, p, speed, speed, speed, dw, dp);
    }

    public Cinematic(Player player, int tick, double x, double y, double z, double w, double p, double dx, double dy, double dz, double dw, double dp) {
        this.player = player;
        this.tick = tick - 1;
        Location loc = new Location(player.getWorld(), x, y, z, (float) w, (float) p);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(loc);
        loc.getChunk().load(false);
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.dw = (float) dw;
        this.dp = (float) dp;

        new BukkitRunnable() {
            @Override
            public void run() {
                if(player.getLocation().getWorld().equals(loc.getWorld()) && player.getLocation().distanceSquared(loc)>1) return;
                cancel();

                //위치 이동 확인
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        start();
                    }
                }.runTask(plugin);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);

    }

    //시네마틱 진행
    private void start() {
        stand = player.getLocation().getWorld().spawn(player.getLocation(), ArmorStand.class);
        stand.setGravity(false);
        stand.setVisible(false);
        Vector vec = player.getLocation().getDirection();
        dx = vec.getX() * dx;
        dy = vec.getY() * dy;
        dz = vec.getZ() * dz;
        new Repeater().runTaskTimer(plugin, 0, 1);
    }

    class Repeater extends BukkitRunnable {

        @Override
        public void run() {
            if(tick==0) {
                cancel();
                stand.remove();
                return;
            }
            tick--;
            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(stand);
            Location loc = stand.getLocation();
            loc.setX(loc.getX() + dx);
            loc.setY(loc.getY() + dy);
            loc.setZ(loc.getZ() + dz);
            loc.setYaw(loc.getYaw() + dw);
            loc.setPitch(loc.getPitch() + dp);
            if(!loc.getChunk().isLoaded()) loc.getChunk().load(false);
            stand.teleport(loc);

        }
    }

}
