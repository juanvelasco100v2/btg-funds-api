package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setup")
@Tag(name = "Setup inicial")
public class SetupController {

    private final AuthService authService;

    public SetupController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario administrador inicial (solo funciona si no existe ningún admin)")
    public Responses.Auth setupAdmin(@Valid @RequestBody Requests.SetupAdmin request) {
        return authService.setupAdmin(request);
    }
}
