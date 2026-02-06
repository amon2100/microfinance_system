package com.sacco.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoanRepaymentRepository {
    public long insert(Connection connection, long loanId, BigDecimal amount) {
        String sql = "INSERT INTO loan_repayments(loan_id, amount) VALUES(?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, loanId);
            ps.setBigDecimal(2, amount);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to insert loan repayment", ex);
        }
        return -1;
    }
}
