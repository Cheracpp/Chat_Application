package com.aymane.quickchat.client;

import com.aymane.quickchat.utils.Serialization;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private String username;
    private PrintWriter writer;
    private BufferedReader reader;
    private Scanner scanner;


    public ChatClient(String hostname, int port) {

        try {
            this.socket = new Socket(hostname, port);
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.scanner = new Scanner(System.in);
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
            e.printStackTrace();
        }
    }

    public void start() {
            String serverMessage;
            String clientMessage;
            while (!Objects.equals(serverMessage = readMessage(), "success")) {
                System.out.println(serverMessage);
                if(!serverMessage.contains("Enter") && !serverMessage.contains("Confirm") && !serverMessage.contains("Reconfirm") ){

                }else{
                    clientMessage = scanner.nextLine();
                    if (Objects.equals(serverMessage, "Enter your username")) {
                        this.username = clientMessage;
                    }
                    writer.println(clientMessage);
                }
            }
        System.out.println("Connected to the chat server");
        new Thread(new MessageReceiver(this.socket)).start();

        while (true) {
            String content = scanner.nextLine();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
            sendMessage(username, content, timestamp);
        }
    }

    public void sendMessage(String sender, String content, String timestamp) {
        String message = Serialization.createMessageJson("message",sender,content,timestamp);
        writer.println(message);
    }
    public String readMessage() {
        String message = null;
        try {
            message = reader.readLine();
            if (message == null) { // Check for end of stream
                throw new IOException("Server closed the connection.");
            }
        } catch (IOException e) {
            handleReadError(e);
        }
        return message;
    }

    // Handle errors during reading
    private void handleReadError(IOException e) {
        System.err.println("Error reading from server: " + e.getMessage());
        // Implement additional error recovery or logging here
        // Consider reconnecting or notifying the user
    }



    private class MessageReceiver implements Runnable {
        private final Socket socket;

        public MessageReceiver(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String json;
                while ((json = reader.readLine()) != null) {
                Serialization.printMessageFromJson(json);
                }
            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

}
