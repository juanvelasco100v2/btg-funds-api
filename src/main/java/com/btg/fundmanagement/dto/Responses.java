package com.btg.fundmanagement.dto;

import java.util.List;
import java.util.Set;

public final class Responses {
    private Responses() {}

    public record Auth(String token, String userId, String email, Set<String> roles) {}

    public record Fund(String fundId, String name, long minimumAmount, String category) {}

    public record SubscriptionInfo(
            String userId, String fundId, String fundName,
            long amount, String subscribedAt
    ) {}

    public record TransactionInfo(
            String transactionId, String userId, String fundId,
            String fundName, String type, long amount, String createdAt
    ) {}

    public record UserInfo(
            String userId, String email, String name, long balance,
            String notificationPreference, String phone, Set<String> roleIds
    ) {}

    public record RoleInfo(
            String roleId, String roleName, String description,
            List<Requests.PermissionEntry> permissions, String createdAt
    ) {}

    public record Page<T>(List<T> items, int count, String lastKey) {}

    public record Error(String message, int status, String timestamp) {}

    public record Message(String message) {}
}
