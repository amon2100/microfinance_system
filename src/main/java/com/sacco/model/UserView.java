package com.sacco.model;

public class UserView {
    private final long id;
    private final String username;
    private final Role role;
    private final boolean active;

    public UserView(long id, String username, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
