package com.simonsejse.SubCommands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class Reset extends SubCommand { //SUB-CLASS OF THE SUPERCLASS SubCommand

    private ReportSystem plugin = ReportSystem.getPlugin(ReportSystem.class);
    private FileInterface configFile;

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public FileInterface getConfigFile() {
        return configFile;
    }
    public void setPlugin(ReportSystem plugin) {
        this.plugin = plugin;
    }
    public ReportSystem getPlugin() {
        return plugin;
    }

    public Reset(){
        this.configFile = ReportSystem.getConfigFile();
    }

    private String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public String getName(){return "reset";}

    @Override
    public String getSyntax() {
        return "/report reset";
    }

    @Override
    public void perform(Player p, String... args){
        if (!p.hasPermission("report.reset")){
            ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(string -> p.sendMessage(loadColor(string)));
            return;
        }
        p.sendMessage("Config.yml has been reset.");
        plugin.setupYmlMessages();

    }
}
