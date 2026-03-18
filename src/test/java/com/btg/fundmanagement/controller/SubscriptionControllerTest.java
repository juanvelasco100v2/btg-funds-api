package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock private SubscriptionService subscriptionService;
    @Mock private Authentication authentication;

    private SubscriptionController subscriptionController;

    @BeforeEach
    void setUp() {
        subscriptionController = new SubscriptionController(subscriptionService);
        when(authentication.getName()).thenReturn("u1");
    }

    @Test
    void subscribe_returnsMessage() {
        var expected = new Responses.Message("Suscrito exitosamente al fondo FPV");
        when(subscriptionService.subscribe("u1", "f1")).thenReturn(expected);

        var result = subscriptionController.subscribe(authentication, "f1");

        assertTrue(result.message().contains("Suscrito exitosamente"));
    }

    @Test
    void cancel_returnsMessage() {
        var expected = new Responses.Message("Suscripcion cancelada al fondo FPV");
        when(subscriptionService.cancel("u1", "f1")).thenReturn(expected);

        var result = subscriptionController.cancel(authentication, "f1");

        assertTrue(result.message().contains("Suscripcion cancelada"));
    }

    @Test
    void findMySubscriptions_returnsList() {
        var sub = new Responses.SubscriptionInfo("u1", "f1", "FPV", 75000, "2024-01-01");
        when(subscriptionService.findByUser("u1")).thenReturn(List.of(sub));

        var result = subscriptionController.findMySubscriptions(authentication);

        assertEquals(1, result.size());
        assertEquals("u1", result.getFirst().userId());
        assertEquals("f1", result.getFirst().fundId());
    }

    @Test
    void findMySubscriptions_emptyList() {
        when(subscriptionService.findByUser("u1")).thenReturn(List.of());

        assertTrue(subscriptionController.findMySubscriptions(authentication).isEmpty());
    }
}
