package com.sacco.repository;

import com.sacco.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MemberRepository {
    public long create(Connection connection, String externalId, String fullName, String nationalId, String phone, String email, String photoPath) {
        String sql = "INSERT INTO members(external_id, full_name, national_id, phone, email, photo_path) VALUES(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, externalId);
            ps.setString(2, fullName);
            ps.setString(3, nationalId);
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.setString(6, photoPath);
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
        String sql = "SELECT id, external_id, full_name, national_id, phone, email, photo_path FROM members ORDER BY id DESC";
        List<Member> members = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                members.add(new Member(
                        rs.getLong("id"),
                    rs.getString("external_id"),
                        rs.getString("full_name"),
                        rs.getString("national_id"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("photo_path")
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

    public List<Member> search(Connection connection, String term) {
        String sql = "SELECT id, external_id, full_name, national_id, phone, email, photo_path FROM members " +
                "WHERE CAST(id AS TEXT) = ? OR lower(full_name) LIKE ? OR national_id = ? ORDER BY id DESC";
        List<Member> members = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, term);
            ps.setString(2, "%" + term.toLowerCase() + "%");
            ps.setString(3, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new Member(
                            rs.getLong("id"),
                            rs.getString("external_id"),
                            rs.getString("full_name"),
                            rs.getString("national_id"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("photo_path")
                    ));
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to search members", ex);
        }
        return members;
    }

    public String findExternalId(Connection connection, long memberId) {
        String sql = "SELECT external_id FROM members WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("external_id");
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load member external id", ex);
        }
        return null;
    }
}
