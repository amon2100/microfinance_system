package com.sacco.app;

import com.sacco.config.DbInitializer;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        DbInitializer.initialize();
        SceneManager.init(stage);
        SceneManager.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
