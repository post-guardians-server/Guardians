package me.rukon0621.guardians.dialogquest;

import me.rukon0621.gui.buttons.Icon;
import me.rukon0621.gui.windows.Window;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class QuestLogWindow extends Window {
    public QuestLogWindow(Player player) {
        super(player, "&f\uF000", 6);
        map.clear();
        int slot = 0;
        Iterator<String> itr = DialogQuestManager.getQuestLog(player).iterator();
        while(itr.hasNext()) {

            String s = itr.next();

            if(slot>=54) {
                itr.remove();
                continue;
            }
            Quest quest = DialogQuestManager.getQuestData().get(s);
            if(quest==null) {
                itr.remove();
                continue;
            }
            map.put(slot, new Icon() {
                @Override
                public ItemStack getIcon() {
                    return quest.getLogItem();
                }
            });
            slot++;
        }
        reloadGUI();
        open();
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) {
            DialogQuestManager.openQuestList(player);
        }
    }
}
