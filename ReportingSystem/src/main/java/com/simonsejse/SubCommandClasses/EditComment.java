package com.simonsejse.SubCommandClasses;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.Comment;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class EditComment extends CommentArgs {

    private FileInterface configFile;

    public EditComment(){
        this.configFile = ReportSystem.getConfigFile();
    }

    @Override
    public String getName() {
        return "edit";
    }

    private String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public void perform(Player p, String... args) {  //report comment edit <id> <line> <comment>
        if (args.length == 4){
            ((List<String>) configFile.get("Messages.Comment.Edit.lastArgument")).forEach(s -> p.sendMessage(loadColor(s)));
            return;
        }
        if (args.length > 4){
            int id = 0;
            try{
                id = Integer.parseInt(args[2]);
            }catch(NumberFormatException nfe){
                ((List<String>) configFile.get("Messages.ARGUMENT_NOT_A_NUMBER")).forEach(s -> {
                   if (s.contains("{number}")) s = s.replace("{number}", args[2]);
                   p.sendMessage(loadColor(s));
                   return;
                });
            }
            int line = 0;
            try{
                line = Integer.parseInt(args[3]);
            }catch(NumberFormatException nfe){
                ((List<String>) configFile.get("Messages.ARGUMENT_NOT_A_NUMBER")).forEach(s -> {
                    if (s.contains("{number}")) s = s.replace("{number}", args[3]);
                    p.sendMessage(loadColor(s));
                    return;
                });
            }
            if (line == 0 | line < 0){
                int finalLine = line;
                ((List<String>) configFile.get("Messages.Comment.invalidLine")).forEach(s->{
                    if(s.contains("{line}")) s = s.replace("{line}", String.valueOf(finalLine));
                    p.sendMessage(loadColor(s));
                });
                return;
            }
            /*
            Both args[2] and args[3] are numbers or else they would've return; and stopped the method.
             */
            if (id <= ReportManager.getReportCount() - 1) {
                Report report = ReportManager.getSpecificReportById(id);
                if (report.getComments().length == 0) {
                    ((List<String>) configFile.get("Messages.Comment.noComments")).forEach(s -> p.sendMessage(loadColor(s)));
                    return;
                }

                if (line <= report.getComments().length) {
                    //Line is within scope of comments
                    for (Comment comment : report.getComments()) {
                        if (comment.getId() == line) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 4; i < args.length; i++) {
                                sb.append(args[i] + " ");
                            }
                            comment.setComment(sb.toString());
                            int finalLine = line;
                            int finalId = id;
                            ((List<String>) configFile.get("Messages.Comment.editedComment")).forEach(s -> {
                                if (s.contains("{line}")) s = s.replace("{line}", String.valueOf(finalLine));
                                if (s.contains("{id}")) s = s.replace("{id}", String.valueOf(finalId));
                                if (s.contains("{comment}")) s = s.replace("{comment}", sb.toString());
                                p.sendMessage(loadColor(s));
                            });
                            return;
                        }
                    }
                } else {
                    ((List<String>) configFile.get("Messages.Comment.commentDoesntExist")).forEach(s -> p.sendMessage(loadColor(s)));
                }
            }else{
                ((List<String>) configFile.get("Messages.Report_not_found")).forEach(s-> {
                    p.sendMessage(loadColor(s));
                });
            }
        }
    }
}
