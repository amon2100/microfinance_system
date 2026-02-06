package com.sacco.ui;

import com.sacco.app.SessionContext;
import com.sacco.model.Loan;
import com.sacco.model.Role;
import com.sacco.model.User;
import com.sacco.service.LoanService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;

public class LoansController {
    @FXML
    private TableView<Loan> loansTable;
    @FXML
    private TableColumn<Loan, Number> idColumn;
    @FXML
    private TableColumn<Loan, String> memberColumn;
    @FXML
    private TableColumn<Loan, Number> memberIdColumn;
    @FXML
    private TableColumn<Loan, String> principalColumn;
    @FXML
    private TableColumn<Loan, String> rateColumn;
    @FXML
    private TableColumn<Loan, Number> termColumn;
    @FXML
    private TableColumn<Loan, String> outstandingColumn;
    @FXML
    private TableColumn<Loan, String> statusColumn;
    @FXML
    private TableColumn<Loan, String> issuedAtColumn;

    @FXML
    private TextField memberIdField;
    @FXML
    private TextField principalField;
    @FXML
    private TextField rateField;
    @FXML
    private TextField termField;
    @FXML
    private TextField loanIdField;
    @FXML
    private TextField repaymentMemberIdField;
    @FXML
    private TextField repaymentAmountField;
    @FXML
    private TextField approveLoanIdField;
    @FXML
    private TextField disburseLoanIdField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button issueButton;
    @FXML
    private Button approveButton;
    @FXML
    private Button disburseButton;
    @FXML
    private Button repaymentButton;

    private final LoanService loanService = new LoanService();
    private final ObservableList<Loan> loans = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()));
        memberColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMemberName()));
        memberIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getMemberId()));
        principalColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPrincipal().toPlainString()));
        rateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getInterestRate().toPlainString()));
        termColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getTermMonths()));
        outstandingColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOutstandingBalance().toPlainString()));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        issuedAtColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getIssuedAt()));
        loansTable.setItems(loans);
        applyRoleRestrictions();
        refresh();
    }

    private void applyRoleRestrictions() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return;
        }
        Role role = user.getRole();
        if (role == Role.TELLER) {
            issueButton.setDisable(true);
            approveButton.setDisable(true);
            disburseButton.setDisable(true);
        } else if (role == Role.MANAGER) {
            repaymentButton.setDisable(true);
        } else if (role == Role.ADMIN) {
            issueButton.setDisable(true);
            approveButton.setDisable(true);
            disburseButton.setDisable(true);
            repaymentButton.setDisable(true);
        }
    }

    @FXML
    private void handleIssueLoan() {
        String memberIdText = memberIdField.getText();
        String principalText = principalField.getText();
        String rateText = rateField.getText();
        String termText = termField.getText();
        if (memberIdText == null || memberIdText.isBlank() || principalText == null || principalText.isBlank()) {
            messageLabel.setText("Member ID and principal are required.");
            return;
        }
        try {
            long memberId = Long.parseLong(memberIdText.trim());
            BigDecimal principal = new BigDecimal(principalText.trim());
            BigDecimal rate = rateText == null || rateText.isBlank() ? BigDecimal.ZERO : new BigDecimal(rateText.trim());
            int termMonths = termText == null || termText.isBlank() ? 12 : Integer.parseInt(termText.trim());
            long userId = getActorId();
            loanService.issueLoan(userId, memberId, principal, rate, termMonths);
            messageLabel.setText("Loan issued.");
            refresh();
        } catch (Exception ex) {
            messageLabel.setText("Failed: " + ex.getMessage());
        }
    }

    @FXML
    private void handleRepayment() {
        String loanIdText = loanIdField.getText();
        String memberIdText = repaymentMemberIdField.getText();
        String amountText = repaymentAmountField.getText();
        if (loanIdText == null || loanIdText.isBlank() || memberIdText == null || memberIdText.isBlank()
                || amountText == null || amountText.isBlank()) {
            messageLabel.setText("Loan ID, Member ID and amount are required.");
            return;
        }
        try {
            long loanId = Long.parseLong(loanIdText.trim());
            long memberId = Long.parseLong(memberIdText.trim());
            BigDecimal amount = new BigDecimal(amountText.trim());
            long userId = getActorId();
            loanService.recordRepayment(userId, loanId, memberId, amount);
            messageLabel.setText("Repayment posted.");
            refresh();
        } catch (Exception ex) {
            messageLabel.setText("Failed: " + ex.getMessage());
        }
    }

    @FXML
    private void handleApproveLoan() {
        String loanIdText = approveLoanIdField.getText();
        if (loanIdText == null || loanIdText.isBlank()) {
            messageLabel.setText("Loan ID is required for approval.");
            return;
        }
        try {
            long loanId = Long.parseLong(loanIdText.trim());
            long userId = getActorId();
            loanService.approveLoan(userId, loanId);
            messageLabel.setText("Loan approved.");
            refresh();
        } catch (Exception ex) {
            messageLabel.setText("Failed: " + ex.getMessage());
        }
    }

    @FXML
    private void handleDisburseLoan() {
        String loanIdText = disburseLoanIdField.getText();
        if (loanIdText == null || loanIdText.isBlank()) {
            messageLabel.setText("Loan ID is required for disbursement.");
            return;
        }
        try {
            long loanId = Long.parseLong(loanIdText.trim());
            long userId = getActorId();
            loanService.disburseLoan(userId, loanId);
            messageLabel.setText("Loan disbursed.");
            refresh();
        } catch (Exception ex) {
            messageLabel.setText("Failed: " + ex.getMessage());
        }
    }

    private void refresh() {
        loans.setAll(loanService.listLoans());
    }

    private long getActorId() {
        Long actorUserId = SessionContext.getCurrentUserId();
        return actorUserId != null ? actorUserId : 0L;
    }
}
