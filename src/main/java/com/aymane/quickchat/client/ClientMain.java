package com.aymane.quickchat.client;

import javafx.application.Application;
import javafx.stage.Stage;
import com.aymane.quickchat.controllers.Controller;

public class ClientMain extends Application {
    @Override
    public void start(Stage primaryStage) {
        ChatClient client = new ChatClient("localhost", 443);
        Controller controller = new Controller(primaryStage, client);
        controller.initialize();
    }
    public static void main(String[] args) {
        launch(args);
    }


}
