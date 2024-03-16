package com.aymane.quickchat.model;

import java.sql.Timestamp;

public class Message {
    private String type;
    private String sender;
    private String content;
    private Timestamp timestamp;
    private int messageId;

    // Constructor, getters, and setters
    public Message(){};
    public Message(String type, String sender, String content, Timestamp timestamp) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
    public int setMessageId() {
        return this.messageId;
    }



    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }



    // toString method for printing
    @Override
    public String toString() {
        return "[" + this.timestamp.toString() + "] " + this.sender + ": " + this.content;
    }
}

