package com.ally.blogapp.controller;

import com.ally.blogapp.model.Role;
import com.ally.blogapp.model.User;
import com.ally.blogapp.service.UserService;
import com.ally.blogapp.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private VBox loginPane;
    @FXML private VBox registerPane;

    // Login fields
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginError;

    // Register fields
    @FXML private TextField regUsername;
    @FXML private TextField regEmail;
    @FXML private PasswordField regPassword;
    @FXML private PasswordField regConfirmPassword;
    @FXML private ComboBox<String> regRole;
    @FXML private Label regError;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        regRole.getItems().addAll("BLOGGER", "READER");
        regRole.setValue("READER");
        showLogin();
    }

    @FXML
    private void handleLogin() {
        loginError.setText("");
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginError.setText("Please fill in all fields.");
            return;
        }

        var userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            loginError.setText("User not found.");
            return;
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            loginError.setText("Incorrect password.");
            return;
        }

        SceneManager.setCurrentUser(user);
        navigateByRole(user);
    }

    @FXML
    private void handleRegister() {
        regError.setText("");
        String username = regUsername.getText().trim();
        String email = regEmail.getText().trim();
        String password = regPassword.getText();
        String confirm = regConfirmPassword.getText();
        String role = regRole.getValue();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            regError.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirm)) {
            regError.setText("Passwords do not match.");
            return;
        }

        if (password.length() < 4) {
            regError.setText("Password must be at least 4 characters.");
            return;
        }

        try {
            User user = new User(username, email, password, Role.valueOf(role));
            User saved = userService.register(user);
            SceneManager.setCurrentUser(saved);
            navigateByRole(saved);
        } catch (IllegalArgumentException e) {
            regError.setText(e.getMessage());
        }
    }

    private void navigateByRole(User user) {
        if (user.getRole() == Role.BLOGGER) {
            SceneManager.switchScene("dashboard-view.fxml", "Dashboard");
        } else {
            SceneManager.switchScene("reader-view.fxml", "Blog");
        }
    }

    @FXML
    private void showLogin() {
        loginPane.setVisible(true);
        loginPane.setManaged(true);
        registerPane.setVisible(false);
        registerPane.setManaged(false);
    }

    @FXML
    private void showRegister() {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(true);
        registerPane.setManaged(true);
    }
}
