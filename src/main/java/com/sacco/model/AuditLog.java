package com.sacco.model;

import java.time.Instant;

public class AuditLog {
    private final long id;
    private final Long actorUserId;
    private final String action;
    private final String entityType;
    private final Long entityId;
    private final String details;
    private final Instant createdAt;

    public AuditLog(long id, Long actorUserId, String action, String entityType, Long entityId, String details, Instant createdAt) {
        this.id = id;
        this.actorUserId = actorUserId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
