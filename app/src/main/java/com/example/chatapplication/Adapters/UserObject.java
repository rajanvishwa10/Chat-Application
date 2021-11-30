package com.example.chatapplication.Adapters;

import java.util.Objects;

public class UserObject {
    private String name, phone;
    public UserObject(String name, String phone){
        this.name = name;
        this.phone = phone;
    }

    public UserObject(){

    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserObject that = (UserObject) o;
        return Objects.equals(name, that.name) && Objects.equals(phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone);
    }
}
