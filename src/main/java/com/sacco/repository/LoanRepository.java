package com.sacco.repository;

import com.sacco.model.Loan;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LoanRepository {
        public long create(Connection connection, String externalId, long memberId, BigDecimal principal, BigDecimal interestRate,
                   int termMonths, long createdBy) {
        String sql = "INSERT INTO loans(external_id, member_id, principal, interest_rate, term_months, status, created_by, outstanding_balance) " +
            "VALUES(?, ?, ?, ?, ?, 'PENDING', ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, externalId);
            ps.setLong(2, memberId);
            ps.setBigDecimal(3, principal);
            ps.setBigDecimal(4, interestRate);
            ps.setInt(5, termMonths);
            ps.setLong(6, createdBy);
            ps.setBigDecimal(7, principal);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create loan", ex);
        }
        return -1;
    }

    public List<Loan> findAll(Connection connection) {
        String sql = "SELECT l.id, l.external_id, l.member_id, m.full_name, l.principal, l.interest_rate, l.term_months, l.issued_at, l.status, " +
                "l.outstanding_balance, l.created_by, l.approved_by, l.disbursed_by " +
                "FROM loans l JOIN members m ON m.id = l.member_id ORDER BY l.id DESC";
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                loans.add(new Loan(
                        rs.getLong("id"),
                    rs.getString("external_id"),
                        rs.getLong("member_id"),
                        rs.getString("full_name"),
                        rs.getBigDecimal("principal"),
                        rs.getBigDecimal("interest_rate"),
                        rs.getInt("term_months"),
                        rs.getString("issued_at"),
                        rs.getString("status"),
                        rs.getBigDecimal("outstanding_balance"),
                        rs.getObject("created_by") == null ? null : rs.getLong("created_by"),
                        rs.getObject("approved_by") == null ? null : rs.getLong("approved_by"),
                        rs.getObject("disbursed_by") == null ? null : rs.getLong("disbursed_by")
                ));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load loans", ex);
        }
        return loans;
    }

    public Loan findById(Connection connection, long loanId) {
        String sql = "SELECT l.id, l.external_id, l.member_id, m.full_name, l.principal, l.interest_rate, l.term_months, l.issued_at, l.status, " +
                "l.outstanding_balance, l.created_by, l.approved_by, l.disbursed_by " +
                "FROM loans l JOIN members m ON m.id = l.member_id WHERE l.id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Loan(
                            rs.getLong("id"),
                            rs.getString("external_id"),
                            rs.getLong("member_id"),
                            rs.getString("full_name"),
                            rs.getBigDecimal("principal"),
                            rs.getBigDecimal("interest_rate"),
                            rs.getInt("term_months"),
                            rs.getString("issued_at"),
                            rs.getString("status"),
                            rs.getBigDecimal("outstanding_balance"),
                            rs.getObject("created_by") == null ? null : rs.getLong("created_by"),
                            rs.getObject("approved_by") == null ? null : rs.getLong("approved_by"),
                            rs.getObject("disbursed_by") == null ? null : rs.getLong("disbursed_by")
                    );
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load loan", ex);
        }
        return null;
    }

    public boolean hasDefaultedLoans(Connection connection, long memberId) {
        String sql = "SELECT id FROM loans WHERE member_id = ? AND status = 'DEFAULTED' LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to check defaulted loans", ex);
        }
    }

    public void updateStatus(Connection connection, long loanId, String status, Long approvedBy, Long disbursedBy) {
        String sql = "UPDATE loans SET status = ?, approved_by = COALESCE(?, approved_by), disbursed_by = COALESCE(?, disbursed_by) " +
                "WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            if (approvedBy == null) {
                ps.setNull(2, java.sql.Types.BIGINT);
            } else {
                ps.setLong(2, approvedBy);
            }
            if (disbursedBy == null) {
                ps.setNull(3, java.sql.Types.BIGINT);
            } else {
                ps.setLong(3, disbursedBy);
            }
            ps.setLong(4, loanId);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to update loan status", ex);
        }
    }

    public void updateOutstandingBalance(Connection connection, long loanId, BigDecimal newBalance) {
        String sql = "UPDATE loans SET outstanding_balance = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, loanId);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to update outstanding balance", ex);
        }
    }
}
