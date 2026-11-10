
package com.leave.manager.security;

import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("MANAGER", "EMPLOYEE", "ADMIN");

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {

        Object userId = extractAllClaims(token).get("userId");

        if (userId == null) {
            throw new IllegalArgumentException(
                    "JWT does not contain userId"
            );
        }

        return Long.valueOf(userId.toString());
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {

        Claims claims = extractAllClaims(token);

        String email = claims.getSubject();
        Object userId = claims.get("userId");
        String role = claims.get("role", String.class);

        if (email == null || email.isBlank()
                || userId == null
                || role == null
                || !ALLOWED_ROLES.contains(role)) {
            return false;
        }

        try {
            return Long.parseLong(userId.toString()) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    /**
     * Gets the authenticated employee ID.
     */
    public Long extractUserIdFromAuthentication(
            Authentication authentication) {

        return extractAuthenticatedUserId(authentication, "ROLE_EMPLOYEE");
    }

    /**
     * Gets the authenticated manager ID.
     */
    public Long extractManagerIdFromAuthentication(
            Authentication authentication) {

        return extractAuthenticatedUserId(authentication, "ROLE_MANAGER");
    }

    /**
     * Validates authentication and extracts the user ID.
     */
    private Long extractAuthenticatedUserId(
            Authentication authentication,
            String requiredRole) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                requiredRole.equals(
                                        authority.getAuthority()))) {

            throw new AccessDeniedException(
                    requiredRole.replace("ROLE_", "")
                            + " access is required."
            );
        }

        Object details = authentication.getDetails();

        if (!(details instanceof Long userId) || userId <= 0) {
            throw new AccessDeniedException(
                    "Authenticated user ID is unavailable."
            );
        }

        return userId;
    }
}
