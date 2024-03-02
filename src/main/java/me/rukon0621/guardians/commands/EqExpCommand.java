package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.main;
import me.rukon0621.rkutils.bukkit.command.AbstractCommand;
import me.rukon0621.rkutils.bukkit.command.SubCommand;
import me.rukon0621.rkutils.bukkit.util.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.rkutils.RkUtils.pfix;

public class EqExpCommand extends AbstractCommand {
    public EqExpCommand() {
        super(main.getPlugin(), "사증", false);
        addCommand(new SubCommand("사증", "리로드") {
            @Override
            public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
                return new ArrayList<>();
            }

            @Override
            public void execute(CommandSender commandSender, String[] strings) {
                //main.getPlugin().getEquipmentExpManager().reload();
                Msg.send(commandSender, "사증 데이터가 리로드 되었습니다.", pfix);
            }
        });
    }
}
