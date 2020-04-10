package com.simonsejse;

import com.simonsejse.Commands.ReportCmd;
import com.simonsejse.ReportManagingSystem.Comment;
import com.simonsejse.ReportManagingSystem.EnumFlag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class MySQLManaging {

    private ReportSystem plugin = ReportSystem.getPlugin(ReportSystem.class);
    private PreparedStatement updateReports;

    public MySQLManaging(){

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

    public void addUser(UUID playerUuid, boolean isSent){
        try(ResultSet rs = plugin.getStatement().executeQuery("SELECT * from users WHERE userUuid ='"+playerUuid+"'")){
            if (rs.next()){
                int warningLevel = (rs.getInt("warningLevel") + (!isSent ? 1 : 0));
                int reportSent = (rs.getInt("reportSent") + (isSent ? 1 : 0));
                String address = rs.getString("userIp");
                updateUser(playerUuid, warningLevel, reportSent, address);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void addReports(int id, String defenseUsername, String attackerUsername, String date, EnumFlag flag, String reason, Comment[] comments, double[] location){
        try {
            updateReports = plugin.getConnection().prepareStatement("INSERT INTO reports (reportId, defenseUsername, attackerUsername, dateTime, flag, reason, x, y, z) VALUES (?,?,?,?,?,?,?,?,?) on DUPLICATE KEY UPDATE reportId = ?, defenseUsername = ?, attackerUsername = ?, dateTime = ?, flag = ?, reason = ?, x = ?, y = ?, z = ?;");
            //sql ved tal "+id+" ved strings '"+string+"' du har '' fordi at man skriver f.eks VALUES('Playername', 0); derfor skal der '' foran string
            updateReports.setInt(1, id);
            updateReports.setString(2, defenseUsername);
            updateReports.setString(3, attackerUsername);
            updateReports.setString(4, date);
            updateReports.setString(5, flag.name());
            updateReports.setString(6, reason);
            updateReports.setDouble(7, location[0]);
            updateReports.setDouble(8, location[1]);
            updateReports.setDouble(9, location[2]);
            updateReports.setInt(10, id);
            updateReports.setString(11, defenseUsername);
            updateReports.setString(12, attackerUsername);
            updateReports.setString(13, date);
            updateReports.setString(14, flag.name());
            updateReports.setString(15, reason);
            updateReports.setDouble(16, location[0]);
            updateReports.setDouble(17, location[1]);
            updateReports.setDouble(18, location[2]);
            updateReports.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addInventory(ItemStack[] inventoryContents, String defenseUsername, String attackerUsername, String dateTime, int total){
        try{
            updateReports = plugin.getConnection().prepareStatement("INSERT INTO inventory(inventoryId, defenseUsername, attackerUsername, dateTime, total) VALUES(?,?,?,?,?) on DUPLICATE KEY UPDATE inventoryId = ?, defenseUsername = ?, attackerUsername = ?, dateTime = ?, total = ?;");
            updateReports.setInt(1, updateReports.getFetchSize());
            updateReports.setString(2,defenseUsername);
            updateReports.setString(3, attackerUsername);
            updateReports.setString(4, dateTime);
            updateReports.setInt(5, total);
            updateReports.setInt(6, updateReports.getFetchSize());
            updateReports.setString(7,defenseUsername);
            updateReports.setString(8, attackerUsername);
            updateReports.setString(9, dateTime);
            updateReports.setInt(10, total);
        }catch(SQLException e){
            Bukkit.getServer().getLogger().log(Level.SEVERE, "&cSQLException when trying to add inventory to inventory table!");
        }


    }



}
