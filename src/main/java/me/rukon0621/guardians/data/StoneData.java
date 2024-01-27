package me.rukon0621.guardians.data;

import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.ItemSaver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StoneData {
    public static final String stoneColor = "#ffffbb";

    private final ItemGrade grade;
    private final Stat stat;
    private final double value;

    public static long getGrantPrice(int level, ItemGrade grade) {
        long price;
        switch (grade) {
            case UNCOMMON -> price = 5000;
            case UNIQUE -> price = 15000;
            case EPIC -> price = 30000;
            case LEGEND -> price = 120000;
            case ANCIENT -> price = 300000;
            default -> price = 1000;
        }
        return (long) (price * (level / 10D));
    }

    public StoneData(ItemGrade grade, Stat stat, double value) {
        this.grade = grade;
        this.stat = stat;
        this.value = value;
    }

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

    public ItemClass getStoneItem() {
        ItemData item = new ItemData(Objects.requireNonNull(ItemSaver.getItem("아다만트석 " + grade.getStr())));
        item.setLevel(0);
        item.setGrade(grade);
        item.addStoneData(this);
        return item.getItem();
    }

}
