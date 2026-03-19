package com.btg.fundmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void validToken_setsAuthentication() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer valid.token.here");
        when(jwtService.isValid("valid.token.here")).thenReturn(true);
        when(jwtService.extractUserId("valid.token.here")).thenReturn("u1");
        when(jwtService.extractRoles("valid.token.here")).thenReturn(Set.of("CLIENT"));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("u1", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidToken_noAuthentication() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid.token");
        when(jwtService.isValid("invalid.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void noAuthHeader_noAuthentication() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void nonBearerHeader_noAuthentication() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void validToken_multipleRoles_allAuthoritiesSet() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer multi.role.token");
        when(jwtService.isValid("multi.role.token")).thenReturn(true);
        when(jwtService.extractUserId("multi.role.token")).thenReturn("u1");
        when(jwtService.extractRoles("multi.role.token")).thenReturn(Set.of("ADMIN", "CLIENT"));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(2, auth.getAuthorities().size());
    }

    @Test
    void validToken_credentialsContainRoles() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer valid.token.here");
        when(jwtService.isValid("valid.token.here")).thenReturn(true);
        when(jwtService.extractUserId("valid.token.here")).thenReturn("u1");
        when(jwtService.extractRoles("valid.token.here")).thenReturn(Set.of("CLIENT"));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(Set.of("CLIENT"), auth.getCredentials());
    }
}
