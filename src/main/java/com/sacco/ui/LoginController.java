package com.sacco.ui;

import com.sacco.model.User;
import com.sacco.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private Consumer<User> onLoginSuccess;

    private final AuthService authService = new AuthService();

    public void setSceneManager(Consumer<User> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            messageLabel.setText("Enter username and password.");
            return;
        }
        User user = authService.authenticate(username.trim(), password);
        if (user == null) {
            messageLabel.setText("Invalid credentials.");
            return;
        }
        messageLabel.setText("");
        if (onLoginSuccess != null) {
            onLoginSuccess.accept(user);
        }
    }
}
