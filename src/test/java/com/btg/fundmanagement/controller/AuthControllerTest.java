package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void register_returnsAuthResponse() {
        var request = new Requests.Register("test@test.com", "Test", "password123", "+57300", "EMAIL");
        var expected = new Responses.Auth("token", "u1", "test@test.com", Set.of("CLIENT"));
        when(authService.register(request)).thenReturn(expected);

        var result = authController.register(request);

        assertEquals("token", result.token());
        assertEquals("u1", result.userId());
        assertEquals("test@test.com", result.email());
        assertEquals(Set.of("CLIENT"), result.roles());
    }

    @Test
    void login_returnsAuthResponse() {
        var request = new Requests.Login("test@test.com", "password123");
        var expected = new Responses.Auth("token", "u1", "test@test.com", Set.of("CLIENT"));
        when(authService.login(request)).thenReturn(expected);

        var result = authController.login(request);

        assertEquals("token", result.token());
        assertEquals("u1", result.userId());
    }
}
