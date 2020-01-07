package com.example.vietvan.lapitchat.model;

/**
 * Created by VietVan on 14/07/2018.
 */

public class Message {
    public String message, seen, type, from;
    public long time;
    public String name, image, online, key;

    public Message() {
    }

    public Message(String message, String seen, String type, String from, long time) {
        this.message = message;
        this.seen = seen;
        this.type = type;
        this.from = from;
        this.time = time;
    }

    public Message(String message, String seen, String from, long time, String name, String image, String online) {
        this.message = message;
        this.seen = seen;
        this.from = from;
        this.time = time;
        this.name = name;
        this.image = image;
        this.online = online;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
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
