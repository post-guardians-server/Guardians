package me.rukon0621.guardians.skillsystem.skilltree.windows;

import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.Couple;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.skillsystem.skilltree.SkillTreeManager;
import me.rukon0621.guardians.skillsystem.skilltree.elements.SkillLine;
import me.rukon0621.guardians.skillsystem.skilltree.elements.SkillTree;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class SkillWindowData {
    private static final SkillTreeManager manager = main.getPlugin().getSkillTreeManager();
    private final List<SkillTree> skills = new ArrayList<>();
    private final List<SkillLine> lines = new ArrayList<>();
    private final Couple<Integer, Integer> defaultLocation;
    private int x;
    private int y;
    private final int minusRange;

    public SkillWindowData(Configure config, int treeIndex) {
        minusRange = config.getConfig().getInt("minusRange", 0);
        String s = config.getConfig().getString("defaultLocation", "0,0");
        int num1 = Integer.parseInt(s.split(",")[0].trim()) + minusRange;
        int num2 = Integer.parseInt(s.split(",")[1].trim());
        defaultLocation = new Couple<>(num1, num2);
        ConfigurationSection sc = config.getConfig().getConfigurationSection("skillData");
        if(sc==null) return;
        for(String key : sc.getKeys(false)) {
            ConfigurationSection section = sc.getConfigurationSection(key);
            String[] location = section.getString("location", "0,0").split(",");
            int x = Integer.parseInt(location[0]), y = Integer.parseInt(location[1]);
            SkillTree tree = new SkillTree(x, y, key, treeIndex, section);
            skills.add(tree);
            manager.addSkill(tree);
            this.x = Math.max(x, this.x);
            this.y = Math.max(y, this.y);
        }
        for (SkillTree tree : skills) {
            if (!tree.isRoot()) continue;
            tree.x += minusRange;
            tree.generateChild(this);
        }
    }

    public int getMinusRange() {
        return minusRange;
    }

    public Couple<Integer, Integer> getDefaultLocation() {
        return defaultLocation;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public List<SkillTree> getSkills() {
        return skills;
    }

    public List<SkillLine> getLines() {
        return lines;
    }
}
