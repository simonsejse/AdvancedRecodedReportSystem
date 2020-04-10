package com.simonsejse.Commands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.MySQLManaging;
import com.simonsejse.ReportManagingSystem.Comment;
import com.simonsejse.ReportManagingSystem.EnumFlag;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.SubCommand;
import com.simonsejse.SubCommands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReportCmd implements CommandExecutor {

    private ReportSystem plugin = ReportSystem.getPlugin(ReportSystem.class);
    private List<SubCommand> subCommandList; //zero default value = null
    /*
    Class objects
     */
    /*
    message variable initialization then set later
    */
    private Map<String, Long> cooldowns;
    private FileInterface configFile;
    private List<String> reportSent;

    public Map<String, Long> getCoolDowns(){
        return cooldowns;
    }

    public FileInterface getConfigFile(){
        return configFile;
    }

    public List<String> getReportSent(){
        return reportSent;
    }

    private MySQLManaging mySQLManaging;
    public ReportCmd(MySQLManaging mySqlManaging){
        this(new Reset()
                , new Reload()
                , new View()
                , new subComment()
                , new Flag()
                , new ListCmd()
                , new AdminCmd()
                , new HelpCmd(),
                new Delete());
        this.configFile = ReportSystem.getConfigFile();
        this.mySQLManaging = mySqlManaging;
        cooldowns = new HashMap<>();
    }

    public ReportCmd(SubCommand... subCommand){
        subCommandList = new ArrayList<>();
        Collections.addAll(subCommandList, subCommand);
    }

    public String loadColor(String color){
        return ChatColor.translateAlternateColorCodes('&', color);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(!(sender instanceof Player)){return false;}
        Player p = (Player) sender;
        if (args.length >= 1){
            for(int i = subCommandList.size() - 1; i >= 0; i--){
                if (subCommandList.get(i).getName().equalsIgnoreCase(args[0])){
                    subCommandList.get(i).perform(p, args);
                    return false;
                }
            }
            if (!p.hasPermission("report.player")){
                ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(string -> p.sendMessage(loadColor(string)));
                return false;
            }
            //if (p.getName().equalsIgnoreCase(args[0])){
              //  List<String> cantReport = (List<String>) configFile.get("Messages.cantReportYourself");
                //for(String s : cantReport){
                  //  p.sendMessage(loadColor(s));
                //}
                //return false;
            //}
            if (args.length < 2){
                List<String> missingReason = (List<String>) configFile.get("Messages.missingReason");
                for(String s : missingReason){
                    p.sendMessage(loadColor(s));
                }
                return false;
            }
            if (!(boolean) configFile.get("Settings.reportedPlayerHasToBeOnline")){ //Player doesn't have to be online, checks if player has ever played:
                if (!hasPlayerPlayedBefore(args[0].trim().toLowerCase())){
                    List<String> neverPlayed = (List<String>) configFile.get("Messages.playerNeverPlayed");
                    for(String s : neverPlayed){
                        if (s.contains("{player}")) s = s.replace("{player}", args[0]);
                        p.sendMessage(loadColor(s));
                    }
                    return false;
                }
                //player has played before, continues to report player:
            }else{ //Player has to be online:
                if(!isPlayerOnline(args[0])) {
                    List<String> notOnline = (List<String>) configFile.get("Messages.playerNotOnline");
                    for(String s : notOnline){
                        if (s.contains("{player}")) s = s.replace("{player}", args[0]);
                        p.sendMessage(loadColor(s));
                    }
                    return false;
                }


                //player is online, continues to report player:
            }
            //INITIALIZING WITHIN VARIABLE SCOPE  (Scope refers to the visibility of variables.)
            int coolDownTime = 30;
            try {
                coolDownTime = (int) configFile.get("Messages.delayTime");
            } catch (NumberFormatException nfe) {
               Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The chosen number in your CONFIG.YML - Messages.delayTime is not a given number. Change it!");
            }
            if (getCoolDowns().containsKey(p.getName())) {
                long timeLeft = ((cooldowns.get(p.getName()) / 1000) + coolDownTime) - (System.currentTimeMillis() / 1000);
                if (timeLeft > 0) {
                    List<String> delayMessage = (List<String>) configFile.get("Messages.Delay");
                    delayMessage.forEach(s -> {
                        if (s.contains("{sec}")) {
                            s = s.replace("{sec}", "" + timeLeft);
                        }
                        p.sendMessage(loadColor(s));
                    });
                    return false;
                }
            }
            cooldowns.put(p.getName(), System.currentTimeMillis());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String date = dtf.format(now);

            int id = ReportManager.getReportCount();
            StringBuilder reason = new StringBuilder();

            for (int i = 1; i < args.length; i++) {
                reason.append(args[i]).append(" ");
            }

            Location loc = p.getLocation();
            ReportManager.addReport(id, args[0], p.getName(), date, EnumFlag.OPEN, reason.toString(), new Comment[]{}, new double[]{loc.getX(), loc.getY(), loc.getZ()});

            List<String> wait = (List<String>) configFile.get("Messages.Wait");
            for (String string : wait) {
                p.sendMessage(loadColor(string));
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    reportSent = (List<String>) configFile.get("Messages.Reported_Someone_Successful");
                    reportSent.forEach(string -> {
                        if (string.contains("{id}"))
                            string = string.replace("{id}", String.valueOf(id));
                        if (string.contains("{reportedPlayer}"))
                            string = string.replace("{reportedPlayer}", args[0]);
                        if (string.contains("{player}"))
                            string = string.replace("{player}", p.getName());
                        if (string.contains("{date}"))
                            string = string.replace("{date}", date);
                        if (string.contains("{reason}"))
                            string = string.replace("{reason}", reason.toString());

                        p.sendMessage(loadColor(string));
                    });
                    cancel();
                }
            }.runTaskTimer(plugin, 50L, 0L);

            mySQLManaging.addUser(p.getUniqueId(), true);
            mySQLManaging.addUser(getUuidOfString(args[0]), false);

        }else{
            List<String> no_arguments = (List<String>) configFile.get("Messages.NO_ARGUMENTS");
            for (String no_argument : no_arguments) {
                p.sendMessage(loadColor(no_argument));
            }
            return false;
        }
        return true;
    }

    public void updateUser(UUID uuid, int warningLevel, int reportSent, String address){
        try{
            String query = "INSERT INTO users(userUuid, warningLevel, reportSent, userIp) VALUES(?,?,?,?) on DUPLICATE KEY UPDATE userUuid = ?, warningLevel = ?, reportSent = ?, userIp = ?;";
            PreparedStatement preparedStatement = plugin.getConnection().prepareStatement(query);
            preparedStatement.setString(1, String.valueOf(uuid));
            preparedStatement.setInt(2, warningLevel);
            preparedStatement.setInt(3, reportSent);
            preparedStatement.setString(4, address);
            preparedStatement.setString(5, String.valueOf(uuid));
            preparedStatement.setInt(6, warningLevel);
            preparedStatement.setInt(7, reportSent);
            preparedStatement.setString(8, address);

            preparedStatement.execute();
        }catch(SQLException sql){
            plugin.getServer().getLogger().log(Level.SEVERE, null, ReportCmd.class);
        }
    }


    public UUID getUuidOfString(String name){
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    public boolean isPlayerOnline(String playerName){
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.equalsIgnoreCase(playerName)).collect(Collectors.toList()).size() == 1 ? true : false;
    }

    public boolean hasPlayerPlayedBefore(String playerName){
        return Bukkit.getOfflinePlayer(playerName).hasPlayedBefore();
    }

}
