package com.simonsejse.SubCommandClasses;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.Comment;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DeleteComment extends CommentArgs {

    private FileInterface configFile;

    private String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public DeleteComment(){
        this.configFile = ReportSystem.getConfigFile();
    }

    @Override
    public String getName(){
        return "delete";
    }

    @Override
    public void perform(Player p, String... args){
        // /report comment delete 2 1
        if (args.length == 4){
            int report_id;
            int line;
           if(isANumber(args[2])) {
               if (isANumber(args[3])) {
                   report_id = Integer.parseInt(args[2]);
                   line = Integer.parseInt(args[3]);
                   if(!ReportManager.doesReportExistsById(report_id)){
                       ((List<String>) configFile.get("Messages.Report_not_found")).forEach(s -> p.sendMessage(loadColor(s)));
                       return;
                   }
                   Report report = ReportManager.getSpecificReportById(report_id);
                   Comment[] comments = report.getComments();
                   if (comments.length == 0 || report.getComments().length < 0) {
                       ((List<String>) configFile.get("Messages.Comment.noComments")).forEach(s -> p.sendMessage(loadColor(s)));
                       return;
                   }
                   if (Arrays.stream(comments).filter(comment -> comment.getId() == line).findAny().isPresent()) {
                        for(int i = 0;i<comments.length;i++){
                            if (comments[i].getId() == line){
                                comments = (Comment[]) ArrayUtils.remove(comments, i);
                                //Changes all other comment ids
                                Arrays.stream(comments).filter(comment -> comment.getId() > line).forEach(c -> c.setId(c.getId() - 1));
                                report.setComments(comments);
                                String query = "DELETE FROM comments WHERE reportId = '"+report_id+"' and commentId = '"+line+"';";
                                try {
                                    ReportSystem.getPlugin(ReportSystem.class).getStatement().executeUpdate(query);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                //tilføj MYSQL der sletter kommentar
                                //tilføj MYSQL der sletter kommentar
                                //tilføj MYSQL der sletter kommentar
                                //tilføj MYSQL der sletter kommentar

                            }
                        }
                   }else{
                       ((List<String>) configFile.get("Messages.Comment.invalidLine")).forEach(s -> {
                           if (s.contains("{line}")) s = s.replace("{line}", String.valueOf(line));
                           p.sendMessage(loadColor(s));
                       });
                   }
               } else {
                   ((List<String>) configFile.get("Messages.ARGUMENT_NOT_A_NUMBER")).forEach(s -> {
                       if (s.contains("{number}")) s = s.replace("{number}", args[3]);
                       p.sendMessage(loadColor(s));
                   });
               }
           }else{
               ((List<String>) configFile.get("Messages.ARGUMENT_NOT_A_NUMBER")).forEach(s -> {
                   if (s.contains("{number}")) s = s.replace("{number}", args[2]);
                   p.sendMessage(loadColor(s));
               });
           }
        }else if (args.length == 2){

        }else if (args.length == 1){


        }
    }

    public boolean isANumber(String number){
        try{
            Integer.parseInt(number);
            return true;
        }catch(NumberFormatException nfe){
            return false;
        }
    }

}
