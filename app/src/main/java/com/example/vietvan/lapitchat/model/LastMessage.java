package com.example.vietvan.lapitchat.model;

/**
 * Created by VietVan on 24/07/2018.
 */

public class LastMessage {
    public String fromID, toID, type, content;
    public long timestamp;
    public boolean read;
    public String name, image;

    public LastMessage() {
    }

    public LastMessage(String fromID, String toID, String type, String content, long timestamp, boolean read, String name, String image) {
        this.fromID = fromID;
        this.toID = toID;
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
        this.read = read;
        this.name = name;
        this.image = image;
    }

    public String getFromID() {
        return fromID;
    }

    public void setFromID(String fromID) {
        this.fromID = fromID;
    }

    public String getToID() {
        return toID;
    }

    public void setToID(String toID) {
        this.toID = toID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
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

    @Override
    public String toString() {
        return "LastMessage{" +
                "fromID='" + fromID + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
