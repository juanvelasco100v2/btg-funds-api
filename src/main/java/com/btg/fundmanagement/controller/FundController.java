package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.FundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funds")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Fondos")
public class FundController {

    private final FundService fundService;

    public FundController(FundService fundService) {
        this.fundService = fundService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los fondos")
    public List<Responses.Fund> findAll() {
        return fundService.findAll();
    }

    @GetMapping("/{fundId}")
    @Operation(summary = "Obtener fondo por ID")
    public Responses.Fund findById(@PathVariable String fundId) {
        return fundService.findById(fundId);
    }
}
