package com.btg.fundmanagement.repository;

import com.btg.fundmanagement.entity.Fund;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.util.List;
import java.util.Optional;

@Repository
public class FundRepository {

    private final DynamoDbTable<Fund> table;

    public FundRepository(DynamoDbEnhancedClient client,
                          @Value("${aws.dynamodb.table.funds}") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(Fund.class));
    }

    public Optional<Fund> findById(String fundId) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(fundId).build()));
    }

    public List<Fund> findAll() {
        return table.scan().items().stream().toList();
    }
}
