
package com.leave.admin.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // Temporary development secret.
    // Replace it with a secure environment variable before deployment.
    private static final String SECRET_KEY =
            "VGhpc0lzQVNlY3JldEtleUZvckpXVEF1dGhlbnRpY2F0aW9uMjAyNg==";

    private static final long EXPIRATION_TIME =
            1000 * 60 * 60; // 1 hour

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate JWT token
    public String generateToken(String username) {

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()
                                + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    // Extract username from JWT
    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    // Validate JWT token
    public boolean isTokenValid(String token, String username) {

        String extractedUsername = extractUsername(token);

        return extractedUsername.equals(username)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                        Decoders.BASE64.decode(SECRET_KEY)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
