package com.cannizarro.jukebox.service.impl;

import com.cannizarro.jukebox.client.SpotifyClient;
import com.cannizarro.jukebox.config.exception.JukeboxException;
import com.cannizarro.jukebox.config.utils.DatabaseUtils;
import com.cannizarro.jukebox.constants.ErrorMessages;
import com.cannizarro.jukebox.dao.TransactionDAO;
import com.cannizarro.jukebox.dao.UserDAO;
import com.cannizarro.jukebox.dto.SearchDTO;
import com.cannizarro.jukebox.dto.StateDTO;
import com.cannizarro.jukebox.dto.TrackDTO;
import com.cannizarro.jukebox.dto.TransactionDTO;
import com.cannizarro.jukebox.entity.Transaction;
import com.cannizarro.jukebox.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.stream.LongStream;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final SpotifyClient spotifyClient;
    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;
    public CustomerServiceImpl(SpotifyClient spotifyClient, UserDAO userDAO, TransactionDAO transactionDAO) {
        this.spotifyClient = spotifyClient;
        this.userDAO = userDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public Mono<StateDTO> getState(String username) {

        return userDAO.getUserForCustomer(username)
                .flatMap(user ->
                    Mono.zip(
                        spotifyClient.getState(user.getBearerToken()),
                        transactionDAO.getTracksQueued(user),
                        (stateDTO, tracksDTO) -> {
                            stateDTO.setQueue(new ArrayList<>(tracksDTO.getQueue()));
                            stateDTO.setRestaurantName(user.getRestaurantName());
                            stateDTO.setSecondsQueued(
                                getSecondsQueued(
                                    tracksDTO.getQueue().stream().mapToLong(TrackDTO::getLength),
                                    tracksDTO.getQueue().isEmpty() || tracksDTO.getQueue().stream().anyMatch(trackDTO -> trackDTO.getLength() == null)
                                )
                            );
                            return stateDTO;
                    })
                    .defaultIfEmpty(new StateDTO(ErrorMessages.NO_PLAYBACK_EXCEPTION_FOR_CUSTOMER, user.getRestaurantName()))
                )
                .onErrorMap(throwable -> {
                    if(throwable instanceof JukeboxException)
                        return new ResponseStatusException(HttpStatus.GONE, throwable.getMessage(), throwable);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage(), throwable);
                });
    }

    @Override
    public Mono<SearchDTO> search(String search, int page, String username) {
        return userDAO.getUserForCustomer(username)
                .flatMap(user -> Mono.zip(
                    spotifyClient.search(search, page, user.getBearerToken()),
                    transactionDAO.getUnfulfilledTracks(user.getUsername()),
                    (searchResults, transactionPage) ->
                            new SearchDTO(
                                    searchResults.getQueue(),
                                    getSecondsQueued(
                                            transactionPage.items().stream().mapToLong(Transaction::getTrackLength),
                                            transactionPage.items().isEmpty() || transactionPage.items().stream().anyMatch(transaction -> transaction.getTrackLength() == null)
                                    )
                            )
                    )
                )
                .onErrorMap(throwable -> {
                    if(throwable instanceof JukeboxException)
                        return new ResponseStatusException(HttpStatus.GONE, throwable.getMessage(), throwable);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage(), throwable);
                });
    }

    @Override
    public Mono<TransactionDTO> createTransaction(String trackUri, String trackId, int trackLength, String username, String customerName) {
        return userDAO.getUserForCustomer(username)
                .flatMap(user -> spotifyClient.addToQ(trackUri, user.getBearerToken(), user))
                .flatMap(user -> {
                    Transaction transaction = Transaction.builder()
                            .transactionId(DatabaseUtils.generateRowId())
                            .userId(user.getUsername())
                            .fulfilled(false)
                            .trackId(trackId)
                            .trackLength(trackLength)
                            .customerName(customerName)
                            .price(user.getPrice())
                            .build();
                    return transactionDAO.save(transaction);
                }).map(transaction -> TransactionDAO.modelMapper.map(transaction, TransactionDTO.class))
                .onErrorMap(throwable -> {
                    if(throwable instanceof JukeboxException)
                        return new ResponseStatusException(HttpStatus.GONE, throwable.getMessage(), throwable);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage(), throwable);
                });
    }

    private Long getSecondsQueued(LongStream queueDTO, boolean isEmpty){
        return isEmpty ? 1L : queueDTO.sum();
    }
}
