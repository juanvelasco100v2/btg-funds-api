package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Transacciones")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Historial de transacciones del usuario (paginado)")
    public Responses.Page<Responses.TransactionInfo> findMyTransactions(
            Authentication auth,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String lastKey) {
        return transactionService.findByUser(auth.getName(), limit, lastKey);
    }
}
