package me.rukon0621.guardians.helper;

import java.util.*;

public class TabCompleteUtils {
    public static List<String> searchAtList(ArrayList<String> list, String startWith) {
        ArrayList<String> result = new ArrayList<>();
        for(String s : list) {
            if (!s.contains(startWith)) continue;
            result.add(s);
        }
        return result;
    }
    public static List<String> searchAtList(List<String> list, String startWith) {
        ArrayList<String> result = new ArrayList<>();
        for(String s : list) {
            if (!s.contains(startWith)) continue;
            result.add(s);
        }
        return result;
    }
    public static List<String> searchAtSet(Set<String> list, String startWith) {
        ArrayList<String> result = new ArrayList<>();
        for(String s : list) {
            if (!s.contains(startWith)) continue;
            result.add(s);
        }
        return result;
    }
    public static List<String> searchAtSet(HashSet<String> list, String startWith) {
        ArrayList<String> result = new ArrayList<>();
        for(String s : list) {
            if (!s.contains(startWith)) continue;
            result.add(s);
        }
        return result;
    }

    public static List<String> search(Collection<String> list, String contain) {
        List<String> result = new ArrayList<>();
        for(String s : list) {
            if (!s.contains(contain)) continue;
            result.add(s);
        }
        return result;
    }
}
