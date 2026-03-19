package com.btg.fundmanagement.repository;

import com.btg.fundmanagement.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepository {

    private final DynamoDbTable<Role> table;

    public RoleRepository(DynamoDbEnhancedClient client,
                          @Value("${aws.dynamodb.table.roles}") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(Role.class));
    }

    public void save(Role role) {
        table.putItem(role);
    }

    public Optional<Role> findById(String roleId) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(roleId).build()));
    }

    public List<Role> findAll() {
        return table.scan().items().stream().toList();
    }

    public void delete(String roleId) {
        table.deleteItem(Key.builder().partitionValue(roleId).build());
    }
}
