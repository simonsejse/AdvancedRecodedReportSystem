package com.simonsejse.FileLoadSaver;

import com.simonsejse.ReportSystem;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile implements FileInterface {

    private ReportSystem plugin = ReportSystem.getPlugin(ReportSystem.class);

    private File f;
    private YamlConfiguration yamlConfiguration;


    public ConfigFile(String filename){
        f = new File(plugin.getDataFolder(), filename);
        yamlConfiguration = YamlConfiguration.loadConfiguration(f);
    }
    public ConfigFile(String path, String filename){
        f = new File(plugin.getDataFolder() + File.separator + path, filename);
        yamlConfiguration = YamlConfiguration.loadConfiguration(f);
    }

    public File getFile(){return f;}
    public YamlConfiguration getYaml(){return yamlConfiguration;}

    @Override
    public Object get(String path){
        return yamlConfiguration.get(path);
    }


    @Override
    public void create() {
        if(!f.exists()){
            f.getParentFile().mkdirs();
            try{
                f.createNewFile();
            }catch(IOException e){e.printStackTrace();}
            plugin.setupYmlMessages();
        }

    }

    @Override
    public void load() {
        try{
            yamlConfiguration.load(f);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try{
            yamlConfiguration.save(f);
        }catch(IOException e){e.printStackTrace();}
    }

    @Override
    public void set(String path, Object object) {
        yamlConfiguration.set(path, object);
        save();
    }
}
