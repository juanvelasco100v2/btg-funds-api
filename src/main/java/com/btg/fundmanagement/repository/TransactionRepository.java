package com.btg.fundmanagement.repository;

import com.btg.fundmanagement.entity.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

@Repository
public class TransactionRepository {

    private final DynamoDbTable<Transaction> table;
    private final DynamoDbIndex<Transaction> userIdIndex;

    public TransactionRepository(DynamoDbEnhancedClient client,
                                 @Value("${aws.dynamodb.table.transactions}") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(Transaction.class));
        this.userIdIndex = table.index("userId-createdAt-index");
    }

    public void save(Transaction transaction) {
        table.putItem(transaction);
    }

    public Page<Transaction> findByUserId(String userId, int limit, Map<String, AttributeValue> exclusiveStartKey) {
        var condition = QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build());
        var requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(condition)
                .scanIndexForward(false)
                .limit(limit);

        if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
            requestBuilder.exclusiveStartKey(exclusiveStartKey);
        }

        return userIdIndex.query(requestBuilder.build()).stream().findFirst().orElse(null);
    }
}
