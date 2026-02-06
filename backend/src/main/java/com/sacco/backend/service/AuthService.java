package com.sacco.backend.service;

import com.sacco.backend.dto.AuthRequest;
import com.sacco.backend.model.User;
import com.sacco.backend.repository.UserRepository;
import com.sacco.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public String login(AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        if (!auth.isAuthenticated()) {
            throw new RuntimeException("Invalid credentials");
        }
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtService.generateToken(user.getUsername(), user.getRole().name(), user.getId());
    }
}
