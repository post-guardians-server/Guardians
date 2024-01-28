package me.rukon0621.guardians.spelluse;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.events.ItemClickEvent;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.PotionManager;
import me.rukon0621.guardians.main;
import me.rukon0621.rpvp.RukonPVP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static me.rukon0621.guardians.main.pfix;

public class SpellUseManager implements Listener {


    private final Map<String, String> spellMapper = new HashMap<>();
    private final Set<UUID> usingPerson = new HashSet<>();

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
    public void onQuit(PlayerQuitEvent e) {
        usingPerson.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChargableBuffItem(ItemClickEvent e) {
        if(!e.getItemData().getType().equals("차징형 버프 아이템")) return;
        if(!spellMapper.containsKey(Msg.uncolor(e.getItemData().getName()))) return;
        Player player = e.getPlayer();
        if(RukonPVP.inst().getPvpManager().isPlayerInBattleInstance(player)) {
            Msg.warn(player, "PVP장에서는 사용할 수 없습니다.");
            return;
        }
        if(new PlayerData(player).getLevel() < e.getItemData().getRequiredLevel()) {
            Msg.warn(player, "이 아이템의 요구 레벨보다 사용자의 레벨이 낮아 아이템을 사용할 수 없습니다.");
            return;
        }

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

        if(!usingPerson.add(player.getUniqueId())) {
            Msg.warn(player, "이미 아이템을 사용하고 있습니다.");
            return;
        }
        e.consume();
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0.8f);
        new BukkitRunnable() {
            private final double startHealth = player.getHealth();
            private final Location startLoc = player.getLocation();
            private int stage = 0;
            private final int goal = (int) (e.getItemData().getUsingTime() * 4D);
            @Override
            public void run() {
                PotionManager.effectGive(player, PotionEffectType.SLOW, 0.25, 2);
                if(!startLoc.getWorld().equals(player.getWorld()) || startLoc.distanceSquared(player.getLocation()) > 0.3) {
                    cancel();
                    Msg.warn(player, "움직여 사용이 취소되었습니다.");
                }
                else if (player.getHealth() < startHealth) {
                    cancel();
                    Msg.warn(player, "피해를 입어 사용이 취소되었습니다.");
                }
                else if(stage >= goal) {
                    Msg.sendTitle(player, "\uE00C", "&a" + "|".repeat(30), 8, 0, 10);
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
                    cancel();
                }
                else {
                    spell.cast(player);
                    int per = (int) Math.ceil((stage / (float) goal) * 30);
                    Msg.sendTitle(player, "\uE00C", "&a" + "|".repeat(per) + "&7" + "|".repeat(30 - per), 8, 0, 10);
                    player.playSound(player, Sound.BLOCK_POWDER_SNOW_HIT, 1, 0.8f);
                    stage++;
                }
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
                super.cancel();
                usingPerson.remove(player.getUniqueId());
            }
        }.runTaskTimer(main.getPlugin(), 0, 5);

    }

}
