package com.simonsejse.ReportManagingSystem;

import com.simonsejse.MySQLManaging;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ReportManager {

    private static List<Report> reports;

    public static MySQLManaging mySQLManaging;

    public static int getReportCount(){
        return reports.size();
    }

    public static List<Report> getReportList(){return reports;}

    public ReportManager(MySQLManaging mySQLManaging){
        reports = new ArrayList<>();
        this.mySQLManaging = mySQLManaging;
    }



    public static Report getSpecificReportById(int id){
        for(Report report : reports){
            if (report.getId() == id){
                return report;
            }
        }
        return null;
    }

    public static boolean doesReportExistsById(int id){
        return id <= reports.size() - 1;
    }

    public static void addReport(int id, String defenseUsername, String attackerUsername, String date, EnumFlag flag, String reason, Comment[] comments, double[] location){
        reports.add(new Report(id, defenseUsername, attackerUsername, date, flag, reason, comments, location));
        mySQLManaging.addReports(id, defenseUsername,attackerUsername,date,flag,reason,comments,location);
    }

    public void saveInventory(ItemStack[] inventoryContents, String defenseUsername, String attackerUsername, String dateTime, int total){
        mySQLManaging.addInventory(inventoryContents, defenseUsername, attackerUsername, dateTime, total);
    }

    public static void setReports(List<Report> reports) {
        ReportManager.reports = reports;
    }





}
