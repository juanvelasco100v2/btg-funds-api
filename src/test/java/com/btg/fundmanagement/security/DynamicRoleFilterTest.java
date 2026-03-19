package com.btg.fundmanagement.security;

import com.btg.fundmanagement.entity.Permission;
import com.btg.fundmanagement.service.RoleService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicRoleFilterTest {

    @Mock private RoleService roleService;
    @Mock private FilterChain filterChain;

    private DynamicRoleFilter filter;

    @BeforeEach
    void setUp() {
        filter = new DynamicRoleFilter(roleService);
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(Set<String> roleIds) {
        var authorities = roleIds.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
        var auth = new UsernamePasswordAuthenticationToken("u1", roleIds, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Permission createPermission(String endpoint, List<String> methods) {
        var p = new Permission();
        p.setEndpoint(endpoint);
        p.setMethods(methods);
        return p;
    }

    @Test
    void allowedRequest_passesThrough() throws ServletException, IOException {
        setAuthentication(Set.of("CLIENT"));
        var request = new MockHttpServletRequest("GET", "/api/funds");
        var response = new MockHttpServletResponse();
        when(roleService.getPermissionsForRoles(Set.of("CLIENT")))
                .thenReturn(List.of(createPermission("/api/funds/**", List.of("GET"))));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotEquals(403, response.getStatus());
    }

    @Test
    void deniedRequest_returns403() throws ServletException, IOException {
        setAuthentication(Set.of("CLIENT"));
        var request = new MockHttpServletRequest("DELETE", "/api/admin/roles/1");
        var response = new MockHttpServletResponse();
        when(roleService.getPermissionsForRoles(Set.of("CLIENT")))
                .thenReturn(List.of(createPermission("/api/funds/**", List.of("GET"))));

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void noAuthentication_passesThrough() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/funds");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void wrongMethod_returns403() throws ServletException, IOException {
        setAuthentication(Set.of("CLIENT"));
        var request = new MockHttpServletRequest("POST", "/api/funds");
        var response = new MockHttpServletResponse();
        when(roleService.getPermissionsForRoles(Set.of("CLIENT")))
                .thenReturn(List.of(createPermission("/api/funds/**", List.of("GET"))));

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void shouldNotFilter_authEndpoints() {
        var request = new MockHttpServletRequest("POST", "/api/auth/login");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_setupEndpoints() {
        var request = new MockHttpServletRequest("POST", "/api/setup/admin");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_actuatorEndpoints() {
        var request = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_swaggerEndpoints() {
        var request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_apiDocsEndpoints() {
        var request = new MockHttpServletRequest("GET", "/v3/api-docs");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_optionsMethod() {
        var request = new MockHttpServletRequest("OPTIONS", "/api/funds");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldFilter_protectedEndpoints() {
        var request = new MockHttpServletRequest("GET", "/api/funds");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void denied_responseBodyContainsMessage() throws ServletException, IOException {
        setAuthentication(Set.of("CLIENT"));
        var request = new MockHttpServletRequest("DELETE", "/api/admin/roles/1");
        var response = new MockHttpServletResponse();
        when(roleService.getPermissionsForRoles(Set.of("CLIENT")))
                .thenReturn(List.of(createPermission("/api/funds/**", List.of("GET"))));

        filter.doFilterInternal(request, response, filterChain);

        var body = response.getContentAsString();
        assertTrue(body.contains("No tiene permisos"));
        assertTrue(body.contains("403"));
    }
}
