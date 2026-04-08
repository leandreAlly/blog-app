package com.ally.blogapp.controller;

import com.ally.blogapp.model.Role;
import com.ally.blogapp.model.User;
import com.ally.blogapp.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private VBox sidebar;
    @FXML private Button createPostBtn;

    @FXML
    public void initialize() {
        User user = SceneManager.getCurrentUser();
        userLabel.setText(user.getUsername());
        roleLabel.setText(user.getRole().name());

        // Hide "Create Post" button for readers
        if (user.getRole() == Role.READER) {
            createPostBtn.setVisible(false);
            createPostBtn.setManaged(false);
        }

        // Load all posts by default
        showAllPosts();
    }

    @FXML
    public void showAllPosts() {
        Parent content = SceneManager.<PostListController>loadFxml("post-list-view.fxml", controller -> {
            controller.setDashboardController(this);
            controller.loadAllPosts();
        });
        rootPane.setCenter(content);
    }

    @FXML
    private void showMyPosts() {
        Parent content = SceneManager.<PostListController>loadFxml("post-list-view.fxml", controller -> {
            controller.setDashboardController(this);
            controller.loadMyPosts();
        });
        rootPane.setCenter(content);
    }

    @FXML
    private void showCreatePost() {
        Parent content = SceneManager.<PostEditorController>loadFxml("post-editor-view.fxml", controller -> {
            controller.setDashboardController(this);
            controller.setNewPost();
        });
        rootPane.setCenter(content);
    }

    public void showEditPost(Long postId) {
        Parent content = SceneManager.<PostEditorController>loadFxml("post-editor-view.fxml", controller -> {
            controller.setDashboardController(this);
            controller.loadPost(postId);
        });
        rootPane.setCenter(content);
    }

    public void showPostDetail(Long postId) {
        Parent content = SceneManager.<PostDetailController>loadFxml("post-detail-view.fxml", controller -> {
            controller.setDashboardController(this);
            controller.loadPost(postId);
        });
        rootPane.setCenter(content);
    }

    @FXML
    private void handleLogout() {
        SceneManager.setCurrentUser(null);
        SceneManager.switchScene("login-view.fxml", "Login");
    }
}
