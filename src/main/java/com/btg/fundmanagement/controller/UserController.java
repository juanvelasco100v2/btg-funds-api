package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    public Responses.UserInfo getProfile(Authentication auth) {
        return userService.getProfile(auth.getName());
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil del usuario autenticado")
    public Responses.UserInfo updateProfile(Authentication auth,
                                            @Valid @RequestBody Requests.UpdateProfile request) {
        return userService.updateProfile(auth.getName(), request);
    }
}
