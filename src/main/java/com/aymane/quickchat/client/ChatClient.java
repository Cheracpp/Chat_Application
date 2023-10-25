package com.aymane.quickchat.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ChatClient {
    private String hostname;
    private int port;
    private Socket socket;
    private PrintWriter writer;
    private MessageListener messageListener;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.messageListener = new MessageListener();
        try {
            this.socket = new Socket(hostname, port);
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to the chat server");
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
            e.printStackTrace();
        }
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            while (true) {
                System.out.print("Enter your message: ");
                String content = scanner.nextLine();
                String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
                sendMessage(username, content, timestamp);
            }
        }
    }

    public void sendMessage(String sender, String content, String timestamp) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "message");
        message.addProperty("sender", sender);
        message.addProperty("content", content);
        message.addProperty("timestamp", timestamp);

        writer.println(message.toString());
    }


    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = reader.readLine()) != null) {
                    try {
                        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
                        messageListener.handleMessage(jsonObject);
                    } catch (JsonSyntaxException e) {
                        System.out.println("Received a message that is not in valid JSON format: " + message);
                    }
                }
            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}

class MessageListener {
    public void handleMessage(JsonObject jsonObject) {
        String sender = jsonObject.get("sender").getAsString();
        String content = jsonObject.get("content").getAsString();
        String timestamp = jsonObject.get("timestamp").getAsString();

        System.out.println("[" + timestamp + "] " + sender + ": " + content);
    }
}
