package com.aymane.quickchat.model;

public class Message {
    private String type;
    private String sender;
    private String content;
    private String timestamp;

    // Constructor, getters, and setters
    public Message(String type, String sender, String content, String timestamp) {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // toString method for printing
    @Override
    public String toString() {
        return "[" + this.timestamp + "] " + this.sender + ": " + this.content;
    }
}

