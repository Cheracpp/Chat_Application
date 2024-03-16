package com.aymane.quickchat.client;

import com.aymane.quickchat.utils.Serialization;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Scanner;


public class ChatClient implements Authenticator {
    private SSLSocket sslSocket;
    private String username;
    private PrintWriter writer;
    private BufferedReader reader;
    private Scanner scanner;


    public ChatClient(String hostname, int port) {

        try {
            String trustStorePath = "/clienttruststore.p12";
            InputStream trustStoreStream = getClass().getResourceAsStream(trustStorePath);
            if (trustStoreStream == null) {
                throw new FileNotFoundException("Could not find the truststore file in the resources.");
            }

            File tempTrustStore = File.createTempFile("clienttruststore", ".p12");
            try (FileOutputStream out = new FileOutputStream(tempTrustStore)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = trustStoreStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            System.setProperty("javax.net.ssl.trustStore", tempTrustStore.getAbsolutePath());
            String trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD");
            if (trustStorePassword == null || trustStorePassword.isEmpty()) {
                trustStorePassword = "quickchat"; // You should prompt the user or load from a secure location
            }
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            this.sslSocket = (SSLSocket) factory.createSocket(hostname, port);
            this.writer = new PrintWriter(sslSocket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            this.scanner = new Scanner(System.in);
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
            e.printStackTrace();
        }
    }
    @Override
    public void startAuthentication(String username, String password, String authType){
        this.username = username;
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        String message = Serialization.createMessageJson(authType,username,password,timestamp);
        writer.println(message);
    }

    public void sendMessage(String sender, String content, Timestamp timestamp) {
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
