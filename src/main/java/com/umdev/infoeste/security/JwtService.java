package com.umdev.infoeste.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {
    private final long EXPIRATION_TIME;
    private static final String TOKEN_PREFIX = "Bearer ";

    private final SecretKey key;

    public JwtService(@Value("${jwt.expiration}") long expirationTime,
                      @Value("${jwt.secret}") String secret) {
        EXPIRATION_TIME = expirationTime;
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public String getAuthUser(HttpServletRequest request) {
        String token = request
                .getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null) {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token.replace(TOKEN_PREFIX, ""))
                    .getPayload()
                    .getSubject();
        }
        return null;
    }

}
