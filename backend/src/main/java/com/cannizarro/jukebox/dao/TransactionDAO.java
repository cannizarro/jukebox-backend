package com.cannizarro.jukebox.dao;

import com.cannizarro.jukebox.client.SpotifyClient;
import com.cannizarro.jukebox.config.entity.User;
import com.cannizarro.jukebox.config.utils.DatabaseUtils;
import com.cannizarro.jukebox.dto.QueueDTO;
import com.cannizarro.jukebox.dto.TransactionDTO;
import com.cannizarro.jukebox.dto.TransactionPageRequestDTO;
import com.cannizarro.jukebox.entity.Transaction;
import com.cannizarro.jukebox.repository.TransactionRepository;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

@Service
public class TransactionDAO {
    private final TransactionRepository transactionRepository;
    private final SpotifyClient spotifyClient;
    public static final ModelMapper modelMapper = new ModelMapper();
    @Value("${spotify.open-url}")
    private String spotifyOpenUrl;

    public TransactionDAO(TransactionRepository transactionRepository, SpotifyClient spotifyClient) {
        this.transactionRepository = transactionRepository;
        this.spotifyClient = spotifyClient;
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        Converter<Transaction, TransactionDTO> converter = new AbstractConverter<>() {
            @Override
            protected TransactionDTO convert(Transaction transaction) {
                return TransactionDTO.builder()
                        .price(transaction.getPrice())
                        .createTimestamp(DatabaseUtils.getInstantFromUUID(transaction.getTransactionId()).toString())
                        .customerName(transaction.getCustomerName())
                        .fulfilled(transaction.isFulfilled())
                        .trackUrl(spotifyOpenUrl+transaction.getTrackId())
                        .transactionId(transaction.getTransactionId().toString())
                        .build();
            }
        };
        modelMapper.addConverter(converter);
    }

    public Mono<Transaction> save(Transaction transaction){
        return transactionRepository.save(transaction);
    }

    public Mono<QueueDTO> getTracksQueued(User user){
        return transactionRepository
                .getUnfulfilledTrackIds(user.getUsername())
                .flatMap(tracks -> spotifyClient.getTracks(tracks, user.getBearerToken()));
    }

    public Mono<Page<Transaction>> getUnfulfilledTracks(String username){
        return transactionRepository
                .getUnfulfilledTransactions(username);
    }

    public Mono<BatchWriteResult> batchUpdateItems(List<Transaction> transactions){
        return transactionRepository.batchUpdateItems(transactions);
    }

    public Mono<Page<Transaction>> getTransactions(String username, TransactionPageRequestDTO requestDTO){
        return transactionRepository.getTransactions(username, requestDTO);
    }

}
