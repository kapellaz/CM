package com.example.challenge3;

import androidx.annotation.NonNull;

public class Message {
    private String userSend;
    private String userReceive;
    private String text;
    private String time;
    private int isRead;

    // Construtor
    public Message(String userSend, String userReceive, String text, String time, int isRead) {
        this.userSend = userSend;
        this.userReceive = userReceive;
        this.text = text;
        this.time = time;
        this.isRead=isRead;
    }

    // Getters e Setters
    public String getUserSend() {
        return userSend;
    }

    public void setUserSend(String userSend) {
        this.userSend = userSend;
    }

    public String getUserReceive() {
        return userReceive;
    }

    public void setUserReceive(String userReceive) {
        this.userReceive = userReceive;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getIsRead() {
        return isRead;
    }
    @NonNull
    @Override
    public String toString() {
        return userReceive;
    }

}
