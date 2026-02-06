package com.sacco.repository;

import com.sacco.model.Role;
import com.sacco.model.User;
import com.sacco.model.UserView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    public User findByUsername(Connection connection, String username) {
        String sql = "SELECT id, external_id, username, password_hash, role, member_id, active FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("id"),
                            rs.getString("external_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            Role.valueOf(rs.getString("role")),
                            rs.getObject("member_id") == null ? null : rs.getLong("member_id"),
                            rs.getInt("active") == 1
                    );
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to find user", ex);
        }
        return null;
    }

    public long create(Connection connection, String externalId, String username, String passwordHash, Role role) {
        String sql = "INSERT INTO users(external_id, username, password_hash, role, active) VALUES(?, ?, ?, ?, 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, externalId);
            ps.setString(2, username);
            ps.setString(3, passwordHash);
            ps.setString(4, role.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create user", ex);
        }
        return -1;
    }

    public User findById(Connection connection, long userId) {
        String sql = "SELECT id, external_id, username, password_hash, role, member_id, active FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("id"),
                            rs.getString("external_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            Role.valueOf(rs.getString("role")),
                            rs.getObject("member_id") == null ? null : rs.getLong("member_id"),
                            rs.getInt("active") == 1
                    );
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to find user by id", ex);
        }
        return null;
    }

    public List<UserView> findAll(Connection connection) {
        String sql = "SELECT id, external_id, username, role, active FROM users ORDER BY id DESC";
        List<UserView> users = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new UserView(
                        rs.getLong("id"),
                        rs.getString("username"),
                        Role.valueOf(rs.getString("role")),
                        rs.getInt("active") == 1
                ));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to list users", ex);
        }
        return users;
    }
}
