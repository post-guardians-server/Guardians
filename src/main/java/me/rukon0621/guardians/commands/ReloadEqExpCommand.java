package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.craft.craft.CraftManager;
import me.rukon0621.guardians.craft.recipes.RecipeManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.dropItem.DropManager;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.region.RegionManager;
import me.rukon0621.guardians.shop.ShopManager;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.guardians.story.StoryManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.main.pfix;

public class ReloadEqExpCommand implements CommandExecutor {

    public ReloadEqExpCommand() {
        main.getPlugin().getCommand("reloadEqExp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //main.getPlugin().getEquipmentExpManager().reload();
        return true;
    }
}
