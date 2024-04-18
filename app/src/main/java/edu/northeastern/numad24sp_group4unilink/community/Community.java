package edu.northeastern.numad24sp_group4unilink.community;

import java.util.ArrayList;

public class Community {

    private String id;

    private String tag;
    private ArrayList<String> users;
    private String picture;

    public Community() {
        // Default constructor required for Firebase
    }

    public Community(String id, String tag, ArrayList<String> users, String picture) {
        this.id=id;
        this.tag = tag;
        this.users = users;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public String getpicture() {
        return picture;
    }

    public void setPictureUrl(String pictureUrl) {
        this.picture = picture;
    }
}