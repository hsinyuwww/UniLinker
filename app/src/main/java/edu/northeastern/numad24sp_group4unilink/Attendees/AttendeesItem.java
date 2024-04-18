package edu.northeastern.numad24sp_group4unilink.Attendees;

public class AttendeesItem implements AttendeesInterface{

    private String userEmail;


    public AttendeesItem(String userEmail) {

        this.userEmail = userEmail;

    }

    public String getUserEmail() {
        return userEmail;
    }

}
