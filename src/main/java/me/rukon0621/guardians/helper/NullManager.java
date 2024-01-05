package me.rukon0621.guardians.helper;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class NullManager {

    @NotNull
    public static Object defaultNull(@Nullable Object ob, @NotNull Object def) {
        if(ob==null) {
            System.out.println("NULL MANAGER NULL");
            return def;
        }
        return ob;
    }

}
