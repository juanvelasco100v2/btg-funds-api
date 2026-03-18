package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.entity.User;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    private User createUser() {
        var user = new User();
        user.setUserId("u1");
        user.setEmail("test@test.com");
        user.setName("Test");
        user.setBalance(500000);
        user.setNotificationPreference("EMAIL");
        user.setPhone("+57300");
        user.setRoleIds(Set.of("CLIENT"));
        return user;
    }

    @Test
    void getProfile_success() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(createUser()));
        var result = userService.getProfile("u1");
        assertEquals("u1", result.userId());
        assertEquals("test@test.com", result.email());
        assertEquals(500000, result.balance());
    }

    @Test
    void getProfile_notFound_throwsException() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());
        assertThrows(ApiException.UserNotFound.class, () -> userService.getProfile("u1"));
    }

    @Test
    void updateProfile_updatesNotificationPreference() {
        var user = createUser();
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        var request = new Requests.UpdateProfile("SMS", null);

        var result = userService.updateProfile("u1", request);

        assertEquals("SMS", result.notificationPreference());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_updatesPhone() {
        var user = createUser();
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        var request = new Requests.UpdateProfile(null, "+57311");

        var result = userService.updateProfile("u1", request);

        assertEquals("+57311", result.phone());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_nullFields_noChanges() {
        var user = createUser();
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        var request = new Requests.UpdateProfile(null, null);

        var result = userService.updateProfile("u1", request);

        assertEquals("EMAIL", result.notificationPreference());
        assertEquals("+57300", result.phone());
    }

    @Test
    void updateProfile_notFound_throwsException() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());
        assertThrows(ApiException.UserNotFound.class,
                () -> userService.updateProfile("u1", new Requests.UpdateProfile("SMS", null)));
    }

    @Test
    void findAll_returnsMappedUsers() {
        when(userRepository.findAll()).thenReturn(List.of(createUser()));
        var result = userService.findAll();
        assertEquals(1, result.size());
        assertEquals("u1", result.getFirst().userId());
    }

    @Test
    void findAll_emptyList() {
        when(userRepository.findAll()).thenReturn(List.of());
        assertTrue(userService.findAll().isEmpty());
    }
}
