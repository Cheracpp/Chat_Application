package com.aymane.quickchat.server;

public class ServerMain {
    public static void main(String[] args) {
        ChatServer server = new ChatServer(12345);
        server.start();
    }
}
