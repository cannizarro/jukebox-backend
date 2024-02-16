package com.cannizarro.jukebox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String transactionId;
    private String userId;
    private float price;
    private boolean fulfilled;
    private String customerName;
    private String trackId;
    private Integer trackLength;

    @DynamoDbSortKey
    public String getTransactionId() {
        return transactionId;
    }

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }
}
