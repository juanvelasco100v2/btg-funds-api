package com.btg.fundmanagement.security;

import com.btg.fundmanagement.entity.Permission;
import com.btg.fundmanagement.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DynamicRoleFilter extends OncePerRequestFilter {

    private final RoleService roleService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DynamicRoleFilter(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getCredentials() instanceof Set<?> roleIds) {
            var method = request.getMethod();
            var path = request.getRequestURI();

            @SuppressWarnings("unchecked")
            var permissions = roleService.getPermissionsForRoles((Set<String>) roleIds);

            boolean allowed = permissions.stream()
                    .anyMatch(p -> pathMatcher.match(p.getEndpoint(), path)
                            && p.getMethods().contains(method));

            if (!allowed) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), Map.of(
                        "message", "No tiene permisos para acceder a este recurso",
                        "status", 403,
                        "timestamp", Instant.now().toString()
                ));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/setup/")
                || path.startsWith("/actuator/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs");
    }
}
