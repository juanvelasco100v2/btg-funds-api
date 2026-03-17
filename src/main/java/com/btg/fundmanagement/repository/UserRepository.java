package com.btg.fundmanagement.repository;

import com.btg.fundmanagement.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final DynamoDbTable<User> table;
    private final DynamoDbIndex<User> emailIndex;

    public UserRepository(DynamoDbEnhancedClient client,
                          @Value("${aws.dynamodb.table.users}") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(User.class));
        this.emailIndex = table.index("email-index");
    }

    public void save(User user) {
        table.putItem(user);
    }

    public Optional<User> findById(String userId) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(userId).build()));
    }

    public Optional<User> findByEmail(String email) {
        var condition = QueryConditional.keyEqualTo(Key.builder().partitionValue(email).build());
        return emailIndex.query(condition).stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }

    public List<User> findAll() {
        return table.scan().items().stream().toList();
    }

    public boolean hasAnyAdmin() {
        return table.scan().items().stream()
                .anyMatch(u -> u.getRoleIds() != null && u.getRoleIds().contains("ADMIN"));
    }
}
