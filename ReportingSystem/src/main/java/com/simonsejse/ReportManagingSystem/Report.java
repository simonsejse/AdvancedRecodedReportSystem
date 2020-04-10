package com.simonsejse.ReportManagingSystem;

import com.simonsejse.ReportSystem;
import org.apache.commons.lang.ArrayUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;


public class Report {

    private int id;
    private String defenseUsername;
    private String attackerUsername;
    private String date;
    private EnumFlag flag;
    private String reason;
    private Comment[] comments;
    private double[] location;
    private PreparedStatement updateComments = null;


    //making an ellipse to make String[]
    public Report(int id, String defenseUsername, String attackerUsername, String date, EnumFlag flag, String reason, Comment[] comments, double[] location){
        this.id = id;
        this.defenseUsername = defenseUsername;
        this.attackerUsername = attackerUsername;
        this.date = date;
        this.flag = flag;
        this.reason = reason;
        this.comments = comments;
        this.location = location;

    }//


    public int getId(){return id;}
    public String getDefenseUsername(){return defenseUsername;}
    public String getAttackerUsername(){return attackerUsername;}
    public String getDate(){return date;}
    public EnumFlag isFlag(){return flag;}
    public String getReason(){return reason;}
    public Comment[] getComments(){return comments;}
    public double[] getLocation(){
        return location;
    }


    public void setId(int id){
        this.id = id;
    }
    public void setDefenseUsername(String defenseUsername){
        this.defenseUsername = defenseUsername;
    }
    public void setAttackerUsername(String attackerUsername){
        this.attackerUsername = attackerUsername;
    }
    public void setDate(String date){
        this.date = date;
    }
    public void setFlag(EnumFlag flag){
        this.flag = flag;
    }
    public void setReason(String reason){this.reason = reason;}
    public void setComments(Comment[] comments){
        this.comments = comments;
    }
    public void setLocation(double[] location){
        this.location = location;
    }

    public void addComment(String comment, String date, String commenter){
        comments = (Comment[]) ArrayUtils.add(comments, new Comment(comment, date, getComments().length + 1, commenter));
        try{
            updateComments = ReportSystem.getPlugin(ReportSystem.class).getConnection().prepareStatement("INSERT INTO comments (reportId, commentId, comment, commenter, dateTime) VALUES(?,?,?,?,?) on DUPLICATE KEY UPDATE reportId = ?, commentId = ?, comment = ?, commenter = ?, dateTime = ?;");
            updateComments.setInt(1, getId());
            updateComments.setInt(2, comments.length);
            updateComments.setString(3, comment);
            updateComments.setString(4, commenter);
            updateComments.setString(5, date);
            updateComments.setInt(6, getId());
            updateComments.setInt(7, comments.length);
            updateComments.setString(8, comment);
            updateComments.setString(9, commenter);
            updateComments.setString(10, date);
            updateComments.executeUpdate();
        }catch(SQLException sql){
            sql.printStackTrace();
        }
    }

    public Report getInstance(){
        return this;
    }



}
