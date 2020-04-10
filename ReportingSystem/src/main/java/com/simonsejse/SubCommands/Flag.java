package com.simonsejse.SubCommands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.EnumFlag;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class Flag extends SubCommand {

    private FileInterface configFile;

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public FileInterface getConfigFile(){
        return configFile;
    }

    public Flag(){
        this.configFile = ReportSystem.getConfigFile();
    }

    public String getSyntax(){return "/report flag <id> closed/open";}

    public String getName(){return "flag";}

    public String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public void perform(Player p, String... args){
        if (!p.hasPermission("report.flag")){
            ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(string -> p.sendMessage(loadColor(string)));
            return;
        }
        if (args.length == 1){
            List<String> noArguments = (List<String>) configFile.get("Messages.flag.no_arguments");
            noArguments.forEach(String -> p.sendMessage(loadColor(String)));
        }else if (args.length == 2){
            ((List<String>) configFile.get("Messages.flag.one_argument")).forEach(s -> p.sendMessage(loadColor(s)));
        }else if (args.length == 3){
            try{
                int id = Integer.parseInt(args[1]);
                if(ReportManager.doesReportExistsById(id)){ //checks if id exists.
                    Report report = ReportManager.getSpecificReportById(id);
                    assert report != null;
                    if (args[2].equalsIgnoreCase("closed")){
                        report.setFlag(EnumFlag.CLOSED);
                        ((List<String>) configFile.get("Messages.flag.closed")).forEach(s -> p.sendMessage(loadColor(s)));
                    }else if (args[2].equalsIgnoreCase("pending")){
                        report.setFlag(EnumFlag.PENDING);
                        ((List<String>) configFile.get("Messages.flag.pending")).forEach(s -> p.sendMessage(loadColor(s)));
                    }else if (args[2].equalsIgnoreCase("working")){
                        report.setFlag(EnumFlag.WORKING);
                        ((List<String>) configFile.get("Messages.flag.working")).forEach(s -> p.sendMessage(loadColor(s)));
                    }else if (args[2].equalsIgnoreCase("open")){
                        report.setFlag(EnumFlag.OPEN);
                        ((List<String>) configFile.get("Messages.flag.open")).forEach(s -> p.sendMessage(loadColor(s)));
                    }else{
                        ((List<String>) configFile.get("Messages.flag.onlyCloseOrOpen")).forEach(s -> p.sendMessage(loadColor(s)));
                        return;
                    }
                }else{
                    ((List<String>) configFile.get("Messages.Report_not_found")).forEach(s -> p.sendMessage(loadColor(s)));
                }
            }catch (NumberFormatException nfe){
                ((List<String>) configFile.get("Messages.ARGUMENT_NOT_A_NUMBER")).forEach(s-> p.sendMessage(loadColor(s)));
            }
        }
    }
}
