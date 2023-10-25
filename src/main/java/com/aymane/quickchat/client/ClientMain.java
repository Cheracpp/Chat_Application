package com.aymane.quickchat.client;

public class ClientMain {
    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 12345);
        client.start();
    }
}
