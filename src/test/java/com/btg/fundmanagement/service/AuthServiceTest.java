package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.entity.User;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.UserRepository;
import com.btg.fundmanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, 500000);
    }

    @Test
    void register_success() {
        var request = new Requests.Register("test@test.com", "Test User", "password123", "+57300", "EMAIL");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(jwtService.generateToken(anyString(), eq("test@test.com"), eq(Set.of("CLIENT")))).thenReturn("token");

        var result = authService.register(request);

        assertEquals("token", result.token());
        assertEquals("test@test.com", result.email());
        assertEquals(Set.of("CLIENT"), result.roles());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailAlreadyExists_throwsException() {
        var request = new Requests.Register("test@test.com", "Test", "password123", null, null);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        assertThrows(ApiException.EmailAlreadyExists.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_defaultNotificationPreference_isEmail() {
        var request = new Requests.Register("test@test.com", "Test", "password123", null, null);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(jwtService.generateToken(anyString(), anyString(), any())).thenReturn("token");

        authService.register(request);

        verify(userRepository).save(argThat(user -> "EMAIL".equals(user.getNotificationPreference())));
    }

    @Test
    void login_success() {
        var request = new Requests.Login("test@test.com", "password123");
        var user = new User();
        user.setUserId("u1");
        user.setEmail("test@test.com");
        user.setPassword("encoded");
        user.setRoleIds(Set.of("CLIENT"));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtService.generateToken("u1", "test@test.com", Set.of("CLIENT"))).thenReturn("token");

        var result = authService.login(request);

        assertEquals("token", result.token());
        assertEquals("u1", result.userId());
    }

    @Test
    void login_invalidEmail_throwsException() {
        var request = new Requests.Login("wrong@test.com", "password");
        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        assertThrows(ApiException.InvalidCredentials.class, () -> authService.login(request));
    }

    @Test
    void login_wrongPassword_throwsException() {
        var request = new Requests.Login("test@test.com", "wrong");
        var user = new User();
        user.setPassword("encoded");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(ApiException.InvalidCredentials.class, () -> authService.login(request));
    }

    @Test
    void setupAdmin_success() {
        var request = new Requests.SetupAdmin("admin@test.com", "Admin", "password123", "+57300");
        when(userRepository.hasAnyAdmin()).thenReturn(false);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(jwtService.generateToken(anyString(), eq("admin@test.com"), eq(Set.of("ADMIN", "CLIENT")))).thenReturn("token");

        var result = authService.setupAdmin(request);

        assertEquals("token", result.token());
        assertEquals(Set.of("ADMIN", "CLIENT"), result.roles());
        verify(userRepository).save(argThat(user -> user.getRoleIds().contains("ADMIN")));
    }

    @Test
    void setupAdmin_adminAlreadyExists_throwsException() {
        var request = new Requests.SetupAdmin("admin@test.com", "Admin", "password123", null);
        when(userRepository.hasAnyAdmin()).thenReturn(true);

        assertThrows(ApiException.AdminAlreadyExists.class, () -> authService.setupAdmin(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void setupAdmin_emailAlreadyExists_throwsException() {
        var request = new Requests.SetupAdmin("existing@test.com", "Admin", "password123", null);
        when(userRepository.hasAnyAdmin()).thenReturn(false);
        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(new User()));

        assertThrows(ApiException.EmailAlreadyExists.class, () -> authService.setupAdmin(request));
    }
}
