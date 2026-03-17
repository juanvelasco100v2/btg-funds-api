package com.btg.fundmanagement.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class Transaction {

    private String transactionId;
    private String userId;
    private String fundId;
    private String fundName;
    private String type;
    private long amount;
    private String createdAt;

    @DynamoDbPartitionKey
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String id) { this.transactionId = id; }

    @DynamoDbSecondaryPartitionKey(indexNames = "userId-createdAt-index")
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFundId() { return fundId; }
    public void setFundId(String fundId) { this.fundId = fundId; }

    public String getFundName() { return fundName; }
    public void setFundName(String fundName) { this.fundName = fundName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    @DynamoDbSecondarySortKey(indexNames = "userId-createdAt-index")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
