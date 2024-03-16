package com.aymane.quickchat.server;

public class ServerMain {
    public static void main(String[] args) {
        ChatServer server = new ChatServer(443);
        server.start();
    }
}
