package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.TransactionService;
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
class TransactionControllerTest {

    @Mock private TransactionService transactionService;
    @Mock private Authentication authentication;

    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        transactionController = new TransactionController(transactionService);
        when(authentication.getName()).thenReturn("u1");
    }

    @Test
    void findMyTransactions_returnsPage() {
        var tx = new Responses.TransactionInfo("tx1", "u1", "f1", "FPV", "SUBSCRIBE", 75000, "2024-01-01");
        var page = new Responses.Page<>(List.of(tx), 1, null);
        when(transactionService.findByUser("u1", 10, null)).thenReturn(page);

        var result = transactionController.findMyTransactions(authentication, 10, null);

        assertEquals(1, result.count());
        assertEquals("tx1", result.items().getFirst().transactionId());
        assertNull(result.lastKey());
    }

    @Test
    void findMyTransactions_withPagination() {
        var page = new Responses.Page<Responses.TransactionInfo>(List.of(), 0, null);
        when(transactionService.findByUser("u1", 5, "someKey")).thenReturn(page);

        var result = transactionController.findMyTransactions(authentication, 5, "someKey");

        assertEquals(0, result.count());
        assertTrue(result.items().isEmpty());
    }
}
