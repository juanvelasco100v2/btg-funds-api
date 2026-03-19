package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.RoleService;
import com.btg.fundmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Administración")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    @Operation(summary = "Listar todos los usuarios")
    public List<Responses.UserInfo> findAllUsers() {
        return userService.findAll();
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un rol")
    public Responses.RoleInfo createRole(@Valid @RequestBody Requests.CreateRole request) {
        return roleService.create(request);
    }

    @PutMapping("/roles/{roleId}")
    @Operation(summary = "Actualizar un rol")
    public Responses.RoleInfo updateRole(@PathVariable String roleId,
                                         @Valid @RequestBody Requests.UpdateRole request) {
        return roleService.update(roleId, request);
    }

    @DeleteMapping("/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un rol")
    public void deleteRole(@PathVariable String roleId) {
        roleService.delete(roleId);
    }

    @GetMapping("/roles/{roleId}")
    @Operation(summary = "Obtener un rol por ID")
    public Responses.RoleInfo findRole(@PathVariable String roleId) {
        return roleService.findById(roleId);
    }

    @GetMapping("/roles")
    @Operation(summary = "Listar todos los roles")
    public List<Responses.RoleInfo> findAllRoles() {
        return roleService.findAll();
    }
}
