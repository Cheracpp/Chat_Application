package com.aymane.quickchat.model;

import java.sql.Timestamp;

public class ConversationSummary {
    private final int conversation_id;
    private final String title;
    private final Timestamp timestamp;
    public ConversationSummary(int conversation_id, String title, Timestamp timestamp){
        this.conversation_id = conversation_id;
        this.title = title;
        this.timestamp = timestamp;
    }
    public int getConversationId(){return this.conversation_id;}
    public String getTitle(){
        return this.title;
    }
    public Timestamp getTimestamp(){
        return this.timestamp;
    }
}
