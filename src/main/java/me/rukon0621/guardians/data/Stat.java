package me.rukon0621.guardians.data;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public enum Stat {


    /*
    모든 확률은 100을 기준으로
    모든 배율은 1을 기준으로
     */
    HEALTH("health", "추가 체력", 0.11,
            false, "#f9aa5d",true, false, -1D),
    HEALTH_PER("healthPer", "추가 체력", 0.015,
            false, "#f9aa5d",false, true, -1D),
    ATTACK_DAMAGE("attackDamage", "공격력", 0.11,
            false, "#f09999",true, false, -1D),
    ATTACK_DAMAGE_PER("attackDamagePer", "공격력", 0.015,
            false, "#f09999",false, true, -1D),
    ARMOR("armor", "방어력", 0.11,
            false, "#88CAAB", true, false, -1D),
    ARMOR_PER("armorPer", "방어력", 0.015,
            false, "#88CAAB", false, true, -1D),
    ABSOLUTE_ARMOR("absoluteArmor", "절대 방어율", 0.015,
            false, "#4809b5", true, true, -1D),
    PLAYER_ARMOR("playerArmor", "플레이어 방어력", 0,
            false, "#8accad", false, true, -1D),
    CRT_CHANCE("criticalChance", "치명타 확률", 0.025,
            true, "#fff866", true, true, -1D),
    CRT_CHANCE_PER("criticalChancePer", "치명타 확률", 0.025,
            true, "#fff866", false, true, -1D),
    CRT_DAMAGE("criticalDamage", "치명타 피해량", 0.025,
            true, "#ff3333", true, true, -1D),
    CRT_DAMAGE_PER("criticalDamagePer", "치명타 피해량", 0.025,
            true, "#ff3333", false, true, -1D),
    MOVE_SPEED("movementSpeed", "이동속도", 0.07,
            false, "#d3f28d", true, false, -1D),
    MOVE_SPEED_PER("movementSpeedPer", "이동속도", 0.07,
            false, "#d3f28d", false, true, -1D),
    REGEN("regen", "재생력", 0.07,
            false, "#ffc7ec", true, false, -1D),
    REGEN_PER("regenPer", "재생력", 0.015,
            false, "#ffc7ec", false, true, -1D),
    EVADE("evade", "회피율", 0.027,
            true, "#93FF33", true, true, 0.25D),
    EVADE_POWER("evadePower", "회피력", 0.027,
            true, "#93FF33", false, false, -1D),
    LUCK("luckLevel", "행운력", 0.035,
            false, "#61fd6d", true, false, -1D),
    LUCK_PER("luckLevelPer", "행운력", 0.035,
            false, "#61fd6d", false, true, -1D),
    IGNORE_ARMOR("armorIgnore", "방어 관통률", 0.027,
            true, "#b12dd0", true, true, 0.5D),
    IGNORE_ARMOR_POWER("armorIgnorePower", "방어 관통력", 0.027,
            true, "#b12dd0", false, false, -1D),

    COOL_DEC("coolDecrease", "쿨타임 감소율", 0.027,
            false, "#00ffd5", true, true, 0.25D),
    COOL_DEC_POWER("coolDecreasePower", "쿨타임 감소력", 0.027,
            false, "#00ffd5", false, false, -1D),
    STUN_DUR("stunDur", "둔화 시간", 0.027,
            false, "#6857b5", true, false, -1D),
    STUN_CHANCE("stunPer", "둔화 확률", 0.027,
            false, "#a08feb", true, true, 1D),
    KB_RESISTANCE("kbResistance", "넉백 저항력", 0.027,
            false, "#a08feb", true, true, 1D),
    ;


    public static final double MAX_PLAYER_ARMOR = 90; //플방 상수 - 최대 플방
    public static final double PLAYER_ARMOR_CONST = 340; //플방 상수 - 최대 방어력
    private final static Map<String, Stat> korKeyMap = new HashMap<>();
    private final static Map<String, Stat> codeKeyMap = new HashMap<>();

    /**
     *
     * @param player player
     * @param armorIgnore 0~1 사이의 double
     * @return 0~1 사이의 값 도출 0.2면 20%의 플레이어 방어력을 의미
     */
    public static double getPlayerArmor(Player player, double dam, double armorIgnore) {
        double am = Stat.ARMOR.getTotal(player) * (1 - armorIgnore);
        double per = (dam / (5 * am)) * 0.7;
        return Math.min(per, 1);
    }


    public static void resetKeyMap() {
        korKeyMap.clear();
        codeKeyMap.clear();
        for(Stat stat : Stat.values()) {
            if(!korKeyMap.containsKey(stat.korName)) korKeyMap.put(stat.korName, stat);
            codeKeyMap.put(stat.codeName, stat);
        }
    }

    public static Stat getStatByKorName(String koreanKeyName) {
        return korKeyMap.get(koreanKeyName);
    }
    public static Stat getStatByCodeName(String codeKeyName) {
        return codeKeyMap.get(codeKeyName);
    }

    private final String codeName;
    private final String korName;
    private final boolean showStat;
    private final boolean usePercentage;
    private final String statColor;
    private final double maxValue;

    //레벨 당 증가하는 스텟 비율
    private final double levelScale;

    //장신구 일때만 스텟 비율이 증가하는 가
    private final boolean scaleOnlyAccessory;

    public static double getTotalPower(Player player) {
        double num = Stat.ATTACK_DAMAGE.getTotal(player);
        num *= 1 + (Stat.CRT_CHANCE.getTotal(player) * Stat.CRT_DAMAGE.getTotal(player));
        num *= (1 + Stat.IGNORE_ARMOR.getTotal(player));
        num += (Stat.ARMOR.getTotal(player) * (1 + Stat.EVADE.getTotal(player)));
        num += (Stat.HEALTH.getTotal(player) / 2.7) * (1 + Stat.REGEN.getTotal(player) / (Stat.HEALTH.getTotal(player) / 2));
        return num;
    }

    Stat(String codeName, String korName, double levelScale, boolean scaleOnlyAccessory, String statColor, boolean showStat, boolean usePercentage, double maxValue) {
        this.codeName = codeName;
        this.korName = korName;
        this.levelScale = levelScale;
        this.scaleOnlyAccessory = scaleOnlyAccessory;
        this.showStat = showStat;
        this.statColor = statColor;
        this.usePercentage = usePercentage;
        this.maxValue = maxValue;
    }

    public double getLevelScale() {
        return levelScale;
    }

    public boolean isScaledOnlyAccessory() {
        return scaleOnlyAccessory;
    }

    /**
     * @param player player
     * @return 해당 스텟의 장비 수치를 반환
     */
    public double get(Player player) {
        return ((Number) new PlayerData(player).getData().getOrDefault(codeName,0)).doubleValue();
    }

    /**
     * @param player player
     * @return 해당 스텟의 플레이어 고유 수치를 반환
     */
    public double getBase(Player player) {
        return ((Number) new PlayerData(player).getData().getOrDefault("base" + codeName,0)).doubleValue();
    }

    /**
     * @param player player
     * @return 해당 스텟의 (스킬 등으로 추가된) 임시 추가 수치 수치를 반환
     */
    public double getAdd(Player player) {
        return ((Number) new PlayerData(player).getData().getOrDefault("add" + codeName,0)).doubleValue();
    }
    /**
     * @param player player
     * @return 해당 스텟의 버프 아이템으로 인한 추가 수치 수치를 반환
     */
    public double getBuff(Player player) {
        return ((Number) new PlayerData(player).getData().getOrDefault("buff" + codeName,0)).doubleValue();
    }
    public double getCollection(Player player) {
        return ((Number) new PlayerData(player).getData().getOrDefault("colt" + codeName,0)).doubleValue();
    }
    public double getEnvironment(Player player) {
        return ((Number) new PlayerData(player).getData().getOrDefault("env" + codeName,0)).doubleValue();
    }
    public void setEnvironment(Player player, double value) {
        new PlayerData(player).getData().put("env" + codeName, value);
    }

    //장비의 값
    public void set(Player player, double value) {
        new PlayerData(player).getData().put(codeName, value);
    }

    //내재 수치 (플레이어 몸 자체의 스텟 값)
    public void setBase(Player player, double value) {
        new PlayerData(player).getData().put("base" + codeName, value);
    }

    //추가 스텟 (스킬 등으로 얻은 임시 버프)
    public void setAdd(Player player, double value) {
        new PlayerData(player).getData().put("add" + codeName, value);
    }

    //버프 스텟
    public void setBuff(Player player, double value) {
        new PlayerData(player).getData().put("buff" + codeName, value);
    }
    //버프 스텟
    public void setCollection(Player player, double value) {
        new PlayerData(player).getData().put("colt" + codeName, value);
    }

    public double getTotal(Player player) {
        double value = get(player) + getBase(player);
        try {
            Stat stat = Stat.valueOf(this + "_PER");
            value *= (1 + stat.getTotal(player));
            value *= (1 + stat.getEnvironment(player));
        } catch (IllegalArgumentException ignored) {
        }
        if(!statColor.endsWith("PER")) value += getEnvironment(player);
        value += getBuff(player) + getAdd(player) + getCollection(player);
        if(maxValue == -1) return value;
        return Math.min(value, maxValue);
    }

    public double getMaxValue() {
        return maxValue;
    }

    public String getStatColor() {
        return statColor;
    }

    public String getCodeName() {
        return codeName;
    }

    public boolean isUsingPercentage() {
        return usePercentage;
    }

    public boolean isShowStat() {
        return showStat;
    }

    public String getKorName() {
        return korName;
    }
}
