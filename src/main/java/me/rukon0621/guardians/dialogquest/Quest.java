package me.rukon0621.guardians.dialogquest;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import me.rukon0621.pay.PaymentData;
import me.rukon0621.pay.RukonPayment;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static me.rukon0621.guardians.data.LevelData.EXP_BOOK_TYPE_NAME;
import static me.rukon0621.guardians.main.pfix;

public class Quest {
    private static final main plugin = main.getPlugin();
    private final String name;
    private final int sort;
    private final String completeNpc;
    private final String completingCustomObject;
    private final ArrayList<String> conditions;
    private final ArrayList<ItemStack> rewards;
    private final ArrayList<ItemStack> convertedRewards;
    private final ArrayList<ItemStack> items; //세이버 객체가 보존됨
    private final ArrayList<ItemStack> convertedItems; //세이버 객체 실체화됨
    private final ArrayList<ItemData> convertedItemData; //세이버 객체 데이터화
    private final HashMap<String, Integer> mobData;
    private final List<String> logLores;
    private final ArrayList<String> customObjects;
    private final ArrayList<String> lores;
    private final boolean isRepeatable;
    private final long repeatTimer;
    private final boolean canGiveUp;
    private final boolean saveItem;
    private final ItemStack iconItem;
    private final String endMessage;
    private String endStory;
    private List<String> endStoryList;
    private final String chainQuest;
    private final boolean navigatable;
    private final boolean isGuardianQuest;
    private final Location navigatingTarget;
    private final double leastContribution;
    private final int endEventDelay;

    public Quest(String name, Configure config) {
        this.name = name;
        this.rewards = (ArrayList<ItemStack>) config.getConfig().getList(name+".rewards", new ArrayList<>());
        int season = RukonPayment.inst().getPassManager().getSeason();
        ArrayList<ItemStack> convRewards = new ArrayList<>();
        for(ItemStack item : rewards) {
            ItemData itemData = new ItemData(item.clone());
            try {
                if(TypeData.getType(itemData.getType()).isMaterialOf("소모품")) {
                    itemData.setSeason(season);
                    item = itemData.getItemStack();
                }
            } catch (NullPointerException ignored) {

            }
            if(itemData.getType().equals("세이버")) {
                convRewards.add(itemData.convertSaver().getItemStack());
            }
            else if(itemData.getType().equals(EXP_BOOK_TYPE_NAME)) {
                convRewards.add(LevelData.getExpBook(LevelData.getExpOfBook(itemData.getName())));
            }
            else {
                convRewards.add(item);
            }
        }
        this.convertedRewards = convRewards;
        this.completeNpc = config.getConfig().getString(name+".completeNpc", "CompleteNpc NOT SET");
        this.sort = config.getConfig().getInt(name+".sort");
        ArrayList<String> cond = (ArrayList<String>) config.getConfig().getList(name+".conditions", new ArrayList<>());
        for(int i = 0; i < cond.size(); i++) {
            cond.set(i, cond.get(i).toLowerCase());
        }
        this.conditions = cond;
        this.iconItem = (ItemStack) config.getConfig().get(name+".icon", new ItemStack(Material.BOOK));
        this.lores = (ArrayList<String>) config.getConfig().getList(name+ ".lore" , new ArrayList<>());


        this.endEventDelay = config.getConfig().getInt(name+".endEventDelay", 60);
        try {
            this.endStoryList = config.getConfig().getStringList(name+".endStory");
            this.endStory = null;
        } catch (Exception e) {
            this.endStoryList = new ArrayList<>();
            this.endStory = config.getConfig().getString(name+".endStory", null);
        }

        this.isGuardianQuest = config.getConfig().getBoolean(name+".isGuardianQuest", false);
        this.isRepeatable = config.getConfig().getBoolean(name+".repeatable", false);
        this.repeatTimer = config.getConfig().getLong(name+".repeatTimer", 600);
        this.canGiveUp = config.getConfig().getBoolean(name+".canGiveUp", false);
        this.endMessage = config.getConfig().getString(name+".endMessage", "null");
        this.logLores = (List<String>) config.getConfig().getList(name+".logs", new ArrayList<>());
        this.saveItem = config.getConfig().getBoolean(name+".saveItem", false);
        this.chainQuest = config.getConfig().getString(name+".chainQuest", "null");
        this.completingCustomObject = config.getConfig().getString(name+".completingCustomObject", null);
        this.navigatable = config.getConfig().getBoolean(name+".navigatable", false);
        this.navigatingTarget = config.getConfig().getLocation(name + ".navigatingTarget", new Location(plugin.getServer().getWorld("world"), 0, 0,0));
        this.leastContribution = config.getConfig().getDouble(name+".leastContribution", 30);
        mobData = new HashMap<>();
        if(sort==1) {
            ArrayList<String> mobDataStrings = (ArrayList<String>) config.getConfig().getList(name+".details");
            customObjects = new ArrayList<>();
            items = new ArrayList<>();
            convertedItems = new ArrayList<>();
            convertedItemData = new ArrayList<>();
            for(String s : mobDataStrings) {
                if(!s.contains(",")) continue;
                String[] data = s.split(",");
                String mobName;
                int amount;
                try {
                    mobName = data[0].trim();
                    amount = Integer.parseInt(data[1].trim());
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    continue;
                }
                mobData.put(mobName, amount);
            }
        }
        else if (sort==2) {
            customObjects = new ArrayList<>();
            items = (ArrayList<ItemStack>) config.getConfig().getList(name+".details");
            ArrayList<ItemStack> converted = new ArrayList<>();
            ArrayList<ItemData> convertedData = new ArrayList<>();
            for(ItemStack item : items) {
                ItemData itemData = new ItemData(item);
                if(itemData.getType().equals("세이버")) {
                    ItemData conv = itemData.convertSaver();
                    converted.add(conv.getItemStack());
                    convertedData.add(conv);
                }
                else {
                    converted.add(item);
                    convertedData.add(itemData);
                }
            }
            convertedItems = converted;
            convertedItemData = convertedData;
        }
        else if (sort==4) {
            customObjects = (ArrayList<String>) config.getConfig().getList(name+".details");
            items = new ArrayList<>();
            convertedItems = new ArrayList<>();
            convertedItemData = new ArrayList<>();
        }
        else {
            customObjects = new ArrayList<>();
            items = new ArrayList<>();
            convertedItems = new ArrayList<>();
            convertedItemData = new ArrayList<>();
        }
    }

    public ItemStack getIcon(Player player) {
        ItemClass it = new ItemClass(new ItemStack(Material.ENCHANTED_BOOK));
        PaymentData pyd = new PaymentData(player);
        PlayerData pdc = new PlayerData(player);
        if(isRepeatable) it.setName(name + " &7[ 반복 ]");
        else it.setName(name);
        for(String lore : lores) {
            it.addLore(lore);
        }
        it.addLore(" ");
        if(isRepeatable) {
            if(pyd.getRemainOfBertBlessing() > 0) it.addLore("&b※퀘스트 쿨타임: " + DateUtil.formatDate((long) (repeatTimer*0.8)) + " &7(신의 가호 시간 감소 적용중)");
            else it.addLore("&e※퀘스트 쿨타임: " + DateUtil.formatDate(repeatTimer));
            it.addLore(" ");
            it.addLore("&e※클리어시 피로도 1 증가");
            it.addLore(String.format("&f현재 피로도: %d &7/ &f%d &c(매일 0으로 초기화됨)", pdc.getFatigue(), pdc.getMaxFatigue()));

        }
        if(completeNpc.equalsIgnoreCase("click")) {
            it.addLore(" ");
            it.addLore("&f※ 이 퀘스트는 &b퀘스트 창에서 클릭&f하여 완료해야합니다.");
        }

        it.addLore("&e※클릭하여 퀘스트를 수락합니다.");
        return it.getItem();
    }

    public String getName() {
        return name;
    }

    public int getSort() {
        return sort;
    }

    public double getLeastContribution() {
        return leastContribution;
    }

    public ItemStack getIconItem() {
        return iconItem;
    }

    public String getCompleteNpc() {
        return completeNpc;
    }

    public ArrayList<String> getConditions() {
        return conditions;
    }

    public ArrayList<ItemStack> getRewards() {
        return rewards;
    }
    public ArrayList<ItemStack> getConvertedRewards() {
        return convertedRewards;
    }

    public ArrayList<ItemStack> getItems() { return items;
    }
    public ArrayList<ItemStack> getConvertedItems() {
        return convertedItems;
    }

    public ArrayList<ItemData> getConvertedItemData() {
        return convertedItemData;
    }

    public HashMap<String, Integer> getMobData() {
        return mobData;
    }

    public ArrayList<String> getCustomObjects() {
        return customObjects;
    }

    public ArrayList<String> getLores() {
        return lores;
    }

    public boolean canGiveUp() {
        return canGiveUp;
    }

    public int getEndEventDelay() {
        return endEventDelay;
    }

    public boolean isRepeatable() {
        return isRepeatable;
    }

    public long getRepeatTimer() {
        return repeatTimer;
    }

    public String getEndMessage() {
        return endMessage;
    }

    public String getChainQuest() {
        if(chainQuest==null) return "null";
        return chainQuest;
    }

    public boolean isCanGiveUp() {
        return canGiveUp;
    }

    public boolean isSaveItem() {
        return saveItem;
    }

    public boolean isGuardianQuest() {
        return isGuardianQuest;
    }

    public boolean isNavigatable() {
        return navigatable;
    }

    public Location getNavigatingTarget() {
        return navigatingTarget;
    }

    @Nullable
    public String getCompletingCustomObject() {
        return completingCustomObject;
    }

    public boolean isItemSaved() {
        return saveItem;
    }

    public void startQuest(Player player) {
        startQuest(player, false);
    }
    public void startQuest(Player player, boolean chained) {
        ArrayList<QuestInProgress> quests = DialogQuestManager.getQuestsInProgress(player);
        boolean pass = true;
        for(QuestInProgress quest : quests) {
            if(quest.getName().equals(name)) {
                Msg.send(player, "&c해당 퀘스트는 이미 진행중인 퀘스트입니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1 ,1);
                return;
            }
        }

        if(isRepeatable) {
            PlayerData pdc = new PlayerData(player);
            if(pdc.getFatigue() == pdc.getMaxFatigue()) {
                Msg.warn(player, "피로도가 가득차 더이상 진행할 수 없습니다. 매일 자정에 피로도가 0이 됩니다.");
                return;
            }
        }

        if(DialogQuestManager.getQuestsInProgress(player).size()>=9) {
            Msg.warn(player, "더이상 퀘스트를 진행할 수 있습니다. 최대 9개의 퀘스트만 병행할 수 있습니다.");
            return;
        }

        if(DialogQuestManager.getCompletedQuests(player).contains(name)) {
            if(isRepeatable) {
                if(DialogQuestManager.getQuestCooltime(player).containsKey(name)) {
                    long time = DialogQuestManager.getQuestCooltime(player).get(name) - new Date().getTime();
                    if(time > 0) {
                        Msg.send(player, "&e이 퀘스트를 다시 받으려면 "+ DateUtil.formatDate(time/1000)+"를 기다려야합니다.", pfix);
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                        pass = false;
                    }
                }
            }
            else {
                if(isGuardianQuest) {
                    Msg.warn(player, "가디언 본부 의뢰는 하루에 1번씩 클리어할 수 있습니다.");
                    return;
                }
                Msg.send(player, "&c해당 퀘스트는 이미 클리어한 퀘스트입니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                pass = false;
            }
        }
        for(String cond : conditions) {
            if(cond.toLowerCase().startsWith("level")) {
                PlayerData pdc = new PlayerData(player);
                if(isRepeatable) {
                    pdc.setFatigue(pdc.getFatigue() + 1);
                }
                String s = cond.replaceAll("level", String.valueOf(pdc.getLevel()));
                if(!StringComparator.StringComparator(s)) {
                    Msg.send(player, "&c레벨 조건이 맞지 않아 퀘스트를 시작할 수 없습니다.", pfix);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                    pass= false;
                    break;
                }
            }
        }
        if(pass) {
            quests.add(new QuestInProgress(name));
            DialogQuestManager.setQuestInProgress(player, quests);
            if(chained) Msg.send(player, "&a퀘스트 목표를 완료하여 퀘스트가 갱신되었습니다! 퀘스트 목록을 확인해보세요!", String.format("&7[ &e%s &7] ", name));
            else Msg.send(player, "&a새로운 퀘스트를 시작했습니다!", pfix);
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1 ,(float) Rand.randDouble(0.8, 1.3));
        }
    }

    public void openQuestCompletingMenu(Player player) {
        InvClass inv = new InvClass(5, DialogQuestManager.questItemGuiName);
        int[] slots = new int[]{13,14,15,22,23,24,33,34,35};
        int index = 0;
        for(ItemStack item : convertedItems) {
            inv.setslot(slots[index], item);
            index++;
        }
        ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&e퀘스트 완료하기");
        it.setCustomModelData(7);
        it.addLore("&f인벤토리에서 아이템을 선택하여");
        it.addLore("&f퀘스트 완료에 사용할 아이템을 모두 넣어주세요.");
        inv.setslot(35, it.getItem());
        it = new ItemClass(new ItemStack(Material.SCUTE), "&c대화문 읽기");
        it.setCustomModelData(7);
        it.addLore("&f퀘스트 완료를 스킵하고 NPC와 대화합니다.");
        inv.setslot(17, it.getItem());

        player.openInventory(inv.getInv());
    }

    public String getEndStory() {
        return endStory;
    }

    public List<String> getEndStoryList() {
        return endStoryList;
    }

    public ItemStack getLogItem() {
        ItemClass item = new ItemClass(iconItem.clone());
        if(isRepeatable) item.setName(name + " &7[ 반복 ]");
        else item.setName(name);
        for(String lore : lores) {
            item.addLore(lore);
        }
        item.addLore(" ");
        item.addLore("&7『 &f퀘스트 정보 &7』");
        final boolean isInstanlyClearable = completeNpc.replaceAll(" ", "").equals("즉시완료") || completeNpc.replaceAll(" ", "").equalsIgnoreCase("click");
        switch (sort) {
            case 1 -> {
                if (isInstanlyClearable) item.addLore("&f아래 나오는 몬스터를 토벌했음.");
                item.addLore(completeNpc + "&f의 부탁으로 다음 몬스터들을 토벌했음.");
                for (String name : mobData.keySet()) {
                    item.addLore(" &7- " + name + " x" + mobData.get(name));
                }
            }
            case 2 -> {
                if (isInstanlyClearable) item.addLore("&f다음 아이템을 가져다줌.");
                else item.addLore(completeNpc + "&f에게 다음 아이템을 가져다줌.");
                for (ItemData itemData : convertedItemData) {
                    item.addLore(" &7- " + itemData.getName() + " &7x" + itemData.getAmount());
                }
            }
            case 3 -> {
                if (isInstanlyClearable) item.addLore("&f퀘스트를 완료함.");
                else item.addLore(completeNpc + "&f에게서 퀘스트를 완료함.");
            }
            case 4 -> {
                if (isInstanlyClearable) item.addLore("&f아래 나오는 목표를 완료했음.");
                else item.addLore(completeNpc + "&f에게서 다음과 같은 부탁을 받음");
                for (String obj : customObjects) {
                    item.addLore(" &7- " + obj);
                }
            }
        }
        if(!chainQuest.equals("null")) {
            item.addLore(" ");
            item.addLore("&f연계 퀘스트: " + chainQuest);
        }
        if(!logLores.isEmpty()) {
            item.addLore(" ");
            item.addLore("&7『 &f퀘스트 일지 &7』");
            for(String str : logLores) {
                item.addLore(str);
            }
        }
        return item.getItem();
    }
}
