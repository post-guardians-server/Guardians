package me.rukon0621.guardians.dropItem;

import me.rukon0621.guardians.data.ItemGrade;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.data.StoneData;
import me.rukon0621.guardians.helper.Couple;
import me.rukon0621.guardians.helper.Rand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("adamantData")
public class DropAdamantData implements ConfigurationSerializable {

    private final Map<Stat, Couple<Double, Double>> stats;
    private final ItemGrade grade;

    public DropAdamantData(Map<Stat, Couple<Double, Double>> data, ItemGrade grade) {
        this.stats = data;
        this.grade = grade;
    }

    public StoneData generateStoneData() {
        Stat stat = Rand.getRandomCollectionElement(stats.keySet());
        return new StoneData(grade, stat, Rand.randDouble(stats.get(stat).getFirst(), stats.get(stat).getSecond()));
    }

    public static DropAdamantData deserialize(Map<String, Object> data) {
        Map<Stat, Couple<Double, Double>> stats = new HashMap<>();
        for(String stat : data.keySet()) {
            if(stat.equals("grade")) continue;
            try {
                String[] v = ((String) data.get(stat)).split(", ");
                stats.put(Stat.valueOf(stat.toUpperCase()), new Couple<>(Double.parseDouble(v[0].trim()), Double.parseDouble(v[1].trim())));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning(stat + " - 이 이름의 스텟 정보는 확인되지 않습니다.");
            }
        }
        return new DropAdamantData(stats, ItemGrade.valueOf(((String) data.get("grade")).toUpperCase()));
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        for(Stat stat : stats.keySet()) {
            data.put(stat.toString(), String.format("%.2f, %.2f", stats.get(stat).getFirst(), stats.get(stat).getSecond()));
        }
        data.put("grade", grade.toString());
        return data;
    }
}
