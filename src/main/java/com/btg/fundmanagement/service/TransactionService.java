package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.repository.TransactionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Responses.Page<Responses.TransactionInfo> findByUser(String userId, int limit, String lastKey) {
        var exclusiveStartKey = decodeLastKey(lastKey);
        var page = transactionRepository.findByUserId(userId, limit, exclusiveStartKey);

        if (page == null) {
            return new Responses.Page<>(java.util.List.of(), 0, null);
        }

        var items = page.items().stream()
                .map(t -> new Responses.TransactionInfo(
                        t.getTransactionId(), t.getUserId(), t.getFundId(),
                        t.getFundName(), t.getType(), t.getAmount(), t.getCreatedAt()))
                .toList();

        var nextKey = encodeLastKey(page.lastEvaluatedKey());
        return new Responses.Page<>(items, items.size(), nextKey);
    }

    private String encodeLastKey(Map<String, AttributeValue> lastKey) {
        if (lastKey == null || lastKey.isEmpty()) return null;
        try {
            var simplified = lastKey.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().s()));
            var json = objectMapper.writeValueAsString(simplified);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes());
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, AttributeValue> decodeLastKey(String encoded) {
        if (encoded == null || encoded.isBlank()) return null;
        try {
            var json = new String(Base64.getUrlDecoder().decode(encoded));
            Map<String, String> simplified = objectMapper.readValue(json, new TypeReference<>() {});
            return simplified.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            e -> AttributeValue.builder().s(e.getValue()).build()));
        } catch (Exception e) {
            return null;
        }
    }
}
