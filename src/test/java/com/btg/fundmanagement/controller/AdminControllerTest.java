package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.RoleService;
import com.btg.fundmanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private UserService userService;
    @Mock private RoleService roleService;

    private AdminController adminController;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(userService, roleService);
    }

    @Test
    void findAllUsers_returnsUserList() {
        var user = new Responses.UserInfo("u1", "test@test.com", "Test", 500000, "EMAIL", "+57300", Set.of("CLIENT"));
        when(userService.findAll()).thenReturn(List.of(user));

        var result = adminController.findAllUsers();

        assertEquals(1, result.size());
        assertEquals("u1", result.getFirst().userId());
    }

    @Test
    void createRole_returnsRoleInfo() {
        var request = new Requests.CreateRole("MANAGER", "Manager", "Gestor",
                List.of(new Requests.PermissionEntry("/api/**", List.of("GET", "POST"))));
        var expected = new Responses.RoleInfo("MANAGER", "Manager", "Gestor",
                List.of(new Requests.PermissionEntry("/api/**", List.of("GET", "POST"))), "2024-01-01");
        when(roleService.create(request)).thenReturn(expected);

        var result = adminController.createRole(request);

        assertEquals("MANAGER", result.roleId());
        assertEquals("Manager", result.roleName());
    }

    @Test
    void updateRole_returnsUpdatedRoleInfo() {
        var request = new Requests.UpdateRole("Updated", "New desc", null);
        var expected = new Responses.RoleInfo("CLIENT", "Updated", "New desc",
                List.of(new Requests.PermissionEntry("/api/funds/**", List.of("GET"))), "2024-01-01");
        when(roleService.update("CLIENT", request)).thenReturn(expected);

        var result = adminController.updateRole("CLIENT", request);

        assertEquals("Updated", result.roleName());
        assertEquals("New desc", result.description());
    }

    @Test
    void deleteRole_callsService() {
        adminController.deleteRole("OLD_ROLE");

        verify(roleService).delete("OLD_ROLE");
    }

    @Test
    void findRole_returnsRoleInfo() {
        var expected = new Responses.RoleInfo("CLIENT", "Cliente", "Rol cliente",
                List.of(new Requests.PermissionEntry("/api/funds/**", List.of("GET"))), "2024-01-01");
        when(roleService.findById("CLIENT")).thenReturn(expected);

        var result = adminController.findRole("CLIENT");

        assertEquals("CLIENT", result.roleId());
    }

    @Test
    void findAllRoles_returnsRoleList() {
        var role = new Responses.RoleInfo("CLIENT", "Cliente", "Rol cliente",
                List.of(), "2024-01-01");
        when(roleService.findAll()).thenReturn(List.of(role));

        var result = adminController.findAllRoles();

        assertEquals(1, result.size());
    }

    @Test
    void findAllRoles_emptyList() {
        when(roleService.findAll()).thenReturn(List.of());

        assertTrue(adminController.findAllRoles().isEmpty());
    }
}
