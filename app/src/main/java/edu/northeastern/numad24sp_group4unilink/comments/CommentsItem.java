package edu.northeastern.numad24sp_group4unilink.comments;


public class CommentsItem implements CommentsInterface {

    private String userEmail;
    private String comment;



    public CommentsItem(String userEmail, String comment) {

        this.userEmail = userEmail;
        this.comment = comment;

    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getComment() {
        return comment;
    }
}
