package com.sacco.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final String DB_FILE = "sacco.db";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:" + getDatabasePath();
        Connection connection = DriverManager.getConnection(url);
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        return connection;
    }

    private static String getDatabasePath() {
        String userHome = System.getProperty("user.home");
        Path dataDir = Path.of(userHome, ".sacco", "data");
        try {
            Files.createDirectories(dataDir);
        } catch (Exception ignored) {
        }
        return dataDir.resolve(DB_FILE).toString();
    }
}
