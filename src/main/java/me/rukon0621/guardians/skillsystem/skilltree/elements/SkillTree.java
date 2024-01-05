package me.rukon0621.guardians.skillsystem.skilltree.elements;

import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.skillsystem.skilltree.SkillTreeManager;
import me.rukon0621.guardians.skillsystem.skilltree.windows.SkillWindowData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTree extends SkillElement{
    private static final SkillTreeManager manager = main.getPlugin().getSkillTreeManager();
    private final String name;
    private final int cmd;
    private final List<String> requiredSkills;

    private final boolean root;
    private final List<String> child = new ArrayList<>();
    private final List<String> bannedSkills;
    private final List<String> lines;
    private final List<String> childNode;
    private final boolean useOrCondition;
    private boolean childSet = false;
    private final List<String> lores;
    private final int point;
    private final int level;
    private final int treeIndex;
    private final Map<Stat, Double> statMap = new HashMap<>();

    public SkillTree(int x, int y, String name, int treeIndex, ConfigurationSection section) {
        super(x, y);
        this.name = name;
        this.treeIndex = treeIndex;
        this.cmd = section.getInt("customModelData", 0);
        this.level = section.getInt("requiredLevel", 1);
        this.point = section.getInt("requiredPoint", 1);
        this.useOrCondition = section.getBoolean("useOrCondition", false);
        this.requiredSkills = section.getStringList("requiredSkills");
        this.bannedSkills = section.getStringList("bannedSkills");
        this.lores = section.getStringList("lore");
        this.root = section.getBoolean("root", false);
        this.lines = section.getStringList("lines");
        this.childNode = section.getStringList("childNode");

        ConfigurationSection stats = section.getConfigurationSection("stats");
        if(stats != null) {
            for(String key : stats.getKeys(false)) {
                try {
                    Stat stat = Stat.valueOf(key);
                    double value = stats.getDouble(key);
                    statMap.put(stat, stat.isUsingPercentage() ? value / 100 : value);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning(name + " - 해당 스킬 트리를 로드하지 못했습니다.");
                    manager.getErrors().add(name);
                    StringBuilder sb = new StringBuilder("알 수 없는 스텟 입니다. 사용 가능한 상수 값: ");
                    for(Stat s : Stat.values()) {
                        sb.append(s.toString()).append(", ");
                    }
                    Bukkit.getLogger().severe(sb.toString());
                }
            }
        }

        if(hasStat()) {
            for(Stat stat : statMap.keySet()) {
                String lore;
                if(stat.isUsingPercentage()) {
                    lore = String.format("&7- %s %.2f%%", stat.getKorName(), statMap.get(stat) * 100D);
                }
                else lore = String.format("&7- %s %.2f", stat.getKorName(), statMap.get(stat));

                if(stat.equals(Stat.STUN_DUR)) lore += "초";
                lores.add(lore);
            }
        }

    }

    public void generateChild(SkillWindowData window) {
        for(String s : lines) {
            String[] data = s.split(",");
            int cmd;
            try {
                cmd = Integer.parseInt(data[0]);
            } catch (NumberFormatException e) {
                cmd = switch (data[0]) {
                    case "─" -> 2;
                    case "┐" -> 3;
                    case "┌" -> 4;
                    case "┘" -> 5;
                    case "└" -> 6;
                    case "┬" -> 7;
                    case "┴" -> 8;
                    case "├" -> 9;
                    case "┤" -> 10;
                    case "/" -> 11;
                    case "\\" -> 12;
                    case "+", "┼" -> 13;
                    default -> 1;
                };
            }
            int x = Integer.parseInt(data[1]) + this.x;
            int y = Integer.parseInt(data[2]) + this.y;
            window.setX(Math.max(window.getX(), x));
            window.setY(Math.max(window.getY(), y));
            window.getLines().add(new SkillLine(x,y,cmd));
        }
        for(String s : childNode) {
            if(s.equals(name)) {
                Bukkit.getLogger().severe(name + " - ChildNode에 자신을 참조할 수 없습니다.");
                continue;
            }
            SkillTree childTree = manager.getSkillTree(s);
            if(childTree.childSet) continue;
            childTree.childSet = true;
            childTree.x += x;
            childTree.y += y;
            childTree.generateChild(window);
        }
    }

    public boolean hasStat() {
        return !statMap.isEmpty();
    }

    public void applyBaseStat(Player player) {
        for(Stat stat : statMap.keySet()) {
            stat.setBase(player, stat.getBase(player) + statMap.get(stat));
        }
    }

    public boolean isUsingOrCondition() {
        return useOrCondition;
    }

    public List<String> getBannedSkills() {
        return bannedSkills;
    }

    public String getName() {
        return name;
    }

    public List<String> getChild() {
        return child;
    }

    public int getCmd() {
        return cmd;
    }

    public List<String> getLines() {
        return lines;
    }

    public List<String> getChildNode() {
        return childNode;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public List<String> getLores() {
        return lores;
    }

    public int getPoint() {
        return point;
    }

    public int getLevel() {
        return level;
    }

    public int getTreeIndex() {
        return treeIndex;
    }

    public boolean isRoot() {
        return root;
    }
}
