package com.aymane.quickchat.controllers;

import com.aymane.quickchat.client.Authenticator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import javax.swing.text.html.ImageView;


public class SigninController {

    private final NavigationController controller;
    private final Authenticator authenticator;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label signupButton;



    public SigninController(NavigationController controller, Authenticator authenticator) {
        this.controller = controller;
        this.authenticator = authenticator;
    }

    @FXML
    private void signinAction(ActionEvent event) {
        authenticator.startAuthentication(usernameField.getText(), passwordField.getText(), "signin");
/*        boolean loginSuccess = true;
        if (loginSuccess) {
            try {
                // Load the conversations view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConversationsView.fxml"));
                Parent conversationsRoot = loader.load();

                // Get the current stage from the event source
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                // Set the new scene
                stage.setScene(new Scene(conversationsRoot));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // handle login failure
            showAlert("Wrong Credentials", "Invalid username or password. Please try again.", Alert.AlertType.ERROR);
        }*/
    }
    @FXML
    private void switchToSignupView(MouseEvent event) {
        controller.showSignupView();
    }

}
