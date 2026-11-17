
package com.leave.manager.security;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final Set<String> ALLOWED_ROLES =
            Set.of("MANAGER", "EMPLOYEE", "ADMIN");

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        logger.info(
                "Incoming request: method={}, URI={}, Authorization header present={}",
                request.getMethod(),
                requestUri,
                authorizationHeader != null
        );

        // No token supplied.
        if (authorizationHeader == null) {
            logger.warn(
                    "JWT missing for request: {}",
                    requestUri
            );

            filterChain.doFilter(request, response);
            return;
        }

        // Validate Authorization header format.
        if (!authorizationHeader.startsWith("Bearer ")) {
            logger.warn(
                    "Invalid Authorization header format for: {}",
                    requestUri
            );

            rejectRequest(
                    response,
                    "Invalid Authorization header"
            );
            return;
        }

        String token =
                authorizationHeader.substring(7).trim();

        if (token.isBlank()) {
            logger.warn("Empty JWT token received");

            rejectRequest(
                    response,
                    "Empty JWT token"
            );
            return;
        }

        try {

            logger.info("Starting JWT validation for {}", requestUri);

            // Validate JWT signature, expiration and claims.
            boolean tokenValid =
                    jwtService.isTokenValid(token);

            logger.info(
                    "JWT validation result: {}",
                    tokenValid
            );

            if (!tokenValid) {
                logger.warn("JWT validation returned false");

                rejectRequest(
                        response,
                        "Invalid JWT claims"
                );
                return;
            }

            // Extract JWT claims.
            String email =
                    jwtService.extractEmail(token);

            String role =
                    jwtService.extractRole(token);

            Long userId =
                    jwtService.extractUserId(token);

            logger.info(
                    "JWT claims extracted successfully: email={}, role={}, userId={}",
                    email,
                    role,
                    userId
            );

            // Validate required claims.
            if (email == null || email.isBlank()
                    || role == null
                    || !ALLOWED_ROLES.contains(role)
                    || userId == null
                    || userId <= 0) {

                logger.warn(
                        "JWT claims are invalid: emailPresent={}, role={}, userId={}",
                        email != null && !email.isBlank(),
                        role,
                        userId
                );

                rejectRequest(
                        response,
                        "Invalid JWT claims"
                );
                return;
            }

            // Set authentication in Spring Security context.
            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                )
                        );

                // Store authenticated user ID.
                authentication.setDetails(userId);

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                logger.info(
                        "Spring Security authentication created: email={}, role={}, userId={}",
                        email,
                        role,
                        userId
                );

            } else {

                logger.debug(
                        "SecurityContext already contains authentication"
                );
            }

        } catch (Exception exception) {

            logger.error(
                    "JWT validation failed for request: {}. Exception type: {}, message: {}",
                    requestUri,
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    exception
            );

            rejectRequest(
                    response,
                    "Invalid or expired JWT token"
            );
            return;
        }

        // Continue to controller and authorization checks.
        filterChain.doFilter(request, response);
    }

    private void rejectRequest(
            HttpServletResponse response,
            String message) throws IOException {

        logger.warn("Request rejected: {}", message);

        SecurityContextHolder.clearContext();

        response.sendError(
                HttpStatus.UNAUTHORIZED.value(),
                message
        );
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/api/internal/notifications/leave-events"
                        .equals(request.getServletPath());
    }
}
