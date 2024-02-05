package com.aymane.quickchat.utils;
import com.aymane.quickchat.model.Message;
import com.google.gson.Gson;


public class Serialization {

    private static final Gson gson = new Gson();

    // Serialize a Message object to JSON string
    public static String serializeMessageToJson(Message message) {
        return gson.toJson(message);
    }

    // Deserialize a JSON string to a Message object
    public static Message deserializeJsonToMessage(String json) {
        return gson.fromJson(json, Message.class);
    }

    // Example method to create a Message object from components
    public static String createMessageJson(String type, String sender, String content, String timestamp) {
        Message message = new Message(type, sender, content, timestamp);
        return serializeMessageToJson(message);
    }

    // Example method to print a Message object from JSON
    public static void printMessageFromJson(String json) {
        Message message = deserializeJsonToMessage(json);
        System.out.println(message);
    }
}