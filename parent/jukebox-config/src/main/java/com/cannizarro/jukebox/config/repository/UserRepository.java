package com.cannizarro.jukebox.config.repository;

import com.cannizarro.jukebox.config.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;

@Repository
@Slf4j
public class UserRepository {

    private final DynamoDbEnhancedAsyncClient dynamoDbAsyncClient;
    private DynamoDbAsyncTable<User> userCredTable;

    public UserRepository(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
    }

    @PostConstruct
    private void init(){
        userCredTable =  dynamoDbAsyncClient.table("user", TableSchema.fromBean(User.class));
    }

    public Mono<User> save(User user){
        return Mono.fromFuture(userCredTable.putItem(user)).thenReturn(user);
    }

    public Mono<User> getUser(String username){
        return Mono.fromFuture(userCredTable.getItem(Key.builder().partitionValue(username).build()));
    }

    public Mono<User> deleteUser(String userId){
        return Mono.fromFuture(userCredTable.deleteItem(
                DeleteItemEnhancedRequest.builder()
                        .key(Key.builder().partitionValue(userId).build())
                        .build()
                )
        );
    }

    public Mono<User> updateItem(User user){
        return Mono.fromFuture(userCredTable.updateItem(user));
    }

    public Flux<User> getUsers(){
        return Flux.from(userCredTable.scan().items())
                .onErrorResume(throwable -> {
                    log.error("Error occurred while fetching user", throwable);
                    return Mono.empty();
                });
    }
}
