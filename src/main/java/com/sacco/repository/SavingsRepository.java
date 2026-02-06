package com.sacco.repository;

import com.sacco.model.SavingsAccount;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SavingsRepository {
    public long createAccount(Connection connection, long memberId, String accountNo) {
        String sql = "INSERT INTO savings_accounts(member_id, account_no, balance) VALUES(?, ?, 0)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, memberId);
            ps.setString(2, accountNo);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create savings account", ex);
        }
        return -1;
    }

    public List<SavingsAccount> findAll(Connection connection) {
        String sql = "SELECT sa.id, sa.member_id, m.full_name, sa.account_no, sa.balance " +
                "FROM savings_accounts sa JOIN members m ON m.id = sa.member_id ORDER BY sa.id DESC";
        List<SavingsAccount> accounts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(new SavingsAccount(
                        rs.getLong("id"),
                        rs.getLong("member_id"),
                        rs.getString("full_name"),
                        rs.getString("account_no"),
                        rs.getBigDecimal("balance")
                ));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load savings accounts", ex);
        }
        return accounts;
    }

    public BigDecimal getBalance(Connection connection, String accountNo) {
        String sql = "SELECT balance FROM savings_accounts WHERE account_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get balance", ex);
        }
        return null;
    }

    public long getMemberId(Connection connection, String accountNo) {
        String sql = "SELECT member_id FROM savings_accounts WHERE account_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("member_id");
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get memberId", ex);
        }
        return -1;
    }

    public void updateBalance(Connection connection, String accountNo, BigDecimal newBalance) {
        String sql = "UPDATE savings_accounts SET balance = ? WHERE account_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setString(2, accountNo);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to update balance", ex);
        }
    }
}
