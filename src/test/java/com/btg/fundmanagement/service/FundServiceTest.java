package com.btg.fundmanagement.service;

import com.btg.fundmanagement.entity.Fund;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.FundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundServiceTest {

    @Mock private FundRepository fundRepository;
    private FundService fundService;

    @BeforeEach
    void setUp() {
        fundService = new FundService(fundRepository);
    }

    private Fund createFund() {
        var fund = new Fund();
        fund.setFundId("1");
        fund.setName("FPV_RECAUDADORA");
        fund.setMinimumAmount(75000);
        fund.setCategory("FPV");
        return fund;
    }

    @Test
    void findAll_returnsMappedFunds() {
        when(fundRepository.findAll()).thenReturn(List.of(createFund()));
        var result = fundService.findAll();
        assertEquals(1, result.size());
        assertEquals("1", result.getFirst().fundId());
        assertEquals("FPV_RECAUDADORA", result.getFirst().name());
        assertEquals(75000, result.getFirst().minimumAmount());
    }

    @Test
    void findAll_emptyList() {
        when(fundRepository.findAll()).thenReturn(List.of());
        assertTrue(fundService.findAll().isEmpty());
    }

    @Test
    void findById_success() {
        when(fundRepository.findById("1")).thenReturn(Optional.of(createFund()));
        var result = fundService.findById("1");
        assertEquals("1", result.fundId());
        assertEquals("FPV", result.category());
    }

    @Test
    void findById_notFound_throwsException() {
        when(fundRepository.findById("99")).thenReturn(Optional.empty());
        assertThrows(ApiException.FundNotFound.class, () -> fundService.findById("99"));
    }
}
