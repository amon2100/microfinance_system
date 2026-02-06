package com.sacco.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuditRepository {
    public void insert(Connection connection, Long actorUserId, String action, String entityType, Long entityId, String details) {
        String sql = "INSERT INTO audit_logs(actor_user_id, action, entity_type, entity_id, details) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (actorUserId == null) {
                ps.setNull(1, java.sql.Types.BIGINT);
            } else {
                ps.setLong(1, actorUserId);
            }
            ps.setString(2, action);
            ps.setString(3, entityType);
            if (entityId == null) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, entityId);
            }
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to insert audit log", ex);
        }
    }
}
