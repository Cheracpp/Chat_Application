package com.aymane.quickchat.controllers;

import com.aymane.quickchat.client.Authenticator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;


public class SignupController {
    private final NavigationController controller;
    private final Authenticator authenticator;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label cancelButton;


    public SignupController(NavigationController controller, Authenticator authenticator) {
        this.controller = controller;
        this.authenticator = authenticator;
    }

    @FXML
    private void handleSignupAction(ActionEvent event) {
        if (passwordField.getText().equals(confirmPasswordField.getText())) {
            // The passwords match, proceed with the signup process
            authenticator.startAuthentication(usernameField.getText(), passwordField.getText(), "signup");
            // Handle the signup logic with the parameters
            // On success: controller.switchToChatView();
            // On failure: display an error message
        } else {
            // The passwords do not match, display an error message to the user
            showAlert("Password Mismatch", "The passwords do not match. Please try again.", Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void switchToSigninView(MouseEvent event) {
        controller.showSigninView();
    }


    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
