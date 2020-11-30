package com.example.chatapplication;

public class RecentChat {
    String phoneNumber;

    public RecentChat(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public RecentChat() {
    }
}
