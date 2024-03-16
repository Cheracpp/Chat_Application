package com.aymane.quickchat.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.aymane.quickchat.client.Authenticator;

import java.io.IOException;

public class Controller implements NavigationController {
    private final Stage primaryStage;
    private final Authenticator authenticator;
    // Other UI components references

    public Controller(Stage primaryStage, Authenticator authenticator) {
        this.primaryStage = primaryStage;
        this.authenticator = authenticator;
    }

    public void initialize() {
        showSigninView();
        this.primaryStage.setTitle("QuickChat");
        this.primaryStage.getIcons().add(new Image("/icons/AppIcon.png"));
        primaryStage.show();
    }

    public void showSigninView() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SigninView.fxml"));
            // Set the controller
            loader.setController(new SigninController(this, authenticator));
            Parent signinRoot = loader.load();

            // Set the scene
            primaryStage.setScene(new Scene(signinRoot));

            primaryStage.resizableProperty().set(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showSignupView() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignupView.fxml"));
            loader.setController(new SignupController(this, authenticator));
            Parent signupRoot = loader.load();

            // Set the scene
            primaryStage.setScene(new Scene(signupRoot));

            primaryStage.resizableProperty().set(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methods to switch to other scenes, like showChatScreen(), etc.


}
