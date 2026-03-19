package com.btg.fundmanagement.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.Set;

@DynamoDbBean
public class User {

    private String userId;
    private String email;
    private String name;
    private String password;
    private long balance;
    private String notificationPreference;
    private String phone;
    private Set<String> roleIds;
    private String createdAt;

    @DynamoDbPartitionKey
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSecondaryPartitionKey(indexNames = "email-index")
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }

    public String getNotificationPreference() { return notificationPreference; }
    public void setNotificationPreference(String pref) { this.notificationPreference = pref; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Set<String> getRoleIds() { return roleIds; }
    public void setRoleIds(Set<String> roleIds) { this.roleIds = roleIds; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
