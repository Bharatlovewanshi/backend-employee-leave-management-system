
package com.leave.employee.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.leave.employee.entity.User;
import com.leave.employee.repository.UserRepository;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader =
                request.getHeader("Authorization");

        logger.info("Request received: {}", request.getRequestURI());

        // Check Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            logger.warn("Authorization header missing or invalid");

            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {

            String email = jwtService.extractEmail(jwt);

            logger.info("JWT email extracted successfully: {}", email);

            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmail(email).orElse(null);

                if (user == null) {

                    logger.warn("User not found for JWT email");

                } else if (!user.isEnabled()) {

                    logger.warn("User account is disabled");

                } else if (jwtService.isTokenValid(jwt, user)) {

                    UserDetails userDetails =
                            org.springframework.security.core.userdetails.User
                                    .withUsername(user.getEmail())
                                    .password(user.getPassword())
                                    .authorities("ROLE_" + user.getRole().name())
                                    .build();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    logger.info("JWT authentication successful for: {}", email);

                    // DEBUG LOG 1: Check assigned roles
                    logger.info(
                            "Authenticated authorities: {}",
                            SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getAuthorities()
                    );

                } else {

                    logger.warn("JWT token validation failed");

                }
            }

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();

            logger.error(
                    "JWT authentication failed: {}",
                    exception.getMessage()
            );
        }

        // DEBUG LOG 2: Check authentication before continuing
        logger.info(
                "Before continuing filter chain, authentication: {}",
                SecurityContextHolder.getContext().getAuthentication()
        );

        filterChain.doFilter(request, response);
    }
}
