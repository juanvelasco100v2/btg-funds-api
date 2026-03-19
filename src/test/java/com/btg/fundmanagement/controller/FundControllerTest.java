package com.btg.fundmanagement.controller;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.service.FundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundControllerTest {

    @Mock private FundService fundService;
    private FundController fundController;

    @BeforeEach
    void setUp() {
        fundController = new FundController(fundService);
    }

    @Test
    void findAll_returnsFundList() {
        var fund = new Responses.Fund("f1", "FPV_RECAUDADORA", 75000, "FPV");
        when(fundService.findAll()).thenReturn(List.of(fund));

        var result = fundController.findAll();

        assertEquals(1, result.size());
        assertEquals("f1", result.getFirst().fundId());
    }

    @Test
    void findAll_emptyList() {
        when(fundService.findAll()).thenReturn(List.of());

        assertTrue(fundController.findAll().isEmpty());
    }

    @Test
    void findById_returnsFund() {
        var fund = new Responses.Fund("f1", "FPV_RECAUDADORA", 75000, "FPV");
        when(fundService.findById("f1")).thenReturn(fund);

        var result = fundController.findById("f1");

        assertEquals("f1", result.fundId());
        assertEquals("FPV_RECAUDADORA", result.name());
        assertEquals(75000, result.minimumAmount());
        assertEquals("FPV", result.category());
    }
}
