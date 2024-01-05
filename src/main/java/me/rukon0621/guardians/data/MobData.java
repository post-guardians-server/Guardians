package me.rukon0621.guardians.data;

import me.rukon0621.guardians.helper.Couple;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.listeners.DamagingListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

import static me.rukon0621.guardians.main.levelSplit;
import static me.rukon0621.guardians.main.typeSplit;

public class MobData {
    private static final Map<UUID, Map<UUID, Double>> contributionMap = new HashMap<>();
    private final Entity entity;
    private final Map<UUID, Double> data;
    public MobData(Entity entity) {
        if(!contributionMap.containsKey(entity.getUniqueId())) contributionMap.put(entity.getUniqueId(), new HashMap<>());
        data = contributionMap.get(entity.getUniqueId());
        this.entity = entity;
    }

    /**
     * @param player player
     * @return 해당 플레이어가 이 몹에게 준 데미지를 반환
     */
    public double getContribution(Player player) {
        return data.getOrDefault(player.getUniqueId(), 0.0);
    }

    /**
     * @return 총 기여도를 반환 (이 몹이 받은 총 데미지를 반환)
     */
    public double getTotalContribution() {
        double d = 0;
        for(double d2 : data.values()) {
            d += d2;
        }
        return d;
    }

    /**
     * 해당 플레이어의 기여도를 추가 (플레이어가 몹에게 amount 만큼의 데미지를 박음)
     * @param player player
     * @param amount 데미지
     */
    public void addContribution(Player player, double amount) {
        data.put(player.getUniqueId(), getContribution(player) + amount);
    }

    public Set<UUID> getAttackers() {
        return data.keySet();
    }

    /**
     * @param player player
     * @return 해당 플레이어의 기여도 백분율 (최대 100%)
     */
    public double getContributionProportion(Player player) {
        return getContribution(player) / getTotalContribution() * 100;
    }

    public void optimize() {
        if(entity.isDead()) {
            clearMobData(entity);
            return;
        }

        Iterator<UUID> itr = getAttackers().iterator();
        while(itr.hasNext()) {
            UUID uuid = itr.next();
            if (DamagingListener.getRemainCombatTime(uuid) != -1) continue;
            itr.remove();
        }

        if(data.isEmpty()) clearMobData(entity);
    }

    public static void clearMobData(Entity entity) {
        contributionMap.remove(entity.getUniqueId());
    }

    /**
     *
     * @param entity 엔티티
     * @return 순수이름(색x, 레벨x), 레벨을 반환 , 일반적인 가디언즈 몹(레벨이 있는 몹)이 아니라면 레벨을 -1로 반환
     */
    public static Couple<String, Integer> getMobData(Entity entity) {
        return getMobData(entity.getName());
    }

    /**
     *
     * @param entityName 엔티티의 이름
     * @return 순수이름(색x, 레벨x), 레벨을 반환 , 일반적인 가디언즈 몹(레벨이 있는 몹)이 아니라면 레벨을 -1로 반환
     */
    public static Couple<String, Integer> getMobData(String entityName) {
        try {
            entityName = Msg.uncolor(entityName);
            if(!entityName.contains(levelSplit)) return new Couple<>(entityName, -1);
            String[] data = entityName.split(levelSplit);
            entityName = data[0].trim();
            int level;
            try {
                level = Integer.parseInt(data[1]);
            } catch (NumberFormatException e) {
                level = 1;
            }

            //속성이 이름에 끼어있으면 해당 문자열은 제거
            if(entityName.contains(typeSplit)) {
                entityName = entityName.split(typeSplit)[1].trim();
            }
            return new Couple<>(entityName, level);
        } catch (Exception e) {
            return new Couple<>(null, 0);
        }
    }

}
