package com.sacco.service;

import com.sacco.config.Database;
import com.sacco.model.Member;
import com.sacco.repository.AuditRepository;
import com.sacco.repository.MemberRepository;

import java.sql.Connection;
import java.util.List;

public class MemberService {
    private final MemberRepository memberRepository = new MemberRepository();
    private final AuditRepository auditRepository = new AuditRepository();

    public long createMember(long actorUserId, String fullName, String nationalId, String phone, String email) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long memberId = memberRepository.create(connection, fullName, nationalId, phone, email);
                auditRepository.insert(connection, actorUserId, "MEMBER_CREATED", "MEMBER", memberId,
                        "Member=" + fullName);
                connection.commit();
                return memberId;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Member creation failed", ex);
        }
    }

    public List<Member> listMembers() {
        try (Connection connection = Database.getConnection()) {
            return memberRepository.findAll(connection);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to list members", ex);
        }
    }
}
