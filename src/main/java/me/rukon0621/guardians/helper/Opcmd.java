package me.rukon0621.guardians.helper;

import org.bukkit.entity.Player;

public class Opcmd {

    public static void opCmd(Player player, String cmd) {
        if(player.isOp()) {
            player.performCommand(cmd);
        }
        else {
            try {
                player.setOp(true);
                player.performCommand(cmd);
            } finally {
                player.setOp(false);
            }
        }
    }

}
