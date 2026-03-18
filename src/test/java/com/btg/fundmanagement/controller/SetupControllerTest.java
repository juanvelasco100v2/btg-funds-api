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
class SetupControllerTest {

    @Mock private AuthService authService;
    private SetupController setupController;

    @BeforeEach
    void setUp() {
        setupController = new SetupController(authService);
    }

    @Test
    void setupAdmin_returnsAuthResponse() {
        var request = new Requests.SetupAdmin("admin@test.com", "Admin", "password123", "+57300");
        var expected = new Responses.Auth("token", "u1", "admin@test.com", Set.of("ADMIN", "CLIENT"));
        when(authService.setupAdmin(request)).thenReturn(expected);

        var result = setupController.setupAdmin(request);

        assertEquals("token", result.token());
        assertEquals(Set.of("ADMIN", "CLIENT"), result.roles());
    }
}
