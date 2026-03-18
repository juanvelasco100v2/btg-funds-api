package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @Mock private Authentication authentication;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
        when(authentication.getName()).thenReturn("u1");
    }

    @Test
    void getProfile_returnsUserInfo() {
        var expected = new Responses.UserInfo("u1", "test@test.com", "Test", 500000, "EMAIL", "+57300", Set.of("CLIENT"));
        when(userService.getProfile("u1")).thenReturn(expected);

        var result = userController.getProfile(authentication);

        assertEquals("u1", result.userId());
        assertEquals("test@test.com", result.email());
        assertEquals(500000, result.balance());
    }

    @Test
    void updateProfile_returnsUpdatedUserInfo() {
        var request = new Requests.UpdateProfile("SMS", "+57311");
        var expected = new Responses.UserInfo("u1", "test@test.com", "Test", 500000, "SMS", "+57311", Set.of("CLIENT"));
        when(userService.updateProfile("u1", request)).thenReturn(expected);

        var result = userController.updateProfile(authentication, request);

        assertEquals("SMS", result.notificationPreference());
        assertEquals("+57311", result.phone());
    }
}
