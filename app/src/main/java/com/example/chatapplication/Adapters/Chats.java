package com.example.chatapplication.Adapters;

public class Chats {
    String Sender, Receiver, Message, Date;
    boolean isseen;

    public Chats() {
    }

    public Chats(String sender, String receiver, String message, String date, boolean isseen) {
        Sender = sender;
        Receiver = receiver;
        Message = message;
        Date = date;
        this.isseen = isseen;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean seen) {
        isseen = seen;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String getReceiver() {
        return Receiver;
    }

    public void setReceiver(String receiver) {
        Receiver = receiver;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
