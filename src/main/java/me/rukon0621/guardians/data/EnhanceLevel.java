package me.rukon0621.guardians.data;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public enum EnhanceLevel {

    ZERO(0, 0, 0,
            0, null, "#777777"),
    ONE(0.1, 95, 0,
            1, null, "#777777"),
    TWO(0.2, 90, 0,
            2, null, "#888888"),
    THREE(0.3, 85, 0,
            3, null, "#888888"),
    FOUR(0.3, 80, 0,
            5, null, "#888888"),
    FIVE(0.7, 75, 0,
            10, "아다만트 부스러기:1", "#88aa88"),
    SIX(0.4, 50, 0,
            10, null, "#88aa88"),
    SEVEN(0.4, 50, 0,
            0, null, "#88aa88"),
    EIGHT(0.5, 45, 0,
            0, null, "#99cc99"),
    NINE(0.5, 40, 0,
            0, null, "#99cc99"),
    TEN(1, 40, 0,
            0, null, "#8888aa"),
    ELEVEN(0.6, 40, 1.8,
            0, null, "#8888aa"),
    TWELVE(0.7, 40, 3,
            0, null, "#8888aa"),
    THIRTEEN(0.7, 40, 4.2,
            0, null, "#9999cc"),
    FOURTEEN(0.8, 40, 6,
            0, null, "#9999cc"),
    FIFTEEN(1, 37, 9.5,
            0, null, "#aa8888"),
    SIXTEEN(1.2, 35, 13,
            0, null, "#aa8888"),
    SEVENTEEN(1.3, 35, 16.3,
            0, null, "#aa8888"),
    EIGHTEEN(1.5, 20, 48.5,
            0, null, "#cc9999"),
    NINETEEN(2, 10, 49,
            0, null, "#cc9999"),
    TWENTY(3, 5, 49.5,
            0, null, "#ffaa33"),
    ;

    private static final List<Double> stackedMultiply = new ArrayList<>();

    public static void initialize() {
        for(EnhanceLevel level : EnhanceLevel.values()) {
            if(level.equals(ZERO)) stackedMultiply.add(ZERO.multiply);
            else stackedMultiply.add(stackedMultiply.get(level.ordinal() - 1) + level.multiply);
            System.out.println(level.getLevel() + " : " + level.getMoney(0));
        }
    }

    public static EnhanceLevel getEnhanceLevel(int level) {
        return EnhanceLevel.values()[level];
    }

    private final double multiply;
    private final double chance;
    private final double downChance;
    private final int requiredStone;
    private final String extraItem;
    private final String colorKey;

    EnhanceLevel(double multiply, double chance, double downChance, int requiredStone, String extraItem, String colorKey) {
        this.multiply = multiply;
        this.chance = chance;
        this.downChance = downChance;
        this.colorKey = colorKey;
        this.requiredStone = requiredStone;
        this.extraItem = extraItem;
    }

    @Nullable
    public EnhanceLevel upgrade() {
        int nextIndex = ordinal() + 1;
        if(nextIndex == values().length) return null;
        return values()[nextIndex];
    }
    @Nullable
    public boolean isMaxLevel() {
        return ordinal() + 1 == values().length;
    }

    public int getRequiredStone() {
        return requiredStone;
    }

    public String getExtraItem() {
        return extraItem;
    }

    public double getChance() {
        return chance;
    }

    public double getFailDownChance() {
        return ((100 - getChance()) / 2);
    }

    public double getBigFailChance() {
        return downChance;
    }

    //15x^{2.5}+100=y
    public long getMoney(int reqLevel) {
        //return (long) (15*Math.pow(ordinal(), 2.5) + 100);
        return (long) (reqLevel*0.9*Math.pow(ordinal(), 1.5) + 100);
    }

    public long getLevel() {
        return ordinal();
    }

    public double getMultiply() {
        return stackedMultiply.get(ordinal());
    }

    public String getColorKey() {
        return colorKey;
    }
}
