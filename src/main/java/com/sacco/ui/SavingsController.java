package com.sacco.ui;

import com.sacco.app.SessionContext;
import com.sacco.model.Role;
import com.sacco.model.User;
import com.sacco.model.SavingsAccount;
import com.sacco.service.SavingsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;

public class SavingsController {
    @FXML
    private TableView<SavingsAccount> accountsTable;
    @FXML
    private TableColumn<SavingsAccount, String> accountNoColumn;
    @FXML
    private TableColumn<SavingsAccount, String> memberColumn;
    @FXML
    private TableColumn<SavingsAccount, Number> memberIdColumn;
    @FXML
    private TableColumn<SavingsAccount, String> balanceColumn;

    @FXML
    private TextField memberIdField;
    @FXML
    private TextField accountNoField;
    @FXML
    private TextField amountField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button createAccountButton;
    @FXML
    private Button depositButton;
    @FXML
    private Button withdrawButton;

    private final SavingsService savingsService = new SavingsService();
    private final ObservableList<SavingsAccount> accounts = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        accountNoColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAccountNo()));
        memberColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMemberName()));
        memberIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getMemberId()));
        balanceColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getBalance().toPlainString()));
        accountsTable.setItems(accounts);
        applyRoleRestrictions();
        refresh();
    }

    private void applyRoleRestrictions() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return;
        }
        if (user.getRole() != Role.TELLER) {
            createAccountButton.setDisable(true);
            depositButton.setDisable(true);
            withdrawButton.setDisable(true);
        }
    }

    @FXML
    private void handleCreateAccount() {
        String memberIdText = memberIdField.getText();
        String accountNo = accountNoField.getText();
        if (memberIdText == null || memberIdText.isBlank() || accountNo == null || accountNo.isBlank()) {
            messageLabel.setText("Member ID and Account No are required.");
            return;
        }
        try {
            long memberId = Long.parseLong(memberIdText.trim());
            Long actorUserId = SessionContext.getCurrentUserId();
            long userId = actorUserId != null ? actorUserId : 0L;
            savingsService.createAccount(userId, memberId, accountNo.trim());
            messageLabel.setText("Account created.");
            refresh();
        } catch (Exception ex) {
            messageLabel.setText("Failed: " + ex.getMessage());
        }
    }

    @FXML
    private void handleDeposit() {
        runAmountAction(true);
    }

    @FXML
    private void handleWithdraw() {
        runAmountAction(false);
    }

    private void runAmountAction(boolean deposit) {
        String accountNo = accountNoField.getText();
        String amountText = amountField.getText();
        if (accountNo == null || accountNo.isBlank() || amountText == null || amountText.isBlank()) {
            messageLabel.setText("Account No and amount are required.");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountText.trim());
            Long actorUserId = SessionContext.getCurrentUserId();
            long userId = actorUserId != null ? actorUserId : 0L;
            if (deposit) {
                savingsService.deposit(userId, accountNo.trim(), amount);
                messageLabel.setText("Deposit posted.");
            } else {
                savingsService.withdraw(userId, accountNo.trim(), amount);
                messageLabel.setText("Withdrawal posted.");
            }
            refresh();
        } catch (Exception ex) {
            messageLabel.setText("Failed: " + ex.getMessage());
        }
    }

    private void refresh() {
        accounts.setAll(savingsService.listAccounts());
    }
}
