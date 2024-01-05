package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.main;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Base64;

public class Serializer {
    //객체 직렬화
    public static byte[] serialize(Object obj) {
        try {
            if(obj==null) {
                main.getPlugin().getLogger().severe("SERIALIZE EXCEPTION!");
            }

            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            oo.close();
            return bo.toByteArray();
        } catch (IOException e) {
            return new byte[]{};
        }
    }

    @Nullable
    public static Object deserialize(byte[] bytes) {
        if(bytes==null) return null;
        try {
            ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return oi.readObject();
        } catch (Exception e) {
            main.getPlugin().getLogger().severe("DESERIALIZE EXCEPTION!");
            e.printStackTrace();
            return null;
        }
    }
    public static String seriallizeToString(Object obj) {
        byte[] bytes = serialize(obj);
        return Base64.getEncoder().encodeToString(bytes);
    }
    public static Object deseriallizeFromString(String encodedString) {
        byte[] bytes = Base64.getDecoder().decode(encodedString);
        return deserialize(bytes);
    }
    public static byte[] serializeBukkitObject(Object obj) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream oo = new BukkitObjectOutputStream(bo);
            oo.writeObject(obj);
            return bo.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static Object deserializeBukkitObject(byte[] bytes) {
        if(bytes==null) return null;
        try {
            BukkitObjectInputStream bo = new BukkitObjectInputStream(new ByteArrayInputStream(bytes));
            return bo.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String serializeBukkitObjectToString(Object obj) {
        byte[] bytes = serializeBukkitObject(obj);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Nullable
    public static Object deserializeBukkitObjectFromString(String encodedString) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encodedString);
            return deserializeBukkitObject(bytes);
        } catch (NullPointerException e) {
            return null;
        }
    }
}
