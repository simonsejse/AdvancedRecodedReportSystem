package com.simonsejse.SubCommands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.CommentArgs;
import com.simonsejse.SubCommandClasses.DeleteComment;
import com.simonsejse.SubCommandClasses.EditComment;
import com.simonsejse.SubCommandClasses.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class subComment extends SubCommand {

    private ReportSystem plugin = ReportSystem.getPlugin(ReportSystem.class);
    private FileInterface configFile;
    private List<CommentArgs> commentArguments;
    private int maximumComments;

    public FileInterface getConfigFile() {
        return configFile;
    }

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public subComment(){
        commentArguments = new ArrayList<>(Arrays.asList(new CommentArgs[]{new EditComment(), new DeleteComment()}));
        this.configFile = ReportSystem.getConfigFile();

        try{
            maximumComments = Integer.parseInt(configFile.get("Messages.Comment.maximumAmountOfComments").toString());
        }catch(NumberFormatException nfe){
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Messages.Comment.maximumAmountOfComments is not a number! Change in config.yml!");
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Plugin has been disabled!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
    }


    public String getName(){
        return "comment";
    }

    public String getSyntax(){
        return "/report comment <id> <string>";
    }

    public String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public void perform(Player p, String... args){
        if(!p.hasPermission("report.comment")){
            ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(string -> p.sendMessage(loadColor(string)));
            return;
        }
        if (args.length == 1){
            List<String> not_enough_arguments = (List<String>) configFile.get("Messages.Comment.NO_ARGUMENT_COMMENT");
            for(String s : not_enough_arguments){
                p.sendMessage(loadColor(s));
            }
        }else if (args.length == 2){
            List<String> one_argument_given = (List<String>) configFile.get("Messages.Comment.ONE_ARGUMENT_GIVEN");
            for(String s : one_argument_given){
                p.sendMessage(loadColor(s));
            }
        }else if (args.length > 2){
            //report comment edit <id> <line> <comment>
            for(CommentArgs commentArgs : commentArguments){
                if (commentArgs.getName().equalsIgnoreCase(args[1])){
                    commentArgs.perform(p, args);
                    return;
                }
            }
            StringBuilder newComment = new StringBuilder();

            for(int i = 2;i<args.length;i++){
                newComment.append(args[i]).append(" ");
            }
            try{
                int number = Integer.parseInt(args[1]);
                if(ReportManager.doesReportExistsById(number)){
                    Report report = ReportManager.getSpecificReportById(number);
                    assert report != null;
                    if (report.getComments().length < maximumComments){
                        if(report.isFlag().name().equals("OPEN")){
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                            LocalDateTime localDateTime = LocalDateTime.now();
                            String date = dtf.format(localDateTime);

                            report.addComment(newComment.toString(), date, p.getName());

                            List<String> commentSuccess = (List<String>) configFile.get("Messages.Comment.commented_successfully");
                            commentSuccess.forEach(string -> {
                                if (string.contains("{id}")) string = string.replace("{id}", String.valueOf(number));
                                p.sendMessage(loadColor(string));
                            });
                        }else if (report.isFlag().name().equals("CLOSED")){
                            ((List<String>) configFile.get("Messages.Comment.closed_thread")).forEach(s-> p.sendMessage(loadColor(s)));
                        }

                    }else{
                        ((List<String>) configFile.get("Messages.Comment.maximumCommentsReached")).forEach(s -> p.sendMessage(loadColor(s)));
                    }
                }else{
                    ((List<String>) configFile.get("Messages.Report_not_found")).forEach(s -> p.sendMessage(loadColor(s)));
                }
            }catch(NumberFormatException nfe){
                ((List<String>) configFile.get("Messages.ARGUMENT_NOT_A_NUMBER")).forEach(s->p.sendMessage(loadColor(s)));
            }
        }

    }

}
