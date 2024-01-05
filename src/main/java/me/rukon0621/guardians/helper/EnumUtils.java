package me.rukon0621.guardians.helper;

import java.util.ArrayList;
import java.util.List;

public class EnumUtils {

    public static <x> List<String> getEnumStringList(Class<x> e) {
        List<String> list = new ArrayList<>();
        if(!e.isEnum()) {
            throw new ClassCastException(e + " : 이 클래스는 ENUM 클래스가 아닙니다.");
        }
        for(x en : e.getEnumConstants()) {
            list.add(en.toString());
        }
        return list;
    }

}
