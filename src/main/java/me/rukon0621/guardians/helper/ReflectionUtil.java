package me.rukon0621.guardians.helper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil {
    public static void getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }
    }

    @Nullable
    public static Field getField(String name, Class<?> type) {
        List<Field> fields = new ArrayList<>();
        getAllFields(fields, type);
        for(Field field : fields) {
            if(field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }
}
