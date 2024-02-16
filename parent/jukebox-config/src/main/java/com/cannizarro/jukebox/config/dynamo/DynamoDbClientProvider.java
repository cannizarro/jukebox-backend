package com.cannizarro.jukebox.config.dynamo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;

@Configuration
public class DynamoDbClientProvider {

    @Value("${dynamoDb.key}")
    private String key;

    @Value("${dynamoDb.secret}")
    private String secret;

    @Value("${dynamodb.url}")
    private String dynamoDbUrl;

    @Bean
    public DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient(){
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(
                        DynamoDbAsyncClient.builder()
                                .endpointOverride(URI.create(dynamoDbUrl))
                                // The region is meaningless for local DynamoDb but required for client builder validation
                                .region(Region.AP_SOUTH_1)
                                .credentialsProvider(StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(key, secret)))
                                .build()
                )
                .build();
    }

}
