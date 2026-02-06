package com.sacco.ui;

import com.sacco.app.SessionContext;
import com.sacco.model.Role;
import com.sacco.model.UserView;
import com.sacco.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UsersController {
    @FXML
    private TableView<UserView> usersTable;
    @FXML
    private TableColumn<UserView, Number> idColumn;
    @FXML
    private TableColumn<UserView, String> usernameColumn;
    @FXML
    private TableColumn<UserView, String> roleColumn;
    @FXML
    private TableColumn<UserView, String> activeColumn;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<Role> roleCombo;
    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();
    private final ObservableList<UserView> users = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()));
        usernameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        roleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRole().name()));
        activeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isActive() ? "Yes" : "No"));
        roleCombo.setItems(FXCollections.observableArrayList(Role.TELLER, Role.MANAGER, Role.DIRECTOR));
        roleCombo.setValue(Role.TELLER);
        usersTable.setItems(users);
        refresh();
    }

    @FXML
    private void handleCreateUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        Role role = roleCombo.getValue();
        if (username == null || username.isBlank() || password == null || password.isBlank() || role == null) {
            messageLabel.setText("Username, password, and role are required.");
            return;
        }
        try {
            Long actorUserId = SessionContext.getCurrentUserId();
            long userId = actorUserId != null ? actorUserId : 0L;
            userService.createUser(userId, username.trim(), password, role);
            messageLabel.setText("User created.");
            usernameField.clear();
            passwordField.clear();
            roleCombo.setValue(Role.TELLER);
            refresh();
        } catch (Exception ex) {
            messageLabel.setText("Failed: " + ex.getMessage());
        }
    }

    private void refresh() {
        Long actorUserId = SessionContext.getCurrentUserId();
        long userId = actorUserId != null ? actorUserId : 0L;
        users.setAll(userService.listUsers(userId));
    }
}
