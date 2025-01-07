package com.example.finalchallenge.classes;

public class Request {

    private String senderName;  // Corrected variable name
    private String senderID;   // Corrected variable name to 'receiver'

    // Default constructor
    public Request() {
    }

    // Constructor with parameters
    public Request( String senderName, String senderID) {
        this.senderName = senderName;  // Corrected variable names
        this.senderID = senderID;
    }

    // Getter for senderName
    public String getSenderName() {
        return senderName;
    }

    // Setter for senderName
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    // Getter for senderID
    public String getSenderID() {
        return senderID;
    }

    // Setter for senderID
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }


    @Override
    public String toString() {
        return "Request{" +
                ", senderName='" + senderName + '\'' +
                ", senderID='" + senderID + '\'' +
                '}';
    }
}
