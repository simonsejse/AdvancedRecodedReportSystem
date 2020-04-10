package com.simonsejse.SubCommands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class HelpCmd extends SubCommand {


    private FileInterface configFile;

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public FileInterface getConfigFile() {
        return configFile;
    }

    public HelpCmd(){
        this.configFile = ReportSystem.getConfigFile();
    }


    public String getName(){
        return "help";
    }

    public String getSyntax(){
        return "/report help";
    }

    private String loadColor(String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void perform(Player p, String... args){
        if(!p.hasPermission("report.help")){
            ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(string -> p.sendMessage(loadColor(string)));
            return;
        }
        List<String> helpCmd = (List<String>) configFile.get("Messages.HelpCmd.message");
        for(String s : helpCmd){
            p.sendMessage(loadColor(s));
        }


    }
}
