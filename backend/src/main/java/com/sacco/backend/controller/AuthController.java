package com.sacco.backend.controller;

import com.sacco.backend.dto.AuthRequest;
import com.sacco.backend.dto.AuthResponse;
import com.sacco.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        String token = authService.login(request);
        return new AuthResponse(token);
    }
}
