package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.main;

import java.io.File;

public class FileUtil {
    private static final File OUTER_PLUGIN_FOLDER = new File(main.getPlugin().getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParentFile() + "/" + main.PLUGIN_FOLDER_NAME).getAbsoluteFile();


    public static File getOuterPluginFolder() {
        return OUTER_PLUGIN_FOLDER;
    }
    public static File getOuterPluginFolder(String childPath) {
        return new File(OUTER_PLUGIN_FOLDER + "/" + childPath);
    }

}
