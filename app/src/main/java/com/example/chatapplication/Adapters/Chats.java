package com.example.chatapplication.Adapters;

public class Chats {
    String Sender, Receiver, Message, Date;

    public Chats() {
    }

    public Chats(String sender, String receiver, String message, String date) {
        Sender = sender;
        Receiver = receiver;
        Message = message;
        Date = date;
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
