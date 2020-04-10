package com.simonsejse.SubCommands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.Inventorys.ListGUI;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class ListCmd extends SubCommand {

    private FileInterface configFile;

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public FileInterface getConfigFile() {
        return configFile;
    }

    public ListCmd(){
        this.configFile = ReportSystem.getConfigFile();
    }

    private String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String getSyntax(){return "/report list";}

    public String getName(){return "list";}

    public void perform(Player p, String... args){
        if(!p.hasPermission("report.listGui")){
            ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(string -> p.sendMessage(loadColor(string)));
            return;
        }
        if (args.length > 1){
            p.sendMessage(getSyntax());
        }else if(args.length == 1){
            ListGUI listGUI = new ListGUI(0); //0 because players run command /report list
            p.openInventory(listGUI.getInventory());

        }
    }





}
