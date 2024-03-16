package com.aymane.quickchat.server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;

public class ChatServer {
    private final int port;
    SSLServerSocket serverSocket;

    public ChatServer(int port) {
        this.port = port;
    }



    public void start() {
        try {
            // Load the keystore from the classpath
            String keyStorePath = "/serverkeystore.p12";
            InputStream keyStoreStream = getClass().getResourceAsStream(keyStorePath);
            if (keyStoreStream == null) {
                throw new FileNotFoundException("Could not find the keystore file in the resources.");
            }

            // Store the keystore in a temporary file that can be loaded
            File tempKeyStore = File.createTempFile("serverkeystore", ".p12");
            try (FileOutputStream out = new FileOutputStream(tempKeyStore)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = keyStoreStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Set the keystore system properties
            System.setProperty("javax.net.ssl.keyStore", tempKeyStore.getAbsolutePath());
            String keyStorePassword = System.getenv("KEYSTORE_PASSWORD");
            if (keyStorePassword == null || keyStorePassword.isEmpty()) {
                keyStorePassword = "quickchat"; // You should prompt the user or load from a secure location
            }
            System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);

            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            this.serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
            System.out.println("Server is listening on port " + port);

            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                ClientAuthenticater clientAuthenticater = new ClientAuthenticater(clientSocket);
                new Thread(clientAuthenticater).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if(serverSocket != null){

                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}