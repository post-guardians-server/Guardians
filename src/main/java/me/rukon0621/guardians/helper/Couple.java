package me.rukon0621.guardians.helper;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Couple <F, S> implements ConfigurationSerializable {

    private F first;
    private S second;

    public Couple(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public Couple(Map<String, Object> map) {
        this.first = (F) map.get("f");
        this.second = (S) map.get("s");
    }

    public F getFirst() {
        return first;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("f", first);
        map.put("s", second);
        return map;
    }
}
