package com.btg.fundmanagement.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Fund {

    private String fundId;
    private String name;
    private long minimumAmount;
    private String category;

    @DynamoDbPartitionKey
    public String getFundId() { return fundId; }
    public void setFundId(String fundId) { this.fundId = fundId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getMinimumAmount() { return minimumAmount; }
    public void setMinimumAmount(long minimumAmount) { this.minimumAmount = minimumAmount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
