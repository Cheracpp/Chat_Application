package com.aymane.quickchat.model;

import java.sql.Timestamp;

public class FileMessage extends Message {
    private String filePath;
    private long fileSize;

    // Constructor, getters, and setters
    public FileMessage(String type, String sender, String content, Timestamp timestamp, String filePath, long fileSize) {
        super(type, sender, content, timestamp);
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    // toString method for printing
    @Override
    public String toString() {
        // Include file-specific details in the output
        return super.toString() + " [File path: " + this.filePath + ", File size: " + this.fileSize + "]";
    }
}
