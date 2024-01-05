package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.main;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Timer extends BukkitRunnable {
    int repeatNumber;

    public Timer(int repeatNumber, int delay, int periodTick) {
        this.repeatNumber = repeatNumber;
        runTaskTimer(main.getPlugin(), delay, periodTick);
    }

    @Override
    public void run() {
        repeatNumber--;
        if(repeatNumber==0) {
            cancel();
            onEnd();
            return;
        }
        execute();
    }

    public abstract void onEnd();

    public int getRepeatNumber() {
        return repeatNumber;
    }

    public abstract void execute();
}
