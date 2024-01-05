package me.rukon0621.guardians.dropItem;

import me.rukon0621.boxes.RukonBoxes;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Rand;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.party.PartyManager;
import me.rukon0621.guardians.story.StoryManager;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SerializableAs("customDrop")
public class Drop implements ConfigurationSerializable {

    private static double dropBurning = 1.0;
    private static double dropAttrBurning = 1.0;

    private final String itemSaver; //아이템 세이버에서의 아이템 이름

    //레벨이 몹레벨에서 얼마만큼 떨어지는가
    //60레벨 몹을 잡았는데 minusRange가 3이면 57~60사이의 레벨로 정해짐
    private final int minMinusRange;
    private final int maxMinusRange;

    //해당 아이템이 몇개까지 나오는가 (확률 반복)
    private final int loopNumber;

    //아이템의 등장 확률
    private final double chance;
    private final int levelAbove;

    public static void setBurning(double multiply) {
        dropBurning = multiply;
    }
    public static void setAttrBurning(double multiply) {
        dropAttrBurning = multiply;
    }

    //추가적으로 등장할 수 있는 속성 정보
    private final ArrayList<DropAttribute> dropAttrs;

    public Drop(String itemSaver, int minMinusRange, int maxMinusRange, int loopNumber, ArrayList<DropAttribute> dropAttrs, double chance, int levelAbove) {
        this.itemSaver = itemSaver;
        this.minMinusRange = minMinusRange;
        this.maxMinusRange = maxMinusRange;
        this.loopNumber = loopNumber;
        this.dropAttrs = dropAttrs;
        this.chance = chance;
        this.levelAbove = levelAbove;
    }

    /**
     * 기여도에 따라서 아이템을 지급
     * 플레이어의 행운 수치 반영
     * 행운 속성 1당 해당 아이템이 뜰확률이 0.01배 증가
     * 플레이어가 오프라인이라면 메일로 전송
     * ex) 행운 속성 30인 아이템으로 30%로 드롭되는 뼈를 채집 -> 33%로 드롭
     * @param player player
     * @param level 해당 드롭이 몇 레벨인가
     * @param contribution 0~1사이의 실수, 드롭 확률 x Contribution(기여도) = 실제 드롭 확률
     */
    public void giveDropData(Player player,int level, double contribution) {
        ArrayList<ItemStack> items = makeDropList(player, level, contribution);
        if(!player.isOnline()) {
            MailBoxManager.sendAll(player, items);
            return;
        }
        for(ItemStack item : items) {
            player.getInventory().addItem(item);
        }
    }

    public ArrayList<ItemStack> makeDropList(Player player, int level, double contribution) {
        return makeDropList(player, level, contribution, false);
    }
    public ArrayList<ItemStack> makeDropList(Player player, int level, double contribution, boolean SHOW_MODE) {
        return makeDropList(player, level, contribution, SHOW_MODE, false);
    }

    /**
     * 파티원 1명당 기여도 버프 5%
     * @param player player
     * @param level 드롭의 레벨
     * @param contribution 기여도
     * @param SHOW_MODE 이 옵션이 활성화되면 100%의 기본 부가 속성만 표시 (추가적인 부가속성이 뜨지 않음)
     * @param ignorePartyAndLuck 이 옵션이 활성화되면 파티 보너스와 행운력 및 드롭 버닝을 계산하지 않음
     * @return 해당 드롭의 리스트를 생성 (아이템을 주지는 않고 줄 아이템 리스트만 선정)
     */
    public ArrayList<ItemStack> makeDropList(Player player, int level, double contribution, boolean SHOW_MODE, boolean ignorePartyAndLuck) {
        PlayerData pdc = new PlayerData(player);

        if(!ignorePartyAndLuck) {
            double partyBonus = PartyManager.getPartyBonus(player);
            if(partyBonus!=-1) {
                contribution *= 1 + partyBonus;
            }
        }

        ArrayList<ItemStack> items = new ArrayList<>();
        if(itemSaver.contains("경험치")||itemSaver.startsWith("돈")||itemSaver.startsWith("청사진")||itemSaver.startsWith("드롭")||itemSaver.startsWith("미박")) {
            if(itemSaver.startsWith("드롭")) {
                ArrayList<ItemStack> its = new ArrayList<>();
                for(int i = 0 ; i < loopNumber; i++) {
                    double ch = chance * contribution * (1 + Stat.LUCK.getTotal(player) / 130);
                    if(chance<=100&&!Rand.chanceOf(ch)) continue;
                    try {
                        String[] data = itemSaver.split(":");
                        double contributionMultiply;
                        if(data.length==3) contributionMultiply = Double.parseDouble(data[2]);
                        else contributionMultiply = 1;
                        its.addAll(DropManager.getDropList(player, data[1].trim(), level, contribution * contributionMultiply, SHOW_MODE));
                    } catch (Exception e) {
                        Msg.warn(player, "드롭 데이터 처리중 오류가 발생하였습니다. : " + itemSaver);
                        return new ArrayList<>();
                    }
                }
                return its;
            }
            ItemStack it;
            //강화석은 에리어에서 별도로 설정됨
            if(itemSaver.contains("강화석")) {
                it = ItemSaver.getItemParsed("강화석", AreaManger.getArea(pdc.getArea()).getEnhanceStoneLevel()).getItem();
            }
            else if(itemSaver.contains("경험치")) {
                try {
                    int exp = Integer.parseInt(itemSaver.split(":")[1].trim());
                    it = LevelData.getEquipmentExpBook(exp);
                } catch (Exception e) {
                    Msg.warn(player, "드롭 데이터 처리중 오류가 발생하였습니다. : " + itemSaver);
                    return new ArrayList<>();
                }
            }
            else if(itemSaver.startsWith("청사진")) {
                try {
                    it = main.getPlugin().getBluePrintManager().getBlueprintItem(itemSaver.split(":")[1].trim());
                } catch (Exception e) {
                    Msg.warn(player, "드롭 데이터 처리중 오류가 발생하였습니다. : " + itemSaver);
                    return new ArrayList<>();
                }
            }
            else if(itemSaver.startsWith("미박")) {
                try {
                    it = RukonBoxes.inst().getMysteryBoxManager().getBoxItem(itemSaver.split(":")[1].trim());
                } catch (Exception e) {
                    Msg.warn(player, "드롭 데이터 처리중 오류가 발생하였습니다. : " + itemSaver);
                    return new ArrayList<>();
                }
            }
            else {
                try {
                    it = LevelData.getDinarItem(Integer.parseInt(itemSaver.split(":")[1].trim()));
                } catch (Exception e) {
                    Msg.warn(player, "드롭 데이터 처리중 오류가 발생하였습니다. : " + itemSaver);
                    return new ArrayList<>();
                }
            }
            for(int i = 0 ; i < loopNumber; i++) {
                double ch;
                if(ignorePartyAndLuck) ch = chance * contribution;
                else ch = chance * contribution * (1 + Stat.LUCK.getTotal(player) / 100) * dropBurning;
                if(!Rand.chanceOf(ch)) continue;
                if(!SHOW_MODE && itemSaver.startsWith("청사진")) {
                    if(!StoryManager.getReadStory(player).contains("청사진 설명")) {
                        StoryManager.addStory(player, "청사진 설명");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                StoryManager.readStory(player,  "청사진 설명");
                            }
                        }.runTaskLater(main.getPlugin(), 40);
                    }
                }
                items.add(it);
            }
        }
        else {
            if(level<levelAbove) {
                return new ArrayList<>();
            }
            try {
                for(int i = 0 ; i < loopNumber; i++) {
                    double ch;
                    if(ignorePartyAndLuck) ch = chance * contribution;
                    else ch = chance * contribution * (1 + Stat.LUCK.getTotal(player) / 100) * dropBurning;

                    if(chance>100) ch = 100;
                    if(!Rand.chanceOf(ch)) continue;
                    ItemData idata = new ItemData(new ItemStack(ItemSaver.getItem(itemSaver).getItem()));

                    idata.setLevel(Math.max(1, level - Rand.randInt(minMinusRange, maxMinusRange)));
                    if(dropAttrs!=null) {
                        for(DropAttribute attr : dropAttrs) {
                            if(attr.getLevelLimit()>level) continue;

                            if(SHOW_MODE&&attr.getChance()<100) continue;

                            double ch2 = attr.getChance();
                            if(!ignorePartyAndLuck) ch2 *= dropAttrBurning;


                            if(ch2<=100) ch2 = ch2 * contribution * (1 + Stat.LUCK.getTotal(player)/100);
                            if(!Rand.chanceOf(ch2)) continue;
                            idata.addAttr(attr.getAttributeName(), attr.getAttrLevel());
                        }
                    }
                    items.add(idata.getItemStack());
                }
            } catch (Exception e) {
                Msg.warn(player, "드롭 데이터 처리중 오류가 발생하였습니다. : " + itemSaver);
                return new ArrayList<>();
            }
        }
        return items;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("itemSaver", itemSaver);
        if(minMinusRange!=5) data.put("minusRange", minMinusRange);
        if(minMinusRange!=maxMinusRange) data.put("maxMinusRange", maxMinusRange);
        if(loopNumber>0) data.put("loopNumber", loopNumber);
        if(chance!=100.0) data.put("chance", chance);
        if(levelAbove!=0) data.put("levelAbove", levelAbove);
        if(dropAttrs!=null&&dropAttrs.size()>0) data.put("dropAttrs", dropAttrs);
        return data;
    }

    public static Drop deserialize(Map<String, Object> data) {
        int minusRange = (int) data.getOrDefault("minusRange", 5);
        int maxMinusRange = (int) data.getOrDefault("maxMinusRange", minusRange);
        int loopNumber = (int) data.getOrDefault("loopNumber", 1);
        Number chance = (Number) data.getOrDefault("chance", 100.0);
        int levelAbove = (int) data.getOrDefault("levelAbove", 0);
        return new Drop((String) data.get("itemSaver"), minusRange, maxMinusRange, loopNumber, (ArrayList<DropAttribute>) data.getOrDefault("dropAttrs", new ArrayList<>()), chance.doubleValue(), levelAbove);
    }

    public String getItemSaver() {
        return itemSaver;
    }

    public ArrayList<DropAttribute> getDropAttrs() {
        return dropAttrs;
    }
}
