package com.sacco.model;

public class User {
    private final long id;
    private final String username;
    private final String passwordHash;
    private final Role role;
    private final Long memberId;
    private final boolean active;

    public User(long id, String username, String passwordHash, Role role, Long memberId, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.memberId = memberId;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Long getMemberId() {
        return memberId;
    }

    public boolean isActive() {
        return active;
    }
}
