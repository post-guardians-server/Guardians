package me.rukon0621.guardians.craft.craft;

import me.rukon0621.guardians.helper.DateUtil;
import me.rukon0621.guardians.helper.ItemClass;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WaitingItem implements ConfigurationSerializable {
    private final ItemStack result;
    private Date endTime;

    public WaitingItem(ItemStack result, Date endTime) {
        this.result = result;
        this.endTime = endTime;
    }

    /**
     * 대기 아이템을 만듬
     * @param result 결과물 아이템 CLASS
     * @param second 소모시간 (초)
     */
    public WaitingItem(ItemClass result, long second) {
        this.result = result.getItem();
        this.endTime = new Date(System.currentTimeMillis()+(second * 1000));
    }

    //제작 결과물 반환
    public ItemStack getResult() {
        return result;
    }

    public ItemStack getIcon() {
        ItemClass it = new ItemClass(result.clone());
        long remainTime = getRemainTime();
        if(remainTime<=0) {
            it.addLore(" ");
            it.addLore("&f\uE010\uE00C\uE00C&a제작이 완료되었습니다. 클릭하여 가져가세요.");
        }
        else {
            it.addLore(" ");
            it.addLore("&6\uE004\uE00C\uE00C남은 시간: " + DateUtil.formatDate(remainTime/1000));
            it.addLore("&c\uE007+\uE006\uE00C\uE00C쉬프트 우클릭으로 제작을 취소할 수 있습니다.");
            it.addLore("&4단, 제작 취소시 재료는 돌려받을 수 없습니다.");
            it.addLore(" ");
            it.addLore("&e\uE011\uE00C\uE00C쉬프트 좌클릭으로 루나르를 사용하여 제작을 즉시 완료할 수 있습니다.");
            it.addLore("&b\uE017\uE00C\uE00C비용: " + getInstantFinishPrice() + "루나르");
        }
        return it.getItem();
    }

    /**
     * @return millisecond of remainTime
     */
    public long getRemainTime() {
        return endTime.getTime() - System.currentTimeMillis();
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("result", result);
        data.put("endTime", endTime);
        return data;
    }

    public static WaitingItem deserialize(Map<String, Object> data) {
        return new WaitingItem((ItemStack) data.get("result"), (Date) data.get("endTime"));
    }

    /**
     * @return 1시간에 80루나르, 1분당 1.3루나르
     */
    public int getInstantFinishPrice() {
        return (int) Math.max((((double) getRemainTime() / 60000L) * 1.3d), 2);
    }

}
