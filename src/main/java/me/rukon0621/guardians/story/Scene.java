package me.rukon0621.guardians.story;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.region.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;

public class Scene implements Listener {
    private static final main plugin = main.getPlugin();

    private boolean disabled = false;
    private final Player player;
    private final LinkedList<String> scripts;
    private final List<Scene> childScenes = new ArrayList<>();
    private final Set<BukkitRunnable> tasks = new HashSet<>();
    private final String name;
    private final Scene rootScene; //최상위씬 -> 최상위씬이 자신이면 this
    private final boolean hasListener;
    private boolean scriptsEnd = false;

    public Scene(Player player, List<String> scripts, @Nullable String name) {
        this(player, scripts, name, false);
    }

    public Scene(Player player, List<String> scripts, @Nullable String name, boolean hasListener) {
        this.name = name;
        this.player = player;
        this.scripts = new LinkedList<>(scripts);
        this.hasListener = hasListener;
        rootScene = this;
        if(!this.hasListener) Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        if(name==null) startScene(); //Name이 있는 스토리는 직접 start해야함
    }

    public Scene(Player player, List<String> scripts, @Nullable String name, boolean hasListener, Scene rootScene) {
        this.name = name;
        this.player = player;
        this.scripts = new LinkedList<>(scripts);
        this.hasListener = hasListener;
        if(!this.hasListener) Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.rootScene = rootScene;
        startScene();
    }

    public void startScene() {
        int delay = 0;
        Iterator<String> itr = scripts.iterator();
        while(itr.hasNext()) {
            if(disabled) return;
            String script = itr.next();
            String upperScript = script.toUpperCase(Locale.ROOT);

            //IF 문
            if(upperScript.startsWith("@IF")) {
                List<String> result = new ArrayList<>();
                boolean executable = false;
                do {
                    itr.remove();
                    if(!executable) {
                        if (upperScript.equals("@ELSE")) {
                            executable = true; //마지막 IF문이 false 라면 실행가능
                        }
                        else {
                            executable = StoryManager.checkIf(player, script);
                        }
                    }

                    int ifStack = 1;
                    List<String> subScripts = new ArrayList<>();
                    while (itr.hasNext()) {
                        script = itr.next();
                        itr.remove();
                        upperScript = script.toUpperCase(Locale.ROOT);
                        if (upperScript.startsWith("@IF") || upperScript.startsWith("@ELIF") || upperScript.startsWith("@ELSE")) {
                            ifStack++;
                        }
                        else if (upperScript.equals("@ENDIF")) {
                            ifStack--;
                        }
                        if (ifStack == 0) break;
                        subScripts.add(script);
                    }
                    if (executable && result.isEmpty()) result.addAll(subScripts);

                    if(!itr.hasNext()) break;
                    script = itr.next();
                    upperScript = script.toUpperCase(Locale.ROOT);
                } while(upperScript.startsWith("@ELIF")||upperScript.equals("@ELSE"));
                if(!result.isEmpty()) {
                    List<String> remain = new ArrayList<>(scripts);
                    scripts.clear();
                    scripts.addAll(result);
                    scripts.addAll(remain);
                }
                startScene();
                return;
            }

            itr.remove();
            if(upperScript.startsWith("!WAIT")) {
                delay = Integer.parseInt(script.split(":")[1]);
                break;
            }
            else if (upperScript.startsWith("!REPEAT") || upperScript.startsWith("@REPEAT")) {
                int repeat = Integer.parseInt(script.split(":")[1]);
                int repeatInterval = Integer.parseInt(script.split(":")[2]);
                List<String> subScripts = new ArrayList<>();
                while (itr.hasNext()) {
                    String innerScript = itr.next();
                    itr.remove();
                    if (innerScript.equals("!REPEAT_END") || innerScript.equals("@REPEAT_END")) {
                        break;
                    }
                    subScripts.add(innerScript);
                }
                new Repeater(subScripts, repeat, repeatInterval);
            }
            else {
                StoryManager.executeLine(player, script);
            }
        }
        if(!itr.hasNext() && delay == 0) {
            scriptsEnd = true;
            if(!hasListener) {
                RegionManager.getIgnoreRegionPlayers().remove(player);
            }
            //모든 차일드씬이 종료되지 않으면 기다림
            disableIfPossible();
            if(rootScene!=this) rootScene.disableIfPossible();
        }
        else {
            if(delay==0) {
                startScene();
            }
            else {
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        tasks.remove(this);
                        startScene();
                    }
                };
                runnable.runTaskLater(plugin, delay);
                tasks.add(runnable);
            }
        }
    }

    private void disableIfPossible() {
        if(disabled) return;
        if(canDisable()) {
            if(rootScene.equals(this)) {
                StoryManager.endStory(player, true);
            }
            else disable();
        }
    }

    /**
     * @return 모든 ChildScene 및 task가 disable 되었는가
     */
    private boolean canDisable() {
        if(!scriptsEnd) return false;
        for(Scene cs : childScenes) {
            if(!cs.canDisable()) return false;
        }
        for(BukkitRunnable runnable : tasks) {
            if(!runnable.isCancelled()) return false;
        }
        return true;
    }

    class Repeater extends BukkitRunnable {
        private int repeat;
        private final List<String> scripts;
        public Repeater(List<String> scripts, int repeat, int interval) {
            tasks.add(this);
            this.repeat = repeat;
            this.scripts = scripts;
            runTaskTimer(plugin, 0, interval);
        }

        @Override
        public void run() {
            if(repeat==0) {
                this.cancel();
                tasks.remove(this);
                return;
            }
            repeat--;
            childScenes.add(new Scene(player, scripts, null, false, rootScene));
        }
    }

    public String getName() {
        return name;
    }

    /**
     *  강제로 자신을 포함한 모든 자식 씬을 종료시킴
     */
    public void disable() {
        if(hasListener) HandlerList.unregisterAll(this);
        for(Scene scene : childScenes) {
            scene.disable();
        }
        for(BukkitRunnable runnable : tasks) {
            runnable.cancel();
        }
        disabled = true;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if(e.getPlayer().equals(player)) {
            StoryManager.endStory(player, false);

            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerData.setPlayerStun(player, false);
                }
            }.runTaskLater(plugin, 30);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if(e.getPlayer().equals(player)) {
            StoryManager.endStory(player, false);
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerData.setPlayerStun(player, false);
                }
            }.runTaskLater(plugin, 30);
        }
    }
}
