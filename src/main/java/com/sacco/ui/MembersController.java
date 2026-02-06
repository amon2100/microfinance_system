package com.sacco.ui;

import com.sacco.app.SessionContext;
import com.sacco.model.Member;
import com.sacco.service.MemberService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MembersController {
    @FXML
    private TableView<Member> membersTable;
    @FXML
    private TableColumn<Member, Number> idColumn;
    @FXML
    private TableColumn<Member, String> nameColumn;
    @FXML
    private TableColumn<Member, String> nationalIdColumn;
    @FXML
    private TableColumn<Member, String> phoneColumn;
    @FXML
    private TableColumn<Member, String> emailColumn;

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField nationalIdField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private Label messageLabel;

    private final MemberService memberService = new MemberService();
    private final ObservableList<Member> members = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()));
        nameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));
        nationalIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNationalId()));
        phoneColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPhone()));
        emailColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        membersTable.setItems(members);
        refresh();
    }

    @FXML
    private void handleAddMember() {
        String fullName = fullNameField.getText();
        if (fullName == null || fullName.isBlank()) {
            messageLabel.setText("Full name is required.");
            return;
        }
        String nationalId = nationalIdField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        try {
            Long actorUserId = SessionContext.getCurrentUserId();
            long userId = actorUserId != null ? actorUserId : 0L;
            memberService.createMember(userId, fullName.trim(), safe(nationalId), safe(phone), safe(email));
            clearForm();
            messageLabel.setText("Member added.");
            refresh();
        } catch (Exception ex) {
            messageLabel.setText("Failed: " + ex.getMessage());
        }
    }

    private void refresh() {
        members.setAll(memberService.listMembers());
    }

    private void clearForm() {
        fullNameField.clear();
        nationalIdField.clear();
        phoneField.clear();
        emailField.clear();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
