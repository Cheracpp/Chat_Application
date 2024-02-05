package com.aymane.quickchat.server;

import com.aymane.quickchat.model.Message;
import com.aymane.quickchat.utils.Serialization;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler {
    private final Socket clientSocket;
    private final String ClientName;
    private final PrintWriter writer;
    private static final Map<String, PrintWriter> clients = new ConcurrentHashMap<>();


    public ClientHandler(Socket clientSocket, String ClientName) {
        this.clientSocket = clientSocket;
        this.ClientName = ClientName;
        try {
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
            clients.put(ClientName, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create PrintWriter: " + e.getMessage(), e);
        }
    }

    public void start() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String json;
            while ((json = reader.readLine()) != null) {
                Message message = Serialization.deserializeJsonToMessage(json);
                String type = message.getType();
                switch (type) {
                    case "connect":
                        handleConnectMessage(message);
                        break;
                    case "message":
                        handleMessage(message);
                        break;
                    case "disconnect":
                        handleDisconnectMessage(message);
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
            if (ClientName != null) {
                clients.remove(ClientName);
            }
            try {
                clientSocket.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleConnectMessage(Message message) {
        clients.put(ClientName, writer);
        System.out.println(ClientName + " has connected.");
    }


    public void handleMessage(Message message) {
        System.out.println(message);
        String json = Serialization.serializeMessageToJson(message);

        for (Map.Entry<String, PrintWriter> entry : clients.entrySet()) {
            if (!entry.getKey().equals(ClientName)) {
                entry.getValue().println(json);
            }
        }
    }

    public void handleDisconnectMessage(Message message) {
        String sender = message.getSender();
        clients.remove(sender);
        System.out.println(sender + " has disconnected.");
    }
}