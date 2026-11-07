
package com.leave.admin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.leave.admin.security.JwtService;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void shouldGenerateTokenSuccessfully() {

        String token = jwtService.generateToken("admin");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsernameFromToken() {

        String token = jwtService.generateToken("admin");

        String username = jwtService.extractUsername(token);

        assertEquals("admin", username);
    }

    @Test
    void shouldValidateCorrectToken() {

        String token = jwtService.generateToken("admin");

        boolean result = jwtService.isTokenValid(token, "admin");

        assertTrue(result);
    }

    @Test
    void shouldRejectTokenForDifferentUsername() {

        String token = jwtService.generateToken("admin");

        boolean result = jwtService.isTokenValid(token, "employee");

        assertFalse(result);
    }

    @Test
    void shouldRejectMalformedToken() {

        assertThrows(
                Exception.class,
                () -> jwtService.extractUsername("invalid.token.123")
        );
    }
}
