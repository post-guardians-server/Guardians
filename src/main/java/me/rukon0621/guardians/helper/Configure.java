package me.rukon0621.guardians.helper;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Base64;

public class Configure {
    private File file;
    private FileConfiguration config;

    //파일 확장명 넣어야함
    public Configure(String filename, String path) {
        this.file = new File(path, filename);
        this.config = YamlConfiguration.loadConfiguration(file);
        generateParentDir(file);

        if(!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Configure(String path) {
        String[] pathData = path.split("/");
        String filename = pathData[pathData.length-1];
        path = "";
        for(int i = 0;i< pathData.length-1;i++) {
            if(i==0) path += pathData[i];
            else path += "/" + pathData[i];
        }
        this.file = new File(path, filename);
        this.config = YamlConfiguration.loadConfiguration(file);
        generateParentDir(file);

        if(!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Configure(File file) {
        this.config = YamlConfiguration.loadConfiguration(file);
        this.file = file;
        generateParentDir(file);
        if(!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Deprecated
    public Configure(JavaPlugin plugin, String filename, String path) {
        this.file = new File(path, filename);
        this.config = YamlConfiguration.loadConfiguration(file);
        generateParentDir(file);

        if(!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Deprecated
    public Configure(JavaPlugin plugin, String path) {
        String[] pathData = path.split("/");
        String filename = pathData[pathData.length-1];
        path = "";
        for(int i = 0;i< pathData.length-1;i++) {
            if(i==0) path += pathData[i];
            else path += "/" + pathData[i];
        }
        this.file = new File(path, filename);
        this.config = YamlConfiguration.loadConfiguration(file);
        generateParentDir(file);

        if(!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Deprecated
    public Configure(JavaPlugin plugin, File file) {
        this.config = YamlConfiguration.loadConfiguration(file);
        this.file = file;
        generateParentDir(file);
        if(!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //부모 디렉토리 생성
    public void generateParentDir(File file) {
        initPermission(file);
        if(!(file.getParentFile().exists())) {
            generateParentDir(file.getParentFile());
            file.getParentFile().mkdir();
            initPermission(file.getParentFile());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void loadConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }

    //파일 삭제
    //부모 파일이 비어있으면 연쇄적으로 삭제
    //부모 파일의 이름이 limitDeletedString과 동일하면 연쇄 삭제 중단
    public void delete() {
        delete("");
    }

    public void delete(String limitDeleteString) {
        file.delete();
        while(file.getParentFile().length()==0L) {
            file = file.getParentFile();
            if(file.getName().equals(limitDeleteString)) return;
            file.delete();
        }
    }

    public void setObject(String path, Object obj) {
        String encodedObject;
        byte[] serializedObject;
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            oo.flush();
            oo.close();
            serializedObject = bo.toByteArray();
            encodedObject = Base64.getEncoder().encodeToString(serializedObject);
            getConfig().set(path, encodedObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Object getObject(String path) {
        String encodedObject = getConfig().getString(path);
        byte[] serializedObject;
        try {
            serializedObject = Base64.getDecoder().decode(encodedObject);
            ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(serializedObject));
            return oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void setBukkitObject(String path, Object obj) {
        String encodedObject;
        byte[] serializedObject;
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            BukkitObjectOutputStream oo = new BukkitObjectOutputStream(bo);
            oo.writeObject(obj);
            oo.flush();
            oo.close();
            serializedObject = bo.toByteArray();
            encodedObject = Base64.getEncoder().encodeToString(serializedObject);
            getConfig().set(path, encodedObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Object getBukkitObject(String path) {
        String encodedObject = getConfig().getString(path);
        byte[] serializedObject;
        try {
            serializedObject = Base64.getDecoder().decode(encodedObject);
            BukkitObjectInputStream oi = new BukkitObjectInputStream(new ByteArrayInputStream(serializedObject));
            return oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initPermission(File file) {
        file.setExecutable(true, false);
        file.setWritable(true, false);
        file.setReadable(true, false);
    }
}
















