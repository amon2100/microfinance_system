package com.sacco.service;

import com.sacco.config.Database;
import com.sacco.model.Role;
import com.sacco.model.User;
import com.sacco.repository.AuditRepository;
import com.sacco.repository.UserRepository;

import java.sql.Connection;
import java.util.Arrays;

public class AuthorizationService {
    private final UserRepository userRepository = new UserRepository();
    private final AuditRepository auditRepository = new AuditRepository();

    public User requireRole(long actorUserId, String action, String entityType, Long entityId, Role... allowed) {
        try (Connection connection = Database.getConnection()) {
            User user = userRepository.findById(connection, actorUserId);
            if (user == null || !user.isActive()) {
                logAccessDenied(actorUserId, action, entityType, entityId, "User not found or inactive");
                throw new AccessDeniedException("Access denied");
            }
            boolean permitted = Arrays.stream(allowed).anyMatch(role -> role == user.getRole());
            if (!permitted) {
                logAccessDenied(actorUserId, action, entityType, entityId,
                        "Required roles: " + Arrays.toString(allowed) + ", actual: " + user.getRole());
                throw new AccessDeniedException("Access denied for role " + user.getRole());
            }
            return user;
        } catch (AccessDeniedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Authorization failed", ex);
        }
    }

    private void logAccessDenied(long actorUserId, String action, String entityType, Long entityId, String details) {
        try (Connection connection = Database.getConnection()) {
            auditRepository.insert(connection, actorUserId, "ACCESS_DENIED:" + action, entityType, entityId, details);
        } catch (Exception ignored) {
        }
    }
}
