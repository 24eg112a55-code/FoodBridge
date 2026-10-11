package com.communityfoodsharing.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;
    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }
    public String generateToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getUsername()).issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(expiration))).signWith(key).compact();
    }
    public String extractUsername(String token) { return claims(token).getSubject(); }
    public boolean isTokenValid(String token, UserDetails user) {
        try { return extractUsername(token).equalsIgnoreCase(user.getUsername()) && claims(token).getExpiration().after(new Date()); }
        catch (JwtException | IllegalArgumentException ex) { return false; }
    }
    private Claims claims(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}