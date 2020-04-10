package com.simonsejse.SubCommands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.Inventorys.AdminGUI;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminCmd extends SubCommand {

    private FileInterface configFile;

    public FileInterface getConfigFile() {
        return configFile;
    }

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public AdminCmd(){
        configFile = ReportSystem.getConfigFile();
    }

    public String getSyntax(){return "/report admin";}

    public String getName(){return "admin";}

    private String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public void perform(Player p, String... args){
        if (!p.hasPermission("report.admin")){
            ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(string -> p.sendMessage(loadColor(string)));
            return;
        }
        if (args.length > 1){p.sendMessage(getSyntax());return;}
        if (args.length == 1){
            AdminGUI adminGUI = new AdminGUI();
            p.openInventory(adminGUI.getInventory());
        }
    }

}
