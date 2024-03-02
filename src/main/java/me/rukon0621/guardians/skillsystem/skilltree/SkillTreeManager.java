package me.rukon0621.guardians.skillsystem.skilltree;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.FileUtil;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.skillsystem.skilltree.elements.SkillTree;
import me.rukon0621.guardians.skillsystem.skilltree.windows.PreSkillWindow;
import me.rukon0621.guardians.skillsystem.skilltree.windows.SkillWindowData;
import me.rukon0621.guardians.story.StoryManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SkillTreeManager {

    private final Map<String, SkillTree> dataMap = new HashMap<>();
    private final List<SkillWindowData> skillData = new ArrayList<>();
    private final Set<String> basicSkillNames = new HashSet<>();
    private final Set<String> errors = new HashSet<>();

    public SkillTreeManager() {
        basicSkillNames.add("검격 [ LV 1 ]");
        basicSkillNames.add("전격 [ LV 1 ]");
        basicSkillNames.add("회격 [ LV 1 ]");
        basicSkillNames.add("난사 [ LV 1 ]");
        basicSkillNames.add("정조준 [ LV 1 ]");
        basicSkillNames.add("캐논 [ LV 1 ]");
    }

    public String unicodeOfTree(int treeIndex) {
        if(treeIndex==6) return "\uF007";
        else if(treeIndex==7) return "\uF008";
        else if(treeIndex<3) return "\uF004";
        else return "\uF006";
    }

    public void addSkill(SkillTree tree) {
        dataMap.put(tree.getName(), tree);
    }

    public void reload() {
        reload(null);
    }

    public void reload(@Nullable Player player) {
        dataMap.clear();
        skillData.clear();
        for(int i = 0; i < 8; i++) {
            skillData.add(new SkillWindowData(new Configure(main.getPlugin(), FileUtil.getOuterPluginFolder() + "/skilltree/skilldata" + i + ".yml"), i));
        }
        for(SkillTree skillTree : dataMap.values()) {
            for(String pr : skillTree.getRequiredSkills()) {
                dataMap.get(pr).getChild().add(skillTree.getName());
            }
        }
        if(player != null) {
            for(String error : getErrors()) {
                Msg.send(player, "&c" + error + " - 이 스킬트리를 제대로 불러오지 못했습니다.");
            }
        }
        errors.clear();
    }

    public Set<String> getErrors() {
        return errors;
    }

    public void openSkillTree(Player player) {

        if(!(StoryManager.isRead(player, "스킬 설명") || DialogQuestManager.getCompletedQuests(player).contains("&c더 넓은 세계를 위하여"))) {
            Msg.warn(player, "아직은 이 기능을 사용할 수 없습니다.");
            return;
        }

        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerData pdc = new PlayerData(player);
                int skillPoint = (int) ((pdc.getLevel() - 1) * 3 + Math.floor(pdc.getSpiritOfHero() / 5));
                int craftSkillPoint = (pdc.getLevel() - 1);

                Iterator<String> itr = pdc.getSkillData().iterator();
                while(itr.hasNext()) {
                    String s = itr.next();
                    SkillTree tree = dataMap.getOrDefault(s, null);
                    if(tree==null) {
                        itr.remove();
                        continue;
                    }
                    if(tree.getTreeIndex() == 7) craftSkillPoint -= tree.getPoint();
                    else skillPoint -= tree.getPoint();
                }
                pdc.setSkillPoint(skillPoint);
                pdc.setCraftSkillPoint(craftSkillPoint);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new PreSkillWindow(player, "\uF002");
                    }
                }.runTask(main.getPlugin());
            }
        }.runTaskAsynchronously(main.getPlugin());
    }

    public Set<String> getSkillNames() {
        return dataMap.keySet();
    }

    public SkillTree getSkillTree(String name) {
        return dataMap.get(name);
    }

    public SkillWindowData getSkillData(int treeIndex) {
        return skillData.get(treeIndex);
    }

    public boolean isBasicSkill(String skillName) {
        return basicSkillNames.contains(skillName);
    }

    public Set<String> getBasicSkillNames() {
        return basicSkillNames;
    }
}
