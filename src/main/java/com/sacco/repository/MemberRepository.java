package com.sacco.repository;

import com.sacco.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MemberRepository {
    public long create(Connection connection, String fullName, String nationalId, String phone, String email) {
        String sql = "INSERT INTO members(full_name, national_id, phone, email) VALUES(?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, nationalId);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create member", ex);
        }
        return -1;
    }

    public List<Member> findAll(Connection connection) {
        String sql = "SELECT id, full_name, national_id, phone, email FROM members ORDER BY id DESC";
        List<Member> members = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                members.add(new Member(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("national_id"),
                        rs.getString("phone"),
                        rs.getString("email")
                ));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load members", ex);
        }
        return members;
    }

    public boolean existsActiveById(Connection connection, long memberId) {
        String sql = "SELECT id FROM members WHERE id = ? AND active = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to check member active", ex);
        }
    }
}
