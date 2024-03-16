package com.aymane.quickchat.server;
import com.aymane.quickchat.dao.DatabaseManager;
import com.aymane.quickchat.model.Message;
import com.aymane.quickchat.utils.Serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;

public class ClientAuthenticater implements Runnable {

    BufferedReader reader;
    PrintWriter writer;
    String clientName;
    Socket clientsocket;

    public ClientAuthenticater(Socket clientSocket) throws IOException {
        this.clientsocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()))) {
            String json;
            while ((json = reader.readLine()) != null) {
                Message message = Serialization.deserializeJsonToMessage(json);
                String type = message.getType();
                switch (type) {
                    case "signin":
                        boolean test = signIn(message);
                        if(!test){System.out.println("passed");}else{System.out.println("failed");}
                        break;
                    case "signup":
                        test = signUp(message);
                        if(!test){System.out.println("passed");}else{System.out.println("failed");}
                        break;
                    case "disconnect":
                        handleDisconnectMessage(message.getContent());
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
        } catch (SQLException e){

        }
    }

    public boolean signUp(Message message) throws IOException, SQLException {
        String username = message.getSender();
        String password = message.getContent();

        if(!DatabaseManager.isUsernameUnique(username)){
            writer.println("This username is already taken. Try again!!");
            return true;
        }
        DatabaseManager.addUser(username,password);
        this.clientName = username;
        return false;
    }

    public boolean signIn(Message message) throws IOException, SQLException {
        String username = message.getSender();
        String password = message.getContent();
        if(DatabaseManager.isUsernameUnique(username)){
            writer.println("username unavailable");
            return true;
        }
        this.clientName = username;
        if(DatabaseManager.isPasswordCorrect(username, password)){
            return false;
        }else{
            writer.println("password incorrect");
            return true;
        }
    }

    public void handleDisconnectMessage(String message){}



}

