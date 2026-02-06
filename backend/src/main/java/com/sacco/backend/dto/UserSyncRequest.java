package com.sacco.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UserSyncRequest {
    @NotBlank
    private String externalId;
    @NotBlank
    private String username;
    @NotBlank
    private String role;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
