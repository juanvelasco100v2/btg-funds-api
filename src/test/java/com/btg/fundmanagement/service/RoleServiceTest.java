package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.entity.Permission;
import com.btg.fundmanagement.entity.Role;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository);
    }

    private Role createRole() {
        var role = new Role();
        role.setRoleId("CLIENT");
        role.setRoleName("Cliente");
        role.setDescription("Rol de cliente");
        var permission = new Permission();
        permission.setEndpoint("/api/funds/**");
        permission.setMethods(List.of("GET"));
        role.setPermissions(List.of(permission));
        role.setCreatedAt("2024-01-01T00:00:00Z");
        return role;
    }

    @Test
    void getPermissionsForRoles_returnsPermissions() {
        when(roleRepository.findById("CLIENT")).thenReturn(Optional.of(createRole()));

        var permissions = roleService.getPermissionsForRoles(Set.of("CLIENT"));

        assertEquals(1, permissions.size());
        assertEquals("/api/funds/**", permissions.getFirst().getEndpoint());
        assertEquals(List.of("GET"), permissions.getFirst().getMethods());
    }

    @Test
    void getPermissionsForRoles_multipleRoles() {
        var adminRole = new Role();
        adminRole.setRoleId("ADMIN");
        var adminPerm = new Permission();
        adminPerm.setEndpoint("/api/admin/**");
        adminPerm.setMethods(List.of("GET", "POST", "PUT", "DELETE"));
        adminRole.setPermissions(List.of(adminPerm));

        when(roleRepository.findById("CLIENT")).thenReturn(Optional.of(createRole()));
        when(roleRepository.findById("ADMIN")).thenReturn(Optional.of(adminRole));

        var permissions = roleService.getPermissionsForRoles(Set.of("CLIENT", "ADMIN"));

        assertEquals(2, permissions.size());
    }

    @Test
    void getPermissionsForRoles_roleNotFound_skipped() {
        when(roleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        var permissions = roleService.getPermissionsForRoles(Set.of("UNKNOWN"));

        assertTrue(permissions.isEmpty());
    }

    @Test
    void getPermissionsForRoles_nullPermissions_skipped() {
        var role = new Role();
        role.setRoleId("EMPTY");
        role.setPermissions(null);
        when(roleRepository.findById("EMPTY")).thenReturn(Optional.of(role));

        var permissions = roleService.getPermissionsForRoles(Set.of("EMPTY"));

        assertTrue(permissions.isEmpty());
    }

    @Test
    void getPermissionsForRoles_usesCache() {
        when(roleRepository.findById("CLIENT")).thenReturn(Optional.of(createRole()));

        roleService.getPermissionsForRoles(Set.of("CLIENT"));
        roleService.getPermissionsForRoles(Set.of("CLIENT"));

        verify(roleRepository, times(1)).findById("CLIENT");
    }

    @Test
    void create_success() {
        when(roleRepository.findById("NEW_ROLE")).thenReturn(Optional.empty());
        var request = new Requests.CreateRole("NEW_ROLE", "Nuevo Rol", "Descripcion",
                List.of(new Requests.PermissionEntry("/api/test", List.of("GET"))));

        var result = roleService.create(request);

        assertEquals("NEW_ROLE", result.roleId());
        assertEquals("Nuevo Rol", result.roleName());
        assertEquals("Descripcion", result.description());
        assertEquals(1, result.permissions().size());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void create_roleAlreadyExists_throwsException() {
        when(roleRepository.findById("CLIENT")).thenReturn(Optional.of(createRole()));
        var request = new Requests.CreateRole("CLIENT", "Cliente", null,
                List.of(new Requests.PermissionEntry("/api/test", List.of("GET"))));

        assertThrows(ApiException.RoleAlreadyExists.class, () -> roleService.create(request));
        verify(roleRepository, never()).save(any());
    }

    @Test
    void update_success() {
        when(roleRepository.findById("CLIENT")).thenReturn(Optional.of(createRole()));
        var request = new Requests.UpdateRole("Nuevo Nombre", "Nueva desc", null);

        var result = roleService.update("CLIENT", request);

        assertEquals("Nuevo Nombre", result.roleName());
        assertEquals("Nueva desc", result.description());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void update_partialUpdate_onlyName() {
        when(roleRepository.findById("CLIENT")).thenReturn(Optional.of(createRole()));
        var request = new Requests.UpdateRole("Solo Nombre", null, null);

        var result = roleService.update("CLIENT", request);

        assertEquals("Solo Nombre", result.roleName());
        assertEquals("Rol de cliente", result.description());
    }

    @Test
    void update_notFound_throwsException() {
        when(roleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());
        var request = new Requests.UpdateRole("Name", null, null);

        assertThrows(ApiException.RoleNotFound.class, () -> roleService.update("UNKNOWN", request));
    }

    @Test
    void delete_success() {
        when(roleRepository.findById("CLIENT")).thenReturn(Optional.of(createRole()));

        roleService.delete("CLIENT");

        verify(roleRepository).delete("CLIENT");
    }

    @Test
    void delete_notFound_throwsException() {
        when(roleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ApiException.RoleNotFound.class, () -> roleService.delete("UNKNOWN"));
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void findById_success() {
        when(roleRepository.findById("CLIENT")).thenReturn(Optional.of(createRole()));

        var result = roleService.findById("CLIENT");

        assertEquals("CLIENT", result.roleId());
        assertEquals("Cliente", result.roleName());
    }

    @Test
    void findById_notFound_throwsException() {
        when(roleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ApiException.RoleNotFound.class, () -> roleService.findById("UNKNOWN"));
    }

    @Test
    void findAll_returnsRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(createRole()));

        var result = roleService.findAll();

        assertEquals(1, result.size());
        assertEquals("CLIENT", result.getFirst().roleId());
    }

    @Test
    void findAll_empty() {
        when(roleRepository.findAll()).thenReturn(List.of());

        assertTrue(roleService.findAll().isEmpty());
    }
}
