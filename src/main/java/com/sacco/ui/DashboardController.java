package com.sacco.ui;

import com.sacco.model.Role;
import com.sacco.model.User;
import com.sacco.model.Member;
import com.sacco.service.MemberService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import java.net.InetSocketAddress;
import java.net.Socket;

public class DashboardController {
    @FXML
    private Label userLabel;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;

    @FXML
    private Button membersNav;
    @FXML
    private Button savingsNav;
    @FXML
    private Button loansNav;
    @FXML
    private Button transactionsNav;
    @FXML
    private Button usersNav;
    @FXML
    private Button auditNav;

    @FXML
    private VBox membersView;
    @FXML
    private VBox savingsView;
    @FXML
    private VBox loansView;
    @FXML
    private VBox transactionsView;
    @FXML
    private VBox usersView;
    @FXML
    private VBox auditView;

    private final MemberService memberService = new MemberService();

    public void setUser(User user) {
        if (userLabel != null && user != null) {
            userLabel.setText("Logged in as: " + user.getUsername() + " (" + user.getRole().name() + ")");
        }
        applyRoleRestrictions(user);
        refreshOnlineStatus();
    }

    @FXML
    private void showMembers() {
        showView(membersView);
    }

    @FXML
    private void showSavings() {
        showView(savingsView);
    }

    @FXML
    private void showLoans() {
        showView(loansView);
    }

    @FXML
    private void showTransactions() {
        showView(transactionsView);
    }

    @FXML
    private void showUsers() {
        showView(usersView);
    }

    @FXML
    private void showAudit() {
        showView(auditView);
    }

    private void showView(VBox active) {
        VBox[] views = new VBox[]{membersView, savingsView, loansView, transactionsView, usersView, auditView};
        for (VBox view : views) {
            boolean isActive = view == active;
            view.setVisible(isActive);
            view.setManaged(isActive);
        }
    }

    @FXML
    private void handleSearch() {
        String term = searchField.getText();
        if (term == null || term.isBlank()) {
            showAlert("Search", "Enter a name or ID to search.");
            return;
        }
        try {
            var results = memberService.searchMembers(term.trim());
            if (results.isEmpty()) {
                showAlert("Not found", "No member found for: " + term);
                return;
            }
            Member member = results.get(0);
            String details = "ID: " + member.getId() + "\n" +
                    "Name: " + member.getFullName() + "\n" +
                    "National ID: " + safe(member.getNationalId()) + "\n" +
                    "Phone: " + safe(member.getPhone()) + "\n" +
                    "Email: " + safe(member.getEmail());
            showAlert("Member Found", details);
            showMembers();
        } catch (Exception ex) {
            showAlert("Search Error", ex.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void applyRoleRestrictions(User user) {
        if (user == null) {
            return;
        }
        Role role = user.getRole();
        if (role == Role.TELLER) {
            loansNav.setDisable(true);
            transactionsNav.setDisable(true);
            usersNav.setDisable(true);
            auditNav.setDisable(true);
            showMembers();
        } else if (role == Role.MANAGER) {
            savingsNav.setDisable(true);
            transactionsNav.setDisable(true);
            usersNav.setDisable(true);
            showLoans();
        } else if (role == Role.DIRECTOR) {
            showMembers();
        }
    }

    private void refreshOnlineStatus() {
        new Thread(() -> {
            boolean online = isOnline();
            Platform.runLater(() -> {
                statusLabel.setText(online ? "Online" : "Offline");
                statusLabel.getStyleClass().removeAll("status-online", "status-offline");
                statusLabel.getStyleClass().add(online ? "status-online" : "status-offline");
            });
        }).start();
    }

    private boolean isOnline() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("1.1.1.1", 53), 1500);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
