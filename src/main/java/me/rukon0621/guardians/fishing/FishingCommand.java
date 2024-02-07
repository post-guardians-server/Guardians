package me.rukon0621.guardians.fishing;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.ItemGrade;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FishingCommand implements CommandExecutor {

    private final FishingManager manager = main.getPlugin().getFishingManager();

    public FishingCommand() {
        main.getPlugin().getCommand("reloadFishing").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        long price = 0;
        Map<ItemGrade, Integer> map = new HashMap<>();

        if(args.length > 0) {
            if(commandSender instanceof Player player) {
                for(int i = 0; i < Integer.parseInt(args[0]); i++) {
                    ItemData itemData = manager.getResult(player, new ItemData(player.getInventory().getItemInMainHand()));
                    price += manager.getFishPrice(itemData);
                    map.put(itemData.getGrade(), map.getOrDefault(itemData.getGrade(), 0) + 1);
                }

                for(ItemGrade grade : ItemGrade.values()) {
                    if(!map.containsKey(grade)) continue;
                    Msg.send(player, grade.getStr() + " - " + map.get(grade));
                }

                Msg.send(player, "totalPrice - " + price);

            }
            return true;
        }

        main.getPlugin().getFishingManager().reloadFishingData();
        return true;
    }
}
