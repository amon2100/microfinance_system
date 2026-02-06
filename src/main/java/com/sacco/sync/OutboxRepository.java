package com.sacco.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OutboxRepository {
    public void enqueue(Connection connection, String type, String payload) {
        String sql = "INSERT INTO outbox(type, payload, status) VALUES(?, ?, 'PENDING')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, payload);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to enqueue outbox item", ex);
        }
    }

    public List<OutboxItem> findPending(Connection connection) {
        String sql = "SELECT id, type, payload, status FROM outbox WHERE status = 'PENDING' ORDER BY id ASC";
        List<OutboxItem> items = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new OutboxItem(
                        rs.getLong("id"),
                        rs.getString("type"),
                        rs.getString("payload"),
                        rs.getString("status")
                ));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to list outbox", ex);
        }
        return items;
    }

    public void markSent(Connection connection, long id) {
        String sql = "UPDATE outbox SET status = 'SENT', last_error = NULL WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to mark outbox sent", ex);
        }
    }

    public void markFailed(Connection connection, long id, String error) {
        String sql = "UPDATE outbox SET status = 'PENDING', last_error = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, error);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to mark outbox failed", ex);
        }
    }
}
