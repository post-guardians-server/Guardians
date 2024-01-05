package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.pfix;

public class GetLocCommand implements CommandExecutor {

    public GetLocCommand() {
        main.getPlugin().getCommand("getloc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        Location loc = player.getLocation();
        String str = String.format("%.2f:%.2f:%.2f:%.2f:%.2f", loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        Msg.send(player, new ComponentBuilder(Msg.color(pfix + "&a&l이곳을 클릭&6하여 위치를 복사할 수 있습니다."))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(Msg.color("&7클릭하여 위치를 복사합니다.")).create()))
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str))
                .create());
        return true;
    }
}
