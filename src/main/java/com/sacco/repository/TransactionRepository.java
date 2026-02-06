package com.sacco.repository;

import java.math.BigDecimal;
import com.sacco.model.TransactionRecord;
import com.sacco.model.TransactionView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    public long insert(Connection connection, String externalId, long memberId, String type, BigDecimal amount) {
        String sql = "INSERT INTO transactions(external_id, member_id, type, amount) VALUES(?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, externalId);
            ps.setLong(2, memberId);
            ps.setString(3, type);
            ps.setBigDecimal(4, amount);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to insert transaction", ex);
        }
        return -1;
    }

    public List<TransactionView> findAll(Connection connection) {
        String sql = "SELECT t.id, m.full_name, t.type, t.amount, t.created_at " +
                "FROM transactions t JOIN members m ON m.id = t.member_id ORDER BY t.id DESC";
        List<TransactionView> transactions = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                transactions.add(new TransactionView(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("type"),
                        rs.getBigDecimal("amount"),
                        rs.getString("created_at")
                ));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load transactions", ex);
        }
        return transactions;
    }

    public TransactionRecord findById(Connection connection, long transactionId) {
        String sql = "SELECT id, member_id, type, amount, reversed_of FROM transactions WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TransactionRecord(
                            rs.getLong("id"),
                            rs.getLong("member_id"),
                            rs.getString("type"),
                            rs.getBigDecimal("amount"),
                            rs.getObject("reversed_of") == null ? null : rs.getLong("reversed_of")
                    );
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load transaction", ex);
        }
        return null;
    }

    public boolean hasReversal(Connection connection, long originalTransactionId) {
        String sql = "SELECT id FROM transactions WHERE reversed_of = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, originalTransactionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to check reversal", ex);
        }
    }

    public long insertReversal(Connection connection, String externalId, long memberId, long originalTransactionId, BigDecimal amount) {
        String sql = "INSERT INTO transactions(external_id, member_id, type, amount, reversed_of) VALUES(?, ?, 'REVERSAL', ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, externalId);
            ps.setLong(2, memberId);
            ps.setBigDecimal(3, amount);
            ps.setLong(4, originalTransactionId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to insert reversal", ex);
        }
        return -1;
    }
}
