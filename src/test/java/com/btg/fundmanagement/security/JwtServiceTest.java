package com.btg.fundmanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "this-is-a-very-long-secret-key-for-testing-purposes-must-be-at-least-64-characters-long";
    private static final long EXPIRATION = 86400000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        var token = jwtService.generateToken("user1", "test@test.com", Set.of("CLIENT"));
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUserId_returnsCorrectUserId() {
        var token = jwtService.generateToken("user1", "test@test.com", Set.of("CLIENT"));
        assertEquals("user1", jwtService.extractUserId(token));
    }

    @Test
    void extractRoles_returnsCorrectRoles() {
        var token = jwtService.generateToken("user1", "test@test.com", Set.of("ADMIN", "CLIENT"));
        var roles = jwtService.extractRoles(token);
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("CLIENT"));
        assertEquals(2, roles.size());
    }

    @Test
    void isValid_returnsTrueForValidToken() {
        var token = jwtService.generateToken("user1", "test@test.com", Set.of("CLIENT"));
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void isValid_returnsFalseForInvalidToken() {
        assertFalse(jwtService.isValid("invalid.token.here"));
    }

    @Test
    void isValid_returnsFalseForExpiredToken() {
        var expiredService = new JwtService(SECRET, -1000);
        var token = expiredService.generateToken("user1", "test@test.com", Set.of("CLIENT"));
        assertFalse(jwtService.isValid(token));
    }

    @Test
    void parseToken_returnsClaimsWithEmail() {
        var token = jwtService.generateToken("user1", "test@test.com", Set.of("CLIENT"));
        var claims = jwtService.parseToken(token);
        assertEquals("test@test.com", claims.get("email"));
    }

    @Test
    void isValid_returnsFalseForTokenWithDifferentKey() {
        var otherService = new JwtService("another-secret-key-that-is-also-at-least-64-characters-long-for-testing", EXPIRATION);
        var token = otherService.generateToken("user1", "test@test.com", Set.of("CLIENT"));
        assertFalse(jwtService.isValid(token));
    }
}
