package me.rukon0621.guardians.dialogquest;

import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.story.StoryManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Dialog  {
    private final String name;
    private final List<String> dialogs;
    private final List<String> dialogCond;
    private final List<String> questCond;
    private final List<String> storyCond;
    private final List<String> specialCond;
    private final int priority;

    /*
    levelAbove:20 -> 20레벨 부터 ㄱㄴ
    levelBelow:20 -> 20레벨까지 ㄱㄴ
    hasCustomObject:<목표 이름> -> 해당 목표를 가지고 있는가
    notHaveCustomObject:<목표 이름> -> 해당 목표를 가지고 있지 않는가
     */

    public Dialog(String name, Configure config) {
        this.name = name;
        this.dialogs = (List<String>) config.getConfig().getList(name+".dialogs");
        this.dialogCond = (List<String>) config.getConfig().getList(name+".dialogConditions", new ArrayList<>());
        this.questCond = (List<String>) config.getConfig().getList(name+".questConditions", new ArrayList<>());
        this.storyCond = (List<String>) config.getConfig().getList(name+".storyConditions", new ArrayList<>());
        this.specialCond = (List<String>) config.getConfig().getList(name+".specialConditions", new ArrayList<>());
        this.priority = config.getConfig().getInt(name+".priority", 10);
    }

    public Dialog(List<String> list) {
        name = null;
        dialogs = list;
        dialogCond = new ArrayList<>();
        questCond = new ArrayList<>();
        storyCond = new ArrayList<>();
        specialCond = new ArrayList<>();
        priority = 10;
    }

    /**
     * 해당 플레이어가 대화문 조건에 맞춰서 이 대화문을 읽을 수 있는지 반환
     *
     * @param player player
     * @return 해당 플레이어가 대화문 조건에 맞춰서 이 대화문을 읽을 수 있는지 반환
     */
    public boolean isReadable(Player player) {
        try {
            Set<String> dialogs = DialogQuestManager.getReadDialogs(player);
            for (String name : dialogCond) {
                if (name.toLowerCase().startsWith("not!")) {
                    if (dialogs.contains(name.replaceAll("not!", ""))) return false;
                } else {
                    if (!dialogs.contains(name)) return false;
                }
            }
            Set<String> quests = DialogQuestManager.getCompletedQuests(player);
            for (String name : questCond) {
                if (name.toLowerCase().startsWith("not!")) {
                    if (quests.contains(name.replaceAll("not!", ""))) return false;
                } else {
                    if (!quests.contains(name)) return false;
                }
            }
            Set<String> stories = StoryManager.getReadStory(player);
            for (String name : storyCond) {
                if (name.toLowerCase().startsWith("not!")) {
                    if (stories.contains(name.replaceAll("not!", ""))) return false;
                } else {
                    if (!stories.contains(name)) return false;
                }
            }
            if (!specialCond.isEmpty()) {
                PlayerData pdc = new PlayerData(player);
                for (String name : specialCond) {
                    String cond = name.toLowerCase();
                    if (cond.startsWith("levelabove")) {
                        int level = Integer.parseInt(name.split(":")[1]);
                        if (pdc.getLevel() < level) {
                            return false;
                        }
                    } else if (cond.startsWith("levelbelow")) {
                        int level = Integer.parseInt(name.split(":")[1]);
                        if (pdc.getLevel() > level) {
                            return false;
                        }
                    } else if (cond.startsWith("hascustomobject")) {
                        String objectName = name.split(":")[1].trim();
                        if (!DialogQuestManager.hasCustomObject(player, objectName)) return false;
                    }else if (cond.startsWith("nothavecustomobject")) {
                        String objectName = name.split(":")[1].trim();
                        if (DialogQuestManager.hasCustomObject(player, objectName)) return false;
                    } else if (cond.startsWith("hasquest:")) {
                        String qName = name.split(":")[1].trim();
                        boolean pass = false;
                        for (QuestInProgress qip : DialogQuestManager.getQuestsInProgress(player)) {
                            if (!qip.getName().equals(qName)) continue;
                            pass = true;
                            break;
                        }
                        if (!pass) return false;
                    } else if (cond.startsWith("nothasquest:")) {
                        String qName = name.split(":")[1].trim();
                        for (QuestInProgress qip : DialogQuestManager.getQuestsInProgress(player)) {
                            if (qip.getName().equals(qName)) return false;
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Msg.send(player, "&c대화문 조건식에서 오류가 발견되었습니다. &4DialogName : " + name);
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public String getPage(int page) {
        return dialogs.get(page);
    }

    public int getPageSize() {
        return dialogs.size();
    }

    public int getPriority(){
        return priority;
    }
}
