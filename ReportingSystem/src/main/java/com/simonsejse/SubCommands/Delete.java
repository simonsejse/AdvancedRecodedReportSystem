package com.simonsejse.SubCommands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class Delete extends SubCommand {

    private FileInterface configFile;

    public FileInterface getConfigFile() {
        return configFile;
    }

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public Delete()
    {
        this.configFile = ReportSystem.getConfigFile();
    }

    private String loadColor(String s)
    {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public String getName(){
        return "delete";
    }

    @Override
    public String getSyntax(){
        return "/report delete <id>";
    }

    @Override
    public void perform(Player p, String... args){
        if (!p.hasPermission("report.delete")){
            ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(s -> p.sendMessage(loadColor(s)));
            return;
        }
        if (args.length == 1){
            ((List<String>) configFile.get("Messages.delete.oneArgument")).stream().forEach(s -> p.sendMessage(loadColor(s)));
            return;
        }
        if (args.length == 2){
            try{
                int id = Integer.parseInt(args[1]);
                //id is a number
                if (!ReportManager.doesReportExistsById(id)) {
                    ((List<String>) configFile.get("Messages.Report_not_found")).stream().forEach(s -> p.sendMessage(loadColor(s)));
                    return;
                }
                List<Report> reports = ReportManager.getReportList();
                reports.remove(id);
                /*
                @param
                /Automatically every report goes one ID down, but we have to set their ids manually.
                 */
                reports.stream().filter(report -> report.getId() > id).forEach(report -> report.setId(report.getId() - 1));

                ((List<String>) configFile.get("Messages.delete.deletedSuccess")).stream().forEach(s -> {
                    if (s.contains("{id}")) s = s.replace("{id}", String.valueOf(id));
                    p.sendMessage(loadColor(s));
                });
            }catch(NumberFormatException nfe){
                ((List<String>) configFile.get("Messages.ARGUMENT_NOT_A_NUMBER")).stream().forEach(s -> p.sendMessage(loadColor(s)));
                return;
            }
        }
    }
}
