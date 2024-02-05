package com.aymane.quickchat.server;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ClientAuthenticater implements Runnable {

    BufferedReader reader;
    PrintWriter writer;
    String clientName;
    Socket clientsocket;
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/quickchat";
    private static final String DATABASE_USER = "quickchatUser";
    private static final String DATABASE_PASSWORD = "quickchat";

    public ClientAuthenticater(Socket clientSocket) throws IOException {
        this.clientsocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        boolean authentication = true;
        while (authentication) {
            try {
                writer.println("Enter: <1> to sign up <2> to log in ");
                String answer = reader.readLine();
                if (Objects.equals(answer, "1")) {
                    // sign in function
                    attemptingToSignUp();
                } else if (Objects.equals(answer, "2")) {
                    //sign in function
                    attemptingToSignIn();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void attemptingToSignUp() throws IOException {
        boolean startSignUp = true;
        int numberOfAttempts = 0;
        while(startSignUp){
            startSignUp = signUp();
            ++numberOfAttempts;
            if(numberOfAttempts == 5){break;}
        }
        if(startSignUp){
            writer.println("You failed to register. This connection will be closed.");
            clientsocket.close();
        }else{
            writer.println("The registration was successful");
            attemptingToSignIn();
        }
    }


    public void attemptingToSignIn() throws IOException {
        boolean startSignIn = true;
        int numberOfAttempts = 0;
        while(startSignIn){
            startSignIn = signIn();
            ++numberOfAttempts;
            if(numberOfAttempts == 5){break;}
        }
        writer.println("success");
        System.out.println("New client connected with Username: " + clientName);
        ClientHandler clientHandler = new ClientHandler(clientsocket, clientName);
        clientHandler.start();
    }



    public boolean signUp() throws IOException {
        writer.println("Enter your username");
        String username = reader.readLine();
        if(!isUsernameUnique(username)){
            writer.println("This username is already taken. Try again!!");
            return true;
        }
        writer.println("Enter your password");
        String password = reader.readLine();
        writer.println("Confirm your password");
        while(true){
            if(Objects.equals(password,reader.readLine())){
                break;
            }else{
                writer.println("Password confirmation failed. Reconfirm your password!");
            }
        }
        addUser(username,password);
        this.clientName = username;
        return false;
    }

    public boolean signIn() throws IOException {
        writer.println("Enter your username");
        String username = reader.readLine();
        if(isUsernameUnique(username)){
            writer.println("username unavailable");
            return true;
        }
        this.clientName = username;
        writer.println("Enter your password");
        String password = reader.readLine();
        if(isPasswordCorrect(username, password)){
            return false;
        }else{
            writer.println("password incorrect");
            return true;
        }
    }


    //helper functions:
    private static boolean isUsernameUnique(String username) {
        // SQL query to check if a username exists
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    // If count is 0, the username does not exist and is unique
                    return resultSet.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // In a real application, consider a more robust error handling
        }

        // Default to false (not unique) if there's an error
        return false;
    }

    public static void addUser(String username, String plainPassword) {
        // Hash the password
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        // SQL INSERT statement
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameters for the prepared statement
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);

            // Execute the SQL statement
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User added successfully.");
            } else {
                System.out.println("User could not be added.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // In a real application, consider a more robust error handling
        }
    }

    public static boolean isPasswordCorrect(String username, String plainPassword) {
        String sql = "SELECT password FROM users WHERE username = ?";
        String storedHashedPassword = null;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    storedHashedPassword = resultSet.getString("password");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Proper error handling should be done here
        }
        if (storedHashedPassword != null && BCrypt.checkpw(plainPassword, storedHashedPassword)) {
            return true;
        } else {
            return false;
        }
    }


}

