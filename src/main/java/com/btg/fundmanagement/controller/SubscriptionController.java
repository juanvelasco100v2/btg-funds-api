package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Suscripciones")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/{fundId}")
    @Operation(summary = "Suscribirse a un fondo")
    public Responses.Message subscribe(Authentication auth, @PathVariable String fundId) {
        return subscriptionService.subscribe(auth.getName(), fundId);
    }

    @DeleteMapping("/{fundId}")
    @Operation(summary = "Cancelar suscripción a un fondo")
    public Responses.Message cancel(Authentication auth, @PathVariable String fundId) {
        return subscriptionService.cancel(auth.getName(), fundId);
    }

    @GetMapping
    @Operation(summary = "Listar suscripciones del usuario")
    public List<Responses.SubscriptionInfo> findMySubscriptions(Authentication auth) {
        return subscriptionService.findByUser(auth.getName());
    }
}
