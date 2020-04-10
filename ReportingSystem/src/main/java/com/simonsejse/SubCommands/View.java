package com.simonsejse.SubCommands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.Inventorys.UserInfoGUI;
import com.simonsejse.ReportManagingSystem.Comment;
import com.simonsejse.ReportManagingSystem.EnumFlag;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class View extends SubCommand {

    private FileInterface configFile;

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public FileInterface getConfigFile() {
        return configFile;
    }

    public View(){
        this.configFile = ReportSystem.getConfigFile();
    }


    public String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String getName(){return "view";}
    public String getSyntax(){return "/report view";}

    public void perform(Player p, String... args){
        if (!p.hasPermission("report.view")){
            ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(brad -> p.sendMessage(loadColor(brad)));
            return;
        }
        if (args.length == 1){
            ((List<String>) configFile.get("Messages.ONE_ARGUMENT")).forEach(brad -> p.sendMessage(loadColor(brad)));
            return;
        }else if (args.length == 2){
            try{
                int number = Integer.parseInt(args[1]);
                if (number >= ReportManager.getReportCount()){
                    List<String> report_not_found = (List<String>) configFile.get("Messages.Report_not_found");
                    for(String s : report_not_found){
                        p.sendMessage(loadColor(s));
                    }
                    return;
                }

                Report report = ReportManager.getSpecificReportById(number);
                List<String> reportViewer = (List<String>) configFile.get("Messages.ReportViewer");
                reportViewer.forEach(string -> {
                    assert report != null;
                    if(string.contains("{id}")) {
                        string = string.replace("{id}", String.valueOf(report.getId()));
                    }
                    if(string.contains("{reportedPlayer}")) {
                        string = string.replace("{reportedPlayer}", String.valueOf(report.getDefenseUsername()));
                    }
                    if(string.contains("{player}")) {
                        string = string.replace("{player}", String.valueOf(report.getAttackerUsername()));
                    }
                    if(string.contains("{date}")) {
                        string = string.replace("{date}", String.valueOf(report.getDate()));
                    }
                    if(string.contains("{flag}")) {
                        string = string.replace("{flag}", getFlag(report.isFlag()));
                    }
                    if(string.contains("{reason}")) {
                        string = string.replace("{reason}", String.valueOf(report.getReason()));
                    }
                    if(string.contains("{amountcomments}")) {
                        string = string.replace("{amountcomments}", String.valueOf(report.getComments().length));
                    }
                    if (string.contains("{comments}") && report.getComments().length == 0){
                        return;
                    }
                    if(string.contains("{comments}")) {
                        for (int i = 0; i < report.getComments().length; i++) {
                            Comment comment = report.getComments()[i];
                            p.sendMessage(loadColor("&4"+comment.getId()+ "&7. [ID:&c"+comment.getId()+"&7]"+" &7[&c"+comment.getDate()+"&7]&c"+comment.getCommenter()+"&7: "+comment.getComment()));
                        }
                        return; //return so it doesnt print out var string = {comment}
                    }
                    p.sendMessage(loadColor(string));
                });
            }catch(NumberFormatException nfe){
               // UserInfoGUI userInfoGUI = new UserInfoGUI(args[1]);
                // p.openInventory(userInfoGUI.getInventory());
                /*









SPACE SO I DONT FORGET TO FIX








                 */
            }

        }

    }

    private String getFlag(EnumFlag enums){
        switch(enums){
            case OPEN:
                return "&a&lOpen";
            case CLOSED:
                return "&c&lClosed";
            case PENDING:
                return "&5&lPENDING";
            case WORKING:
                return "&e&lWORKING";
        }
        return null;
    }

}
