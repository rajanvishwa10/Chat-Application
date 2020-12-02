package com.example.chatapplication.Adapters;

public class User {
    private String phoneNumber;

    public User(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public User() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
