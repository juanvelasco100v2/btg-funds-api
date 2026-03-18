package com.btg.fundmanagement.service;

import com.btg.fundmanagement.entity.Transaction;
import com.btg.fundmanagement.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository);
    }

    private Transaction createTransaction() {
        var t = new Transaction();
        t.setTransactionId("tx1");
        t.setUserId("u1");
        t.setFundId("f1");
        t.setFundName("FPV_RECAUDADORA");
        t.setType("SUBSCRIBE");
        t.setAmount(75000);
        t.setCreatedAt("2024-01-01T00:00:00Z");
        return t;
    }

    @Test
    void findByUser_returnsPagedResults() {
        var tx = createTransaction();
        @SuppressWarnings("unchecked")
        Page<Transaction> page = mock(Page.class);
        when(page.items()).thenReturn(List.of(tx));
        when(page.lastEvaluatedKey()).thenReturn(null);
        when(transactionRepository.findByUserId(eq("u1"), eq(10), isNull())).thenReturn(page);

        var result = transactionService.findByUser("u1", 10, null);

        assertEquals(1, result.count());
        assertEquals("tx1", result.items().getFirst().transactionId());
        assertEquals("SUBSCRIBE", result.items().getFirst().type());
        assertNull(result.lastKey());
    }

    @Test
    void findByUser_withLastKey_encodesAndDecodes() {
        var tx = createTransaction();
        Map<String, AttributeValue> lastEvalKey = Map.of(
                "userId", AttributeValue.builder().s("u1").build(),
                "createdAt", AttributeValue.builder().s("2024-01-01").build()
        );
        @SuppressWarnings("unchecked")
        Page<Transaction> page = mock(Page.class);
        when(page.items()).thenReturn(List.of(tx));
        when(page.lastEvaluatedKey()).thenReturn(lastEvalKey);
        when(transactionRepository.findByUserId(eq("u1"), eq(10), isNull())).thenReturn(page);

        var result = transactionService.findByUser("u1", 10, null);

        assertNotNull(result.lastKey());
        assertFalse(result.lastKey().isBlank());
    }

    @Test
    void findByUser_nullPage_returnsEmptyPage() {
        when(transactionRepository.findByUserId(eq("u1"), eq(10), isNull())).thenReturn(null);

        var result = transactionService.findByUser("u1", 10, null);

        assertEquals(0, result.count());
        assertTrue(result.items().isEmpty());
        assertNull(result.lastKey());
    }

    @Test
    void findByUser_emptyItems_returnsZeroCount() {
        @SuppressWarnings("unchecked")
        Page<Transaction> page = mock(Page.class);
        when(page.items()).thenReturn(List.of());
        when(page.lastEvaluatedKey()).thenReturn(null);
        when(transactionRepository.findByUserId(eq("u1"), eq(10), isNull())).thenReturn(page);

        var result = transactionService.findByUser("u1", 10, null);

        assertEquals(0, result.count());
        assertTrue(result.items().isEmpty());
    }

    @Test
    void findByUser_invalidLastKey_treatedAsNull() {
        var tx = createTransaction();
        @SuppressWarnings("unchecked")
        Page<Transaction> page = mock(Page.class);
        when(page.items()).thenReturn(List.of(tx));
        when(page.lastEvaluatedKey()).thenReturn(null);
        when(transactionRepository.findByUserId(eq("u1"), eq(10), isNull())).thenReturn(page);

        var result = transactionService.findByUser("u1", 10, "invalid-base64!!!");

        assertEquals(1, result.count());
    }

    @Test
    void findByUser_mapsAllFields() {
        var tx = createTransaction();
        @SuppressWarnings("unchecked")
        Page<Transaction> page = mock(Page.class);
        when(page.items()).thenReturn(List.of(tx));
        when(page.lastEvaluatedKey()).thenReturn(null);
        when(transactionRepository.findByUserId(eq("u1"), eq(10), isNull())).thenReturn(page);

        var result = transactionService.findByUser("u1", 10, null);
        var item = result.items().getFirst();

        assertEquals("tx1", item.transactionId());
        assertEquals("u1", item.userId());
        assertEquals("f1", item.fundId());
        assertEquals("FPV_RECAUDADORA", item.fundName());
        assertEquals("SUBSCRIBE", item.type());
        assertEquals(75000, item.amount());
        assertEquals("2024-01-01T00:00:00Z", item.createdAt());
    }
}
