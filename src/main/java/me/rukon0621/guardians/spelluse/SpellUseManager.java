package me.rukon0621.guardians.spelluse;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import me.rukon0621.guardians.events.ItemClickEvent;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

import static me.rukon0621.guardians.main.pfix;

public class SpellUseManager implements Listener {


    private final Map<String, String> spellMapper = new HashMap<>();

    public Configure getConfig() {
        return new Configure(FileUtil.getOuterPluginFolder() + "/chargableItemSpellMap.yml");
    }

    public void reload() {
        Configure conf = getConfig();
        spellMapper.clear();
        for(String key : conf.getConfig().getKeys(false)) {
            spellMapper.put(key, conf.getConfig().getString(key));
        }
    }

    public SpellUseManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, main.getPlugin());
        reload();
    }

    @EventHandler
    public void onChargableBuffItem(ItemClickEvent e) {
        if(!e.getItemData().getType().equals("차징형 버프 아이템")) return;
        if(!spellMapper.containsKey(Msg.uncolor(e.getItemData().getName()))) return;
        Player player = e.getPlayer();
        String spellName = spellMapper.get(Msg.uncolor(e.getItemData().getName()));
        Spell spell = MagicSpells.getSpellByInternalName(spellName);
        if(spell == null) {
            Msg.warn(player, spellName + " - 이 스펠은 존재하지 않습니다.", pfix);
            return;
        }
        if(spell.getCooldown(player) > 0) {
            Msg.send(player, String.format("이 스킬을 사용하려면 %f.1초를 기다려야합니다.", spell.getCooldown(player)), pfix);
            return;
        }
        e.consume();
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0.8f);
        new BukkitRunnable() {
            private final double startHealth = player.getHealth();
            private final Location startLoc = player.getLocation();
            private int stage = 0;
            private final int goal = (int) (e.getItemData().getUsingTime() * 10D);
            @Override
            public void run() {
                if(startLoc.distanceSquared(player.getLocation()) > 0.3) {
                    cancel();
                    Msg.warn(player, "움직여 사용이 취소되었습니다.");
                }
                else if (player.getHealth() < startHealth) {
                    cancel();
                    Msg.warn(player, "피해를 입어 사용이 취소되었습니다.");
                }
                else if(stage >= goal) {
                    cancel();
                }
                else {
                    spell.cast(player);
                    int per = (int) Math.ceil((stage / (float) goal) * 25);
                    Msg.sendTitle(player, "\uE00C", "&a" + "|".repeat(per) + "&7" + "|".repeat(25 - per), 4, 0, 10);
                    player.playSound(player, Sound.BLOCK_POWDER_SNOW_HIT, 1, 0.8f);
                    stage++;
                }
            }
        }.runTaskTimer(main.getPlugin(), 0, 5);

    }

}
