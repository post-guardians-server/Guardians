package me.rukon0621.guardians.commands;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.variables.Variable;
import me.rukon0621.guardians.helper.DataBase;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Serializer;
import me.rukon0621.guardians.helper.SoloMode;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class test extends AbstractCommand {
    public test() {
        super("test", main.getPlugin());
    }

    @Override
    protected void usage(Player player, String usage) {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        DataBase db = new DataBase("guardians");
        ResultSet set = db.executeQuery(String.format("SELECT * FROM chestData", player.getUniqueId()));
        try {
            while(set.next()) {
                Map<Integer, String> map = (Map<Integer, String>) Serializer.deserializeBukkitObject(set.getBytes(1));
                for(int slot : map.keySet()) {
                    Storage str = (Storage) Serializer.deserializeBukkitObject(set.getBytes(2 + slot));
                    str.getInventory();
                }
            }
            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db.close();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
