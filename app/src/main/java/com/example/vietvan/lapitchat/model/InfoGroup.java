package com.example.vietvan.lapitchat.model;

/**
 * Created by VietVan on 25/07/2018.
 */

public class InfoGroup {
    public String avatar, name, content, key;
    public long time;
    public boolean read;
    public String fromID;

    public InfoGroup() {
    }

    public InfoGroup(String avatar, String name, String content, String key, long time, boolean read, String fromID) {
        this.avatar = avatar;
        this.name = name;
        this.content = content;
        this.key = key;
        this.time = time;
        this.read = read;
        this.fromID = fromID;
    }

    public String getFromID() {
        return fromID;
    }

    public void setFromID(String fromID) {
        this.fromID = fromID;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    @Override
    public String toString() {
        return "InfoGroup{" +
                "name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", key='" + key + '\'' +
                ", fromID='" + fromID + '\'' +
                '}';
    }
}
