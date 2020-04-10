package com.simonsejse;

import com.simonsejse.FileLoadSaver.FileInterface;
import org.bukkit.ChatColor;

public class ChatManage {

    private static FileInterface configFile;

    public ChatManage(){
        configFile = ReportSystem.getConfigFile();
    }

    public static String getMsg(String msg){
        return loadColor(configFile.get(msg).toString());
    }

    public static String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
