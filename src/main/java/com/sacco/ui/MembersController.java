package com.sacco.ui;

import com.sacco.app.SessionContext;
import com.sacco.model.Member;
import com.sacco.service.MemberService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

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
    @FXML
    private ImageView photoPreview;
    @FXML
    private ListView<Member> photoList;

    private String selectedPhotoPath;

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
        photoList.setItems(members);
        photoList.setCellFactory(list -> new ListCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(48);
                imageView.setFitHeight(48);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Member item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getFullName());
                    if (item.getPhotoPath() != null && !item.getPhotoPath().isBlank()) {
                        imageView.setImage(new Image("file:" + item.getPhotoPath(), 48, 48, true, true));
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        refresh();
    }

    @FXML
    private void handleSelectPhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Member Photo");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        var file = chooser.showOpenDialog(photoPreview.getScene().getWindow());
        if (file != null) {
            selectedPhotoPath = file.getAbsolutePath();
            photoPreview.setImage(new Image("file:" + selectedPhotoPath, 120, 120, true, true));
        }
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
            memberService.createMember(userId, fullName.trim(), safe(nationalId), safe(phone), safe(email), selectedPhotoPath);
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
        selectedPhotoPath = null;
        photoPreview.setImage(null);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
