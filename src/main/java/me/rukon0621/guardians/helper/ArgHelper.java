package me.rukon0621.guardians.helper;

import java.util.ArrayList;

public class ArgHelper {
    public static String sumArg(String[] args) {
        return sumArg(args, 0);
    }
    public static String sumArg(String[] args, int startindex) {
        ArrayList<String> list = new ArrayList<>();
        for(String s : args) list.add(s);
        for(int i = 0;i<startindex;i++) {
            if(list.size()==0) return "";
            list.remove(0);
        }
        return String.join(" ", list);
    }
}
