package com.aymane.quickchat.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatServer chatServer;
    private String clientId;
    private final PrintWriter writer;
    private static final Map<String, PrintWriter> clients = new ConcurrentHashMap<>();


    public ClientHandler(Socket clientSocket, ChatServer chatServer) {
        this.clientSocket = clientSocket;
        this.chatServer = chatServer;
        try {
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create PrintWriter: " + e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = reader.readLine()) != null) {
                JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
                String type = jsonObject.get("type").getAsString();

                switch (type) {
                    case "connect":
                        handleConnectMessage(jsonObject);
                        break;
                    case "message":
                        handleMessage(jsonObject);
                        break;
                    case "disconnect":
                        handleDisconnectMessage(jsonObject);
                        break;
                    default:
                        System.out.println("Unknown message type: " + type);
                }
            }
        } catch (SocketException e) {
            System.out.println("A client has disconnected abruptly.");
        } catch (IOException e) {
            System.out.println("Error reading from client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (clientId != null) {
                clients.remove(clientId);
            }
            try {
                clientSocket.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleConnectMessage(JsonObject jsonObject) {
        this.clientId = jsonObject.get("sender").getAsString();
        clients.put(clientId, writer);
        System.out.println(clientId + " has connected.");
    }


    public void handleMessage(JsonObject jsonObject) {
        String sender = jsonObject.get("sender").getAsString();
        String content = jsonObject.get("content").getAsString();
        String timestamp = jsonObject.get("timestamp").getAsString();

        System.out.println("[" + timestamp + "] " + sender + ": " + content);

        for (Map.Entry<String, PrintWriter> entry : clients.entrySet()) {
            if (!entry.getKey().equals(sender)) {
                entry.getValue().println("[" + timestamp + "] " + sender + ": " + content);
            }
        }
    }

    public void handleDisconnectMessage(JsonObject jsonObject) {
        String sender = jsonObject.get("sender").getAsString();
        clients.remove(sender);
        System.out.println(sender + " has disconnected.");
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

}
