package com.btg.fundmanagement.repository;

import com.btg.fundmanagement.entity.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;

@Repository
public class SubscriptionRepository {

    private final DynamoDbTable<Subscription> table;

    public SubscriptionRepository(DynamoDbEnhancedClient client,
                                  @Value("${aws.dynamodb.table.subscriptions}") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(Subscription.class));
    }

    public void save(Subscription subscription) {
        table.putItem(subscription);
    }

    public Optional<Subscription> findByUserAndFund(String userId, String fundId) {
        return Optional.ofNullable(table.getItem(
                Key.builder().partitionValue(userId).sortValue(fundId).build()));
    }

    public List<Subscription> findByUserId(String userId) {
        var condition = QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build());
        return table.query(condition).items().stream().toList();
    }

    public void delete(String userId, String fundId) {
        table.deleteItem(Key.builder().partitionValue(userId).sortValue(fundId).build());
    }
}
