package com.gathering.friends.models;

import java.io.Serializable;
import java.util.Map;

public class ChatMessage implements Serializable {

    String reply_id;
    String sender_username;
    String message;
    Map timeStampMap;

    boolean isDeleted = false;

    public ChatMessage(String reply_id, String sender_username, String message) {
        this.reply_id = reply_id;
        this.sender_username = sender_username;
        this.message = message;
    }

    public ChatMessage(String reply_id, String sender_username, String message, Map timeStampMap) {
        this.reply_id = reply_id;
        this.sender_username = sender_username;
        this.message = message;
        this.timeStampMap = timeStampMap;
    }


    public Map getTimeStampMap() {
        return timeStampMap;
    }


    public void setTimeStampMap(Map timeStampMap) {
        this.timeStampMap = timeStampMap;
    }


    public String getReply_id() {
        return reply_id;
    }

    public void setReply_id(String reply_id) {
        this.reply_id = reply_id;
    }

    public String getSender_username() {
        return sender_username;
    }

    public void setSender_username(String sender_username) {
        this.sender_username = sender_username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
