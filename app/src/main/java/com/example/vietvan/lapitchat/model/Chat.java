package com.example.vietvan.lapitchat.model;

/**
 * Created by VietVan on 16/07/2018.
 */

public class Chat {
    public String seen;
    public long time;

    public Chat() {
    }

    public Chat(String seen, long time) {
        this.seen = seen;
        this.time = time;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
