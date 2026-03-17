package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.entity.Permission;
import com.btg.fundmanagement.entity.Role;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 300_000; // 5 minutes

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Permission> getPermissionsForRoles(Set<String> roleIds) {
        return roleIds.stream()
                .map(this::getCachedRole)
                .filter(r -> r != null && r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .toList();
    }

    private Role getCachedRole(String roleId) {
        var entry = cache.get(roleId);
        if (entry != null && System.currentTimeMillis() - entry.timestamp < CACHE_TTL_MS) {
            return entry.role;
        }
        var role = roleRepository.findById(roleId).orElse(null);
        if (role != null) {
            cache.put(roleId, new CacheEntry(role, System.currentTimeMillis()));
        }
        return role;
    }

    public Responses.RoleInfo create(Requests.CreateRole request) {
        roleRepository.findById(request.roleId())
                .ifPresent(_ -> { throw new ApiException.RoleAlreadyExists(request.roleId()); });

        var role = new Role();
        role.setRoleId(request.roleId());
        role.setRoleName(request.roleName());
        role.setDescription(request.description());
        role.setPermissions(toPermissions(request.permissions()));
        role.setCreatedAt(Instant.now().toString());

        roleRepository.save(role);
        cache.remove(request.roleId());

        return toResponse(role);
    }

    public Responses.RoleInfo update(String roleId, Requests.UpdateRole request) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException.RoleNotFound(roleId));

        if (request.roleName() != null) role.setRoleName(request.roleName());
        if (request.description() != null) role.setDescription(request.description());
        if (request.permissions() != null) role.setPermissions(toPermissions(request.permissions()));

        roleRepository.save(role);
        cache.remove(roleId);

        return toResponse(role);
    }

    public void delete(String roleId) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException.RoleNotFound(roleId));
        roleRepository.delete(roleId);
        cache.remove(roleId);
    }

    public Responses.RoleInfo findById(String roleId) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException.RoleNotFound(roleId));
        return toResponse(role);
    }

    public List<Responses.RoleInfo> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    private List<Permission> toPermissions(List<Requests.PermissionEntry> entries) {
        return entries.stream().map(e -> {
            var p = new Permission();
            p.setEndpoint(e.endpoint());
            p.setMethods(e.methods());
            return p;
        }).toList();
    }

    private Responses.RoleInfo toResponse(Role role) {
        var perms = role.getPermissions() == null ? List.<Requests.PermissionEntry>of()
                : role.getPermissions().stream()
                .map(p -> new Requests.PermissionEntry(p.getEndpoint(), p.getMethods()))
                .toList();
        return new Responses.RoleInfo(role.getRoleId(), role.getRoleName(),
                role.getDescription(), perms, role.getCreatedAt());
    }

    private record CacheEntry(Role role, long timestamp) {}
}
