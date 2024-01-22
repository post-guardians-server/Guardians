package me.rukon0621.guardians.dialogquest;

import lombok.extern.java.Log;
import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.InvClass;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Rand;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.pay.PaymentData;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static me.rukon0621.guardians.main.pfix;

public class QuestInProgress implements ConfigurationSerializable {
    private HashMap<String, Integer> mobData;
    private Set<String> completedCustomObject;
    private String name;

    public QuestInProgress(String name) {
        this.name = name;
        mobData = new HashMap<>();
        completedCustomObject = new HashSet<>();
    }

    private void reloadQuest() {
        Quest quest = DialogQuestManager.getQuestData().get(name);
        if(quest.getSort()!=1) return;
        for(String name : quest.getMobData().keySet()) {
            if(!mobData.containsKey(name)) mobData.put(name, 0);
        }
    }

    public ItemStack getIcon() {
        reloadQuest();
        Quest quest = DialogQuestManager.getQuestData().get(name);
        ItemClass it = new ItemClass(new ItemStack(quest.getIconItem()));
        if(quest.isRepeatable()) it.setName(name + " &7[ 반복 ]");
        else it.setName(name);
        for(String s : quest.getLores()) {
            it.addLore(s);
        }
        if(quest.getSort()==1) {
            it.addLore(" ");
            for(String name : quest.getMobData().keySet()) {
                if(mobData.get(name) >= quest.getMobData().get(name)) {
                    it.addLore("&f"+name+String.format(" &7( &a%d &7/ &2%d &7)", mobData.get(name), quest.getMobData().get(name)));
                }
                else {
                    it.addLore("&f"+name+String.format(" &7( &e%d &7/ &e%d &7)", mobData.get(name), quest.getMobData().get(name)));
                }
            }
        }
        else if (quest.getSort()==2) {
            it.addLore(" ");
            it.addLore("#8fce00\uE011\uE200\uE200가져와야할 아이템을 확인하려면 우클릭하십시오.");
        }
        else if (quest.getSort()==4) {
            it.addLore(" ");
            for(String object : quest.getCustomObjects()) {
                if(completedCustomObject.contains(object)) it.addLore("&a\uE012\uE00C"+object);
                else it.addLore("&c\uE013\uE200"+object);
            }
        }
        if(!(quest.getCompleteNpc().equals("즉시 완료")||quest.getCompleteNpc().equals("즉시완료"))) {
            if(quest.getCompleteNpc().equalsIgnoreCase("click")) {
                it.addLore(" ");
                it.addLore("#f8d97d\uE011\uE200\uE200&e좌클릭#f8d97d하여 퀘스트를 완료하십시오.");
            }
            else {
                it.addLore(" ");
                it.addLore("#f8d97d\uE011\uE200\uE200퀘스트를 완료하려면 &f"+quest.getCompleteNpc()+"#f8d97d에게 찾아가주세요.");
            }

        }
        if(quest.isNavigatable()) {
            it.addLore(" ");
            it.addLore("#cc77cc\uE011\uE200\uE200퀘스트의 위치를 추적하려면 #c3fef3쉬프트+좌클릭#cc77cc을 하십시오.");
        }
        if(quest.canGiveUp()) {
            it.addLore(" ");
            it.addLore("&c\uE014\uE200\uE200퀘스트를 포기하려면 &4쉬프트+우클릭&c을 눌러주세요.");
        }
        return it.getItem();
    }

    public boolean completeQuest(Player player, Entity entity) {
        return completeQuest(player, entity, null, false);
    }
    public boolean completeQuest(Player player, @Nullable Entity entity, @Nullable Inventory inv, boolean force) {
        Quest quest = DialogQuestManager.getQuestData().get(name);
        ArrayList<ItemStack> items = new ArrayList<>();
        reloadQuest();
        ArrayList<ItemStack> beRemoved = new ArrayList<>();
        if(entity!=null) {
            if(!quest.getCompleteNpc().equals(Msg.recolor(entity.getName()))) return false;
        }


        PlayerData pdc = new PlayerData(player);
        //조건 검사
        if(!force) {
            //몹 처치
            if(quest.getSort()==1) {
                for(String mobName : quest.getMobData().keySet()) {
                    if(mobData.get(mobName)<quest.getMobData().get(mobName)) return false;
                }
            }
            //아이템 가져오기
            else if (quest.getSort()==2) {
                if(inv==null) return false;
                //현재 플레이어가 넣은 아이템을 리스트화
                for(int i : DialogQuestManager.completingSlots) {
                    if(inv.getItem(i)==null) continue;
                    items.add(inv.getItem(i));
                }
                if(items.size()==0) return false;
                if(quest.getConditions().contains("onlyname")||quest.getConditions().contains("useitemdata")) {
                /*
                item : 현재 반복중인 퀘스트 완료에 필요한 아이템
                it : 현재 퀘스트를 완료를 위해 넣어진 아이템 (looping)
                items: 현재 퀘스트 완료를 위해 넣어진 아이템 목록 (9칸)
                find: 아이템을 찾았는가
                 */
                    int questDataIndex = 0;
                    for(ItemStack item : quest.getConvertedItems()) {
                        String targetType = null;

                        if(!item.hasItemMeta()) continue;
                        if(item.getItemMeta().getDisplayName().endsWith("타입의 아이템")) {
                            targetType = Msg.uncolor(item.getItemMeta().getDisplayName()).replaceAll("타입의 아이템", "").trim();
                        }
                        int amount = 0;
                        boolean find = false;
                        ItemData questData = quest.getConvertedItemData().get(questDataIndex);
                        questDataIndex++;

                        for(ItemStack it : items) {
                            if(it==null) continue;

                            if(targetType==null&&!it.getItemMeta().getDisplayName().equals(questData.getName())) continue;

                            //Check ItemData
                            ItemData itemData = new ItemData(it);

                            //타겟 타입 확인
                            if(targetType!=null&&!TypeData.getType(itemData.getType()).isMaterialOf(targetType)) continue;

                            //아이템 레벨 계산
                            if(itemData.getLevel()<questData.getLevel()) continue;

                            //아이템 부가 속성 확인
                            boolean passCond = true;
                            for(String attr : questData.getAttrs()) {
                                if(itemData.getAttrLevel(attr) < questData.getAttrLevel(attr)) {
                                    passCond = false;
                                    break;
                                }
                            }

                            //아이템 수 계산
                            if(passCond) {
                                if(amount<item.getAmount()) {
                                    ItemStack remove = new ItemStack(it);
                                    while(remove.getAmount()>0) {
                                        if(amount>=item.getAmount()) break;
                                        remove.setAmount(remove.getAmount()-1);
                                        amount++;
                                    }
                                    beRemoved.add(remove);
                                }

                                if(amount>=item.getAmount()) {
                                    find = true;
                                    break;
                                }
                            }
                        }
                        if(!find) return false;
                    }
                }
                else {
                    for(ItemStack item : quest.getConvertedItems()) {
                        if(!InvClass.hasItem(player, item, items)) return false;
                    }
                }
            }
            //커스텀 목표
            else if (quest.getSort()==4) {
                for(String object : quest.getCustomObjects()) {
                    if(!completedCustomObject.contains(object)) return false;
                }
            }

            //피로도 조건 검사
            if(quest.isRepeatable()) {
                if(pdc.getFatigue() >= pdc.getMaxFatigue()) {
                    Msg.warn(player, "피로도가 가득 차 퀘스트를 완료할 수 없습니다.");
                    return false;
                }
            }
        }


        ArrayList<QuestInProgress> quests = DialogQuestManager.getQuestsInProgress(player);
        for(QuestInProgress qu : quests) {
            if(qu.getName().equals(this.name)) {
                quests.remove(qu);
                break;
            }
        }
        DialogQuestManager.setQuestInProgress(player, quests);

        DialogQuestManager.getCompletedQuests(player).add(name);
        DialogQuestManager.getQuestLog(player).add(0, name);


        //퀘스트 타이머
        if(quest.isRepeatable()) {
            Map<String, Long> cooltime = DialogQuestManager.getQuestCooltime(player);
            PaymentData pay = new PaymentData(player);
            if(pay.getRemainOfBertBlessing() > 0)  cooltime.put(name, new Date().getTime() + quest.getRepeatTimer()*800);
            else cooltime.put(name, System.currentTimeMillis() + quest.getRepeatTimer()*1000);
            DialogQuestManager.setQuestCooltime(player, cooltime);
        }

        //아이템 회수
        if(!quest.isItemSaved()) {
            if(quest.getSort()==2) {
                if(quest.getConditions().contains("onlyname")||quest.getConditions().contains("useitemdata")) {
                    for(ItemStack item : items) {
                        for(ItemStack removing : beRemoved) {
                            if(removing.getItemMeta().equals(item.getItemMeta())) {
                                item.setAmount(removing.getAmount());
                            }
                        }
                    }
                }
                else {
                    for(ItemStack item : quest.getItems()) {
                        InvClass.removeItem(player, item, items);
                    }
                }
            }
        }

        //퀘스트 추적 취소
        Quest navigated = DialogQuestManager.getNavigatingQuest(player);
        if(navigated!=null&&navigated.getName().equals(name)) {
            DialogQuestManager.setNavigatingQuest(player, null);
        }

        if(quest.getCompletingCustomObject()!=null) {
            DialogQuestManager.completeCustomObject(player, quest.getCompletingCustomObject());
        }

        //연계 퀘스트가 없으면 보상 띄우기 또는 보상이 비어 있음
        if(quest.getChainQuest().equals("null")) {
            DialogQuestManager.openQuestDataList(player, name, true, true);
            Msg.send(player, "&a축하합니다! 퀘스트를 클리어하셨습니다!", pfix);
            player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, (float) Rand.randDouble(1.2, 1.5));
        }
        //연계 퀘스트
        else {
            DialogQuestManager.getQuestData().get(quest.getChainQuest()).startQuest(player, true);
        }
        if(!quest.getEndMessage().equals("null")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Msg.send(player, " ");
                    Msg.send(player, quest.getEndMessage(), "&7[ &c! &7] &f");
                    Msg.send(player, " ");
                    Msg.sendTitle(player, "\uE00C", quest.getEndMessage(), 60, 20, 40);
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0.75f);
                    player.playSound(player, Sound.ENTITY_ENDER_DRAGON_HURT, 1, 0.5f);
                }
            }.runTaskLater(main.getPlugin(), quest.getEndEventDelay());
        }
        if(quest.getEndStory() != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    StoryManager.readStoryInstantly(player, quest.getEndStory());
                }
            }.runTaskLater(main.getPlugin(), quest.getEndEventDelay());
        }
        else if(!quest.getEndStoryList().isEmpty()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    StoryManager.readStoryInstantly(player, quest.getEndStoryList());
                }
            }.runTaskLater(main.getPlugin(), quest.getEndEventDelay());
        }

        //추가 이벤트
        if(quest.isRepeatable()) {
            if(!StoryManager.isRead(player,"반복 퀘스트")) {
                StoryManager.readStory(player, "반복 퀘스트");
            }
            pdc.setFatigue(pdc.getFatigue() + 1);
        }

        //Logging
        String uncoloredName = Msg.uncolor(Msg.color(quest.getName()));
        LogManager.log(player, "questClear", uncoloredName);
        //LogManager.stat(player, "questClear", uncoloredName, 1);
        return true;
    }

    public HashMap<String, Integer> getMobData() {
        return mobData;
    }

    public void setMobData(HashMap<String, Integer> data) {
        mobData = data;
    }

    public void killMob(Player player, String mobName) {
        if(mobData.containsKey(mobName)) {
            mobData.put(mobName, mobData.get(mobName)+1);
        }
        else {
            mobData.put(mobName, 1);
        }

        //즉시 완료 퀘스트인 경우 퀘스트 즉시 완료 가능성 확인
        Quest quest = DialogQuestManager.getQuestData().get(name);
        if(quest.getCompleteNpc().equals("즉시 완료")||quest.getCompleteNpc().equals("즉시완료")) {
            completeQuest(player, null);
        }
    }

    public Set<String> getCompletedCustomObject() {
        return completedCustomObject;
    }
    public void setCompletedCustomObject(Set<String> completedCustomObject) {
        this.completedCustomObject = completedCustomObject;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("completedCustomObject", completedCustomObject);
        data.put("mobData", mobData);
        return data;
    }

    public static QuestInProgress deserialize(Map<String, Object> data) {
        QuestInProgress qip = new QuestInProgress((String) data.get("name"));
        qip.setCompletedCustomObject((Set<String>) data.get("completedCustomObject"));
        qip.setMobData((HashMap<String, Integer>) data.get("mobData"));
        return qip;
    }
}
