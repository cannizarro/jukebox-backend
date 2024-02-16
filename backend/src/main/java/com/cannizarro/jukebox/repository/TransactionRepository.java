package com.cannizarro.jukebox.repository;

import com.cannizarro.jukebox.constants.JukeboxConstants;
import com.cannizarro.jukebox.dto.TransactionPageRequestDTO;
import com.cannizarro.jukebox.entity.Transaction;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TransactionRepository {
    private final DynamoDbEnhancedAsyncClient dynamoDbAsyncClient;
    private DynamoDbAsyncTable<Transaction> transactionTable;
    @Value("${dynamoDb.transaction-page-size}")
    private int transactionPageSize;

    public TransactionRepository(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
    }

    @PostConstruct
    private void init() {
        transactionTable = dynamoDbAsyncClient.table("transaction", TableSchema.fromBean(Transaction.class));
    }

    public Mono<Transaction> save(Transaction transaction) {
        return Mono.fromFuture(transactionTable.putItem(transaction)).thenReturn(transaction);
    }

    public Mono<Page<Transaction>> getUnfulfilledTransactions(String userId){

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(userId).build());

        return Mono.from(
                    transactionTable.query(
                        request -> request.queryConditional(queryConditional).filterExpression(getFulfilledExpression(false))
                )
                //TODO limit edge
                .limit(45)
        );
    }

    public Mono<List<String>> getUnfulfilledTrackIds(String username){
        return getUnfulfilledTransactions(username)
                .map(transactionPage -> transactionPage.items().stream().map(Transaction::getTrackId).toList());
    }

    public Mono<BatchWriteResult> batchUpdateItems(List<Transaction> transactions){
        if(transactions.isEmpty())
            return Mono.empty();
        WriteBatch.Builder<Transaction> batch = WriteBatch.builder(Transaction.class)
                .mappedTableResource(transactionTable);
        transactions.forEach(transaction -> batch.addPutItem(builder -> builder.item(transaction)));

        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(batch.build())
                .build();
        return Mono.fromFuture(dynamoDbAsyncClient.batchWriteItem(batchWriteItemEnhancedRequest));
    }

    public Mono<Page<Transaction>> getTransactions(String username, TransactionPageRequestDTO requestDTO){

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(username).build());

        QueryEnhancedRequest.Builder queryEnhancedRequestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .scanIndexForward(requestDTO.getAscending())
                .limit(transactionPageSize);
        if(requestDTO.getStartKey() != null && !JukeboxConstants.FIRST_KEY.equals(requestDTO.getStartKey())){
            queryEnhancedRequestBuilder.exclusiveStartKey(
                    Map.of(
                            "userId", AttributeValue.builder().s(username).build(),
                            "transactionId", AttributeValue.builder().s(requestDTO.getStartKey()).build()
                    )
            );
        }
        if(requestDTO.getFulfilled() != null){
            queryEnhancedRequestBuilder.filterExpression(getFulfilledExpression(requestDTO.getFulfilled()));
        }

        return Mono.from(transactionTable.query(queryEnhancedRequestBuilder.build()));
    }

    private Expression getFulfilledExpression(boolean fulfilled){
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":fulfilledValue", AttributeValue.builder().bool(fulfilled).build());

        return Expression.builder()
                .expression("fulfilled = :fulfilledValue")
                .expressionValues(expressionValues)
                .build();
    }
}
