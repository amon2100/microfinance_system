package com.sacco.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
    private final Key signingKey;
    private final String issuer;
    private final int expirationMinutes;

    public JwtService(@Value("${app.security.jwtSecret}") String secret,
                      @Value("${app.security.jwtIssuer}") String issuer,
                      @Value("${app.security.jwtExpirationMinutes}") int expirationMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String username, String role, Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public io.jsonwebtoken.Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
