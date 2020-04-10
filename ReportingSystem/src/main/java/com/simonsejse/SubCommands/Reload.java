package com.simonsejse.SubCommands;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommandClasses.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class Reload extends SubCommand {

    private FileInterface configFile;

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public FileInterface getConfigFile() {
        return configFile;
    }

    public Reload(){
        this.configFile = ReportSystem.getConfigFile();
    }

    private String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public String getName(){return "reload";}

    @Override
    public String getSyntax(){return "/report reload";}

    @Override
    public void perform(Player p, String... args){
        if(!p.hasPermission("report.reload")){
            ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(string -> p.sendMessage(loadColor(string)));
            return;
        }
        if (args.length > 1){
            //WRONG SYNTAX MESSAGE MAKE LATER CAUSE UR LAZY
            return;
        }else if (args.length == 1){
            p.sendMessage("Config file has been reloaded."); //make it custom message later in config.yml lazy now
            ReportSystem.getPlugin(ReportSystem.class).getServer().getPluginManager().disablePlugin(ReportSystem.getPlugin(ReportSystem.class));
            onDisable();
            onEnable();
        }
    }
    public void onDisable()
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED+ "Disabling REPORT SYSTEM plugin!");
        ReportSystem.getPlugin(ReportSystem.class).getServer().getPluginManager().disablePlugin(ReportSystem.getPlugin(ReportSystem.class));
    }

    public void onEnable()
    {
        ReportSystem.getPlugin(ReportSystem.class).getServer().getPluginManager().enablePlugin(ReportSystem.getPlugin(ReportSystem.class));
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+ "Enabling REPORT SYSTEM plugin!");
    }
}
