package com.example.finalchallenge.classes;

public class Request {

    private String sender;
    private String reciever;

    public Request(){

    }

    public Request(String sender, String reciever){
        this.sender=sender;
        this.reciever=reciever;
    }

    public String getsender() {
        return sender;
    }

    public void setsender(String sender) {
        this.sender = sender;
    }

    public String getreciever() {
        return reciever;
    }

    public void setreciever(String reciever) {
        this.reciever = reciever;
    }

    @Override
    public String toString() {
        return "Request{" +
                "sender='" + sender + '\'' +
                ", reciever=" + reciever +
                '}';
    }
}
