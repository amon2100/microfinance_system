package com.sacco.ui;

import com.sacco.app.SessionContext;
import com.sacco.model.Role;
import com.sacco.model.TransactionView;
import com.sacco.model.User;
import com.sacco.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class TransactionsController {
    @FXML
    private TableView<TransactionView> transactionsTable;
    @FXML
    private TableColumn<TransactionView, Number> idColumn;
    @FXML
    private TableColumn<TransactionView, String> memberColumn;
    @FXML
    private TableColumn<TransactionView, String> typeColumn;
    @FXML
    private TableColumn<TransactionView, String> amountColumn;
    @FXML
    private TableColumn<TransactionView, String> createdAtColumn;

    @FXML
    private TextField reversalTransactionIdField;

    @FXML
    private Label messageLabel;
    @FXML
    private Button reverseButton;

    private final TransactionService transactionService = new TransactionService();
    private final ObservableList<TransactionView> transactions = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()));
        memberColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMemberName()));
        typeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        amountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAmount().toPlainString()));
        createdAtColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCreatedAt()));
        transactionsTable.setItems(transactions);
        applyRoleRestrictions();
        refresh();
    }

    private void applyRoleRestrictions() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return;
        }
        if (user.getRole() != Role.DIRECTOR) {
            reverseButton.setDisable(true);
            reversalTransactionIdField.setDisable(true);
        }
    }

    private void refresh() {
        transactions.setAll(transactionService.listTransactions());
    }

    @FXML
    private void handleReverse() {
        String txIdText = reversalTransactionIdField.getText();
        if (txIdText == null || txIdText.isBlank()) {
            messageLabel.setText("Transaction ID is required.");
            return;
        }
        try {
            long txId = Long.parseLong(txIdText.trim());
            Long actorUserId = SessionContext.getCurrentUserId();
            long userId = actorUserId != null ? actorUserId : 0L;
            transactionService.reverseTransaction(userId, txId);
            messageLabel.setText("Transaction reversed.");
            refresh();
        } catch (Exception ex) {
            messageLabel.setText("Failed: " + ex.getMessage());
        }
    }
}
