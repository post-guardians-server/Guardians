package me.rukon0621.guardians.data;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StoneData {
    private static final String stoneColor = "#ffffbb";

    private final ItemGrade grade;
    private final Stat stat;
    private final double value;

    public StoneData(String lore) {

        String[] data = lore.split("\uE00C");
        String[] data2 = data[1].split(": ");
        switch (data[0].trim()) {
            case "\uE01A" -> grade = ItemGrade.NORMAL;
            case "\uE01B" -> grade = ItemGrade.UNCOMMON;
            case "\uE01C" -> grade = ItemGrade.UNIQUE;
            case "\uE01D" -> grade = ItemGrade.EPIC;
            case "\uE01E" -> grade = ItemGrade.LEGEND;
            case "\uE01F" -> grade = ItemGrade.ANCIENT;
            default -> grade = ItemGrade.UNKNOWN;
        }
        value = Double.parseDouble(data2[1].trim());
        stat = Stat.getStatByKorName(data2[0].trim());
    }

    public double getValue() {
        return value;
    }

    public ItemGrade getGrade() {
        return grade;
    }

    public Stat getStat() {
        return stat;
    }

    public String toLore() {
        return stoneColor + grade.getStoneUnicode() +
                "\uE00C" + stat.getKorName() +
                ": " +
                String.format("%.2f", value);
    }

    public void applyToPlayer(Player player) {
        stat.addBase(player, value);
    }

    public static List<StoneData> getData(List<String> list) {
        return new ArrayList<>();
    }
    public static List<String> dataToString(List<StoneData> list) {
        return new ArrayList<>();
    }

}
