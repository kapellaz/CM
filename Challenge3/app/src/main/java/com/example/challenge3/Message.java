package com.example.challenge3;

import androidx.annotation.NonNull;

public class Message {
    private String userSend;
    private String userReceive;
    private String text;
    private String time;

    // Construtor
    public Message(String userSend, String userReceive, String text, String time) {
        this.userSend = userSend;
        this.userReceive = userReceive;
        this.text = text;
        this.time = time;
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

    @NonNull
    @Override
    public String toString() {
        return userReceive;
    }
}
