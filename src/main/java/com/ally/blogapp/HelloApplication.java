package com.ally.blogapp;

import com.ally.blogapp.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        SceneManager.switchScene("login-view.fxml", "Login");
    }
}
