package me.rukon0621.guardians.skillsystem;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.region.RegionManager;
import me.rukon0621.ridings.RukonRiding;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class RuneSkillManager implements Listener {
    private static final main plugin = main.getPlugin();

    public RuneSkillManager() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void castRuneSkill(Player player, String skillName, int level, ItemData itemData) {
        Skill skill = SkillManager.getSkill(skillName);
        skill.cast(player, level, itemData);
    }

    @EventHandler
    public void onCastRuneSkills(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        if(player.getGameMode().equals(GameMode.SPECTATOR)) {
            e.setCancelled(true);
            return;
        }
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(RegionManager.playerInWardrobe(player)) return;
        int slot = e.getNewSlot();
        if(slot==0||slot>3) return;
        e.setCancelled(true);
        if(RukonRiding.inst().getRideManager().isPlayerRiding(player)) {
            RukonRiding.inst().getRideManager().executeRidingSkill(player, slot);
            return;
        }
        HashMap<String, ItemStack> equipData = EquipmentManager.getEquipmentData(player);
        ItemStack rune = equipData.get("룬"+slot);
        if(!rune.getType().equals(Material.AIR)&&!rune.getType().equals(Material.BARRIER)) {
            ItemData idata = new ItemData(rune);
            String skillName = Msg.uncolor(idata.getName()).split(":")[1].trim();
            castRuneSkill(player, skillName, idata.getLevel(), idata);
        }
        player.getInventory().setHeldItemSlot(e.getPreviousSlot());
    }
}
