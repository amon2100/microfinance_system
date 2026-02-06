package com.sacco.service;

import com.sacco.config.Database;
import com.sacco.model.Role;
import com.sacco.model.User;
import com.sacco.repository.AuditRepository;
import com.sacco.repository.UserRepository;
import com.sacco.util.PasswordHasher;

import java.sql.Connection;

public class AuthService {
    private final UserRepository userRepository = new UserRepository();
    private final AuditRepository auditRepository = new AuditRepository();

    public User authenticate(String username, String password) {
        try (Connection connection = Database.getConnection()) {
            User user = userRepository.findByUsername(connection, username);
            if (user == null || !user.isActive()) {
                auditRepository.insert(connection, null, "LOGIN_FAILED", "USER", null, "Unknown or inactive user");
                return null;
            }
            if (!PasswordHasher.verify(password, user.getPasswordHash())) {
                auditRepository.insert(connection, user.getId(), "LOGIN_FAILED", "USER", user.getId(), "Invalid credentials");
                return null;
            }
            auditRepository.insert(connection, user.getId(), "LOGIN_SUCCESS", "USER", user.getId(), "User login");
            return user;
        } catch (Exception ex) {
            throw new RuntimeException("Authentication failed", ex);
        }
    }

    public long createUser(String username, String password, Role role) {
        try (Connection connection = Database.getConnection()) {
            String hash = PasswordHasher.hash(password);
            long userId = userRepository.create(connection, username, hash, role);
            auditRepository.insert(connection, userId, "USER_CREATED", "USER", userId, "Created user with role " + role.name());
            return userId;
        } catch (Exception ex) {
            throw new RuntimeException("User creation failed", ex);
        }
    }
}
