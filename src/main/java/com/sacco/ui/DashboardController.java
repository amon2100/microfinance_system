package com.sacco.ui;

import com.sacco.model.Role;
import com.sacco.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;

public class DashboardController {
    @FXML
    private Label userLabel;

    @FXML
    private Tab membersTab;
    @FXML
    private Tab savingsTab;
    @FXML
    private Tab loansTab;
    @FXML
    private Tab transactionsTab;
    @FXML
    private Tab auditTab;

    public void setUser(User user) {
        if (userLabel != null && user != null) {
            userLabel.setText("Logged in as: " + user.getUsername() + " (" + user.getRole().name() + ")");
        }
        applyRoleRestrictions(user);
    }

    private void applyRoleRestrictions(User user) {
        if (user == null) {
            return;
        }
        Role role = user.getRole();
        if (role == Role.TELLER) {
            loansTab.setDisable(true);
            transactionsTab.setDisable(true);
            auditTab.setDisable(true);
        } else if (role == Role.MANAGER) {
            savingsTab.setDisable(true);
            transactionsTab.setDisable(true);
        } else if (role == Role.ADMIN) {
            savingsTab.setDisable(true);
            loansTab.setDisable(true);
        }
    }
}
