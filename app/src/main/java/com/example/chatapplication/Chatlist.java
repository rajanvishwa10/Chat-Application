package com.example.chatapplication;

public class Chatlist implements Comparable<Chatlist> {
    public String id;
    public Long date;

    public Chatlist() {
    }

    public Chatlist(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    @Override
    public int compareTo(Chatlist o) {
        if (getDate()== null || o.getDate() == null) {
            return 0;
        }
        return getDate().compareTo(o.getDate());
    }
}
