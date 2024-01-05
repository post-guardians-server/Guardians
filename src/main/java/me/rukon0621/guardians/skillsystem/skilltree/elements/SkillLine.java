package me.rukon0621.guardians.skillsystem.skilltree.elements;

import me.rukon0621.guardians.helper.ItemClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SkillLine extends SkillElement{
    private final int lineType;

    public SkillLine(int x, int y, int line) {
        super(x, y);
        lineType = line;
    }

    public ItemStack getIcon() {
        ItemClass item = new ItemClass(new ItemStack(Material.STRING), "&7");
        item.setCustomModelData(lineType);
        return item.getItem();
    }
}
