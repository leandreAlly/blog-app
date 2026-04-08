package com.ally.blogapp.util;

import com.ally.blogapp.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class SceneManager {

    private static Stage primaryStage;
    private static User currentUser;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void switchScene(String fxmlFile, String title) {
        switchScene(fxmlFile, title, null);
    }

    public static <T> void switchScene(String fxmlFile, String title, Consumer<T> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/com/ally/blogapp/" + fxmlFile));
            Parent root = loader.load();

            if (controllerSetup != null) {
                T controller = loader.getController();
                controllerSetup.accept(controller);
            }

            Scene scene = new Scene(root);
            primaryStage.setTitle("BlogApp - " + title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxmlFile, e);
        }
    }

    public static <T> Parent loadFxml(String fxmlFile, Consumer<T> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/com/ally/blogapp/" + fxmlFile));
            Parent root = loader.load();

            if (controllerSetup != null) {
                T controller = loader.getController();
                controllerSetup.accept(controller);
            }

            return root;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxmlFile, e);
        }
    }
}
