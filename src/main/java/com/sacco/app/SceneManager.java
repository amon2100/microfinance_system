package com.sacco.app;

import com.sacco.model.User;
import com.sacco.ui.DashboardController;
import com.sacco.ui.LoginController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneManager {
    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("SACCO Microfinance System");
    }

    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/sacco/ui/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setSceneManager(SceneManager::showDashboard);
            Scene scene = new Scene(root, 420, 320);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load login screen", ex);
        }
    }

    private static void showDashboard(User user) {
        try {
            SessionContext.setCurrentUser(user);
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/sacco/ui/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root, 900, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load dashboard", ex);
        }
    }
}
