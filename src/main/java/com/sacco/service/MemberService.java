package com.sacco.service;

import com.sacco.config.Database;
import com.sacco.model.Member;
import com.sacco.repository.AuditRepository;
import com.sacco.repository.MemberRepository;
import com.sacco.sync.OutboxRepository;
import com.sacco.util.JsonUtil;

import java.sql.Connection;
import java.util.UUID;
import java.util.List;

public class MemberService {
    private final MemberRepository memberRepository = new MemberRepository();
    private final AuditRepository auditRepository = new AuditRepository();
    private final OutboxRepository outboxRepository = new OutboxRepository();

    public long createMember(long actorUserId, String fullName, String nationalId, String phone, String email, String photoPath) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String externalId = UUID.randomUUID().toString();
                long memberId = memberRepository.create(connection, externalId, fullName, nationalId, phone, email, photoPath);
                enqueueSync(connection, externalId, fullName, nationalId, phone, email, photoPath);
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

    public List<Member> searchMembers(String term) {
        try (Connection connection = Database.getConnection()) {
            return memberRepository.search(connection, term);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to search members", ex);
        }
    }

    private void enqueueSync(Connection connection, String externalId, String fullName, String nationalId, String phone, String email, String photoPath) {
        String payload = buildMemberJson(externalId, fullName, nationalId, phone, email);
        outboxRepository.enqueue(connection, "MEMBER_CREATE", payload);
        if (photoPath != null && !photoPath.isBlank()) {
            outboxRepository.enqueue(connection, "MEMBER_PHOTO", externalId + "|" + photoPath);
        }
    }

    private String buildMemberJson(String externalId, String fullName, String nationalId, String phone, String email) {
        return "{" +
                    "\"externalId\":\"" + JsonUtil.escape(externalId) + "\"," +
                    "\"fullName\":\"" + JsonUtil.escape(fullName) + "\"," +
                    "\"nationalId\":\"" + JsonUtil.escape(nationalId) + "\"," +
                    "\"phone\":\"" + JsonUtil.escape(phone) + "\"," +
                    "\"email\":\"" + JsonUtil.escape(email) + "\"" +
                "}";
    }

}
