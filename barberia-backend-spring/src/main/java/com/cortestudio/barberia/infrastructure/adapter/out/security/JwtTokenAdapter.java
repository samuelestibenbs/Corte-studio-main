package com.cortestudio.barberia.infrastructure.adapter.out.security;

import com.cortestudio.barberia.application.port.out.TokenPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenAdapter implements TokenPort {
    private final SecretKey key;

    public JwtTokenAdapter(@Value("${app.jwt-secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String create(String subject) {
        Date now = new Date();
        return Jwts.builder().subject(subject).issuedAt(now)
                .expiration(new Date(now.getTime() + 24 * 60 * 60 * 1000L)).signWith(key).compact();
    }

    @Override
    public String getSubject(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }
}