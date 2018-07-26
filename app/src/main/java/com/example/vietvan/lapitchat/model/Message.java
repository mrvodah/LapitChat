package com.example.vietvan.lapitchat.model;

/**
 * Created by VietVan on 14/07/2018.
 */

public class Message {
    public String message, seen, type, from;
    public long time;

    public Message() {
    }

    public Message(String message, String seen, String type, String from, long time) {
        this.message = message;
        this.seen = seen;
        this.type = type;
        this.from = from;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
