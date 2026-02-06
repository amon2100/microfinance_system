package com.sacco.service;

import com.sacco.config.Database;
import com.sacco.model.Role;
import com.sacco.model.UserView;
import com.sacco.repository.AuditRepository;
import com.sacco.repository.UserRepository;
import com.sacco.util.PasswordHasher;
import com.sacco.util.JsonUtil;
import com.sacco.sync.OutboxRepository;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository = new UserRepository();
    private final AuditRepository auditRepository = new AuditRepository();
    private final AuthorizationService authorizationService = new AuthorizationService();
    private final OutboxRepository outboxRepository = new OutboxRepository();

    public long createUser(long actorUserId, String username, String password, Role role) {
        authorizationService.requireRole(actorUserId, "USER_CREATE", "USER", null, Role.DIRECTOR);
        try (Connection connection = Database.getConnection()) {
            String hash = PasswordHasher.hash(password);
            String externalId = UUID.randomUUID().toString();
            long userId = userRepository.create(connection, externalId, username, hash, role);
            enqueueSync(connection, externalId, username, role.name());
            auditRepository.insert(connection, actorUserId, "USER_CREATED", "USER", userId,
                    "Created user with role " + role.name());
            return userId;
        } catch (Exception ex) {
            throw new RuntimeException("User creation failed", ex);
        }
    }

    public List<UserView> listUsers(long actorUserId) {
        authorizationService.requireRole(actorUserId, "USER_LIST", "USER", null, Role.DIRECTOR);
        try (Connection connection = Database.getConnection()) {
            return userRepository.findAll(connection);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to list users", ex);
        }
    }

    private void enqueueSync(Connection connection, String externalId, String username, String role) {
        String payload = "{" +
                "\"externalId\":\"" + JsonUtil.escape(externalId) + "\"," +
                "\"username\":\"" + JsonUtil.escape(username) + "\"," +
                "\"role\":\"" + JsonUtil.escape(role) + "\"" +
                "}";
        outboxRepository.enqueue(connection, "USER_CREATE", payload);
    }
}
