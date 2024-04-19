package edu.northeastern.numad24sp_group4unilink.messages.bean;

import java.io.Serializable;

public class ChatBean implements Serializable {
    private long id;
    private String toUser;//send email
    private String fromUser;//from email
    private String content;//content
    private String mark;

    private long time;//time

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
