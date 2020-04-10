package com.simonsejse.FileLoadSaver;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public interface FileInterface {

    void create();
    void save();
    void load();
    void set(String path, Object object);
    Object get(String path);
    File getFile();
    YamlConfiguration getYaml();

}
