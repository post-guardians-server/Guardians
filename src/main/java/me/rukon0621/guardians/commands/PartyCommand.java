package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.DamagingListener;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.party.PartyManager;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.rinstance.RukonInstance;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyCommand implements CommandExecutor {


    public PartyCommand() {
        main.getPlugin().getCommand("partyMenu").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player player)) return false;
        if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(player) || DamagingListener.getRemainCombatTime(player) > -1 || StoryManager.getPlayingStory(player) != null) {
            Msg.warn(player, "지금은 이용할 수 없습니다.");
            return true;
        }
        PartyManager.openPartyGUI(player);
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1.5f);
        return true;
    }
}
