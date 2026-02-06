package com.sacco.backend.security;

public record AuthUser(String username, Long userId, String role) {
}
