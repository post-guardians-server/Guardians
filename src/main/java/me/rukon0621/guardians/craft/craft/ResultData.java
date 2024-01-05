package me.rukon0621.guardians.craft.craft;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.helper.ItemClass;

enum CraftResult {
    SUCCESS, //제작 성공
    NO_PROCESS_TIME, //가공 횟수가 부족
    ONLY_ONE_PROCESS, //기본 가공 (다듬기 등)은 딱 1회만 가능
    NO_SKILL, //스킬 트리 부족
    NOT_FILLED, //아이템 채워지지 않음
    NO_COST //비용 부족
}

public class ResultData {
    private final CraftResult craftResult;
    private final ItemData resultItemData;
    private final ItemClass resultItem;
    private final long craftingTime;
    private final long cost;

    public ResultData(CraftResult result) {
        craftResult = result;
        resultItem = null;
        craftingTime = 0;
        resultItemData = null;
        cost = 0;
    }
    public ResultData(CraftResult result, long cost) {
        craftResult = result;
        resultItem = null;
        craftingTime = 0;
        resultItemData = null;
        this.cost = cost;
    }

    public ResultData(ItemData itemData, CraftResult result, long craftingTime, long cost) {
        craftResult = result;
        resultItem = itemData.getItem();
        resultItemData = itemData;
        this.craftingTime = craftingTime;
        this.cost = cost;
    }

    public long getCost() {
        return cost;
    }

    public ItemClass getResultItem() {
        return resultItem;
    }

    public ItemData getResultItemData() {
        return resultItemData;
    }

    public CraftResult getCraftResult() {
        return craftResult;
    }

    public long getCraftingTime() {
        return craftingTime;
    }
}
