package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.FundRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundService {

    private final FundRepository fundRepository;

    public FundService(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    public List<Responses.Fund> findAll() {
        return fundRepository.findAll().stream()
                .map(f -> new Responses.Fund(f.getFundId(), f.getName(), f.getMinimumAmount(), f.getCategory()))
                .toList();
    }

    public Responses.Fund findById(String fundId) {
        var fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new ApiException.FundNotFound(fundId));
        return new Responses.Fund(fund.getFundId(), fund.getName(), fund.getMinimumAmount(), fund.getCategory());
    }
}
