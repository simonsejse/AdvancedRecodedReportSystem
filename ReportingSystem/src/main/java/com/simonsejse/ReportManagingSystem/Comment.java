package com.simonsejse.ReportManagingSystem;

public class Comment {

    private String comment;
    private int id;
    private String date;
    private String commenter;

    public Comment(String comment, String date, int id, String commenter){
        this.comment = comment;
        this.id = id;
        this.date = date;
        this.commenter = commenter;
    }

    public String getCommenter(){return commenter;}

    public String getComment(){
        return comment;
    }
    public int getId(){
        return id;
    }
    public String getDate(){
        return date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCommenter(String commenter) {
        this.commenter = commenter;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setId(int id) {
        this.id = id;
    }

}
