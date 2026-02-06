package com.sacco.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.sacco.model.Role;
import com.sacco.repository.UserRepository;
import com.sacco.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public final class DbInitializer {
    private DbInitializer() {
    }

    public static void initialize() {
        try (Connection connection = Database.getConnection()) {
            String schema = readResource("schema.sql");
            if (schema != null && !schema.isBlank()) {
                for (String statement : schema.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        connection.createStatement().execute(trimmed);
                    }
                }
            }
            applyMigrations(connection);
            ensureDefaultDirector(connection);
            ensureDirectorUserExists(connection);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize database", ex);
        }
    }

    private static String readResource(String name) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                DbInitializer.class.getClassLoader().getResourceAsStream(name),
                StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private static void applyMigrations(Connection connection) {
        migrateAdminRoleToDirector(connection);
        addColumnIfMissing(connection, "users", "member_id", "INTEGER");
        addColumnIfMissing(connection, "users", "external_id", "TEXT");
        addColumnIfMissing(connection, "members", "active", "INTEGER NOT NULL DEFAULT 1");
        addColumnIfMissing(connection, "members", "photo_path", "TEXT");
        addColumnIfMissing(connection, "members", "external_id", "TEXT");

        addColumnIfMissing(connection, "savings_accounts", "external_id", "TEXT");
        addColumnIfMissing(connection, "loans", "external_id", "TEXT");
        addColumnIfMissing(connection, "transactions", "external_id", "TEXT");

        createOutboxIfMissing(connection);

        addColumnIfMissing(connection, "loans", "term_months", "INTEGER NOT NULL DEFAULT 12");
        addColumnIfMissing(connection, "loans", "created_by", "INTEGER");
        addColumnIfMissing(connection, "loans", "approved_by", "INTEGER");
        addColumnIfMissing(connection, "loans", "disbursed_by", "INTEGER");
        addColumnIfMissing(connection, "loans", "outstanding_balance", "NUMERIC NOT NULL DEFAULT 0");

        addColumnIfMissing(connection, "transactions", "reversed_of", "INTEGER");
    }

    private static void migrateAdminRoleToDirector(Connection connection) {
        try {
            connection.createStatement().execute("UPDATE users SET role = 'DIRECTOR' WHERE role = 'ADMIN'");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to migrate ADMIN role", ex);
        }
    }

    private static void addColumnIfMissing(Connection connection, String table, String column, String definition) {
        try (PreparedStatement ps = connection.prepareStatement("PRAGMA table_info(" + table + ")")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (column.equalsIgnoreCase(rs.getString("name"))) {
                        return;
                    }
                }
            }
            connection.createStatement().execute(
                    "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition
            );
        } catch (Exception ex) {
            throw new RuntimeException("Failed to migrate table " + table + " for column " + column, ex);
        }
    }

    private static void createOutboxIfMissing(Connection connection) {
        try {
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS outbox (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "type TEXT NOT NULL, " +
                            "payload TEXT NOT NULL, " +
                            "status TEXT NOT NULL DEFAULT 'PENDING', " +
                            "last_error TEXT, " +
                            "created_at TEXT NOT NULL DEFAULT (datetime('now'))" +
                            ")"
            );
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create outbox table", ex);
        }
    }

    private static void ensureDefaultDirector(Connection connection) {
        String countSql = "SELECT COUNT(*) AS total FROM users";
        try (PreparedStatement ps = connection.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt("total") == 0) {
                UserRepository userRepository = new UserRepository();
                String hash = PasswordHasher.hash("ChangeMe123!");
                userRepository.create(connection, UUID.randomUUID().toString(), "director", hash, Role.DIRECTOR);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to seed default director", ex);
        }
    }

    private static void ensureDirectorUserExists(Connection connection) {
        String sql = "SELECT id FROM users WHERE username = 'director' LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                UserRepository userRepository = new UserRepository();
                String hash = PasswordHasher.hash("ChangeMe123!");
                userRepository.create(connection, UUID.randomUUID().toString(), "director", hash, Role.DIRECTOR);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to ensure director user", ex);
        }
    }
}
