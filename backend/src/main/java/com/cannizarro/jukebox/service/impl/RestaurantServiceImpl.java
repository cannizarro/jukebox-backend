package com.cannizarro.jukebox.service.impl;

import com.cannizarro.jukebox.client.SpotifyClient;
import com.cannizarro.jukebox.config.dto.SpotifyErrorDTO;
import com.cannizarro.jukebox.config.security.UserContextHelper;
import com.cannizarro.jukebox.constants.ErrorMessages;
import com.cannizarro.jukebox.constants.JukeboxConstants;
import com.cannizarro.jukebox.dao.TransactionDAO;
import com.cannizarro.jukebox.dao.UserDAO;
import com.cannizarro.jukebox.dto.TransactionDTO;
import com.cannizarro.jukebox.dto.TransactionPageDTO;
import com.cannizarro.jukebox.dto.TransactionPageRequestDTO;
import com.cannizarro.jukebox.dto.UserDTO;
import com.cannizarro.jukebox.service.RestaurantService;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final UserDAO userDAO;
    private final SpotifyClient spotifyClient;
    private final TransactionDAO transactionDAO;

    public RestaurantServiceImpl(UserDAO userDAO, SpotifyClient spotifyClient, TransactionDAO transactionDAO) {
        this.spotifyClient = spotifyClient;
        this.userDAO = userDAO;
        this.transactionDAO = transactionDAO;
    }


    @Override
    public Mono<UserDTO> getUser() {
        return UserContextHelper.getUser()
                .map(user -> TransactionDAO.modelMapper.map(user, UserDTO.class));
    }

    @Override
    public Mono<Void> pausePlayback(String deviceId) {
        return spotifyClient.pausePlayback(deviceId)
                .onErrorMap(getSpotifyErrorHandler());
    }

    @Override
    public Mono<Void> resumePlayback(String deviceId) {
        return spotifyClient.resumePlayback(deviceId)
                .onErrorMap(getSpotifyErrorHandler());
    }

    @Override
    public Mono<Void> skipNext(String deviceId) {
        return spotifyClient.skipNext(deviceId)
                .onErrorMap(getSpotifyErrorHandler());
    }

    @Override
    public Mono<Void> skipPrevious(String deviceId) {
        return spotifyClient.skipPrevious(deviceId)
                .onErrorMap(getSpotifyErrorHandler());
    }

    @Override
    public Mono<UserDTO> updateUser(UserDTO userDTO) {
        return UserContextHelper.getUser()
                .flatMap(user -> {
                    TransactionDAO.modelMapper.map(userDTO, user);
                    return userDAO.updateUser(user);
                })
                .map(user -> {
                    TransactionDAO.modelMapper.map(user, userDTO);
                    return userDTO;
                });
    }

    private Function<Throwable, Throwable> getSpotifyErrorHandler(){
        return throwable -> {
            if(throwable instanceof WebClientResponseException){
                SpotifyErrorDTO error = ((WebClientResponseException) throwable).getResponseBodyAs(SpotifyErrorDTO.class);

                return switch (Objects.requireNonNull(error).getError().getStatus()) {
                    case 404, 502-> new ResponseStatusException(HttpStatus.GONE, error.getError().getMessage());

                    case 403 -> new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorMessages.SPOTIFY_ACTION_NOT_ALLOWED);

                    default -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error.getError().getMessage());
                };
            }
            return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
        };
    }

    @Override
    public Mono<TransactionPageDTO> getTransactions(TransactionPageRequestDTO requestDTO) {
        return UserContextHelper.getUser()
                .flatMap(user -> transactionDAO.getTransactions(user.getUsername(), requestDTO))
                .map(transactions ->
                        TransactionPageDTO.builder()
                                .transactions(TransactionDAO.modelMapper.map(transactions.items(), new TypeToken<List<TransactionDTO>>(){}.getType()))
                                .currentKey(requestDTO.getStartKey())
                                .nextKey(transactions.lastEvaluatedKey() == null ?
                                            JukeboxConstants.LAST_KEY :
                                            transactions.lastEvaluatedKey().get("transactionId").s())
                                .build()
                );
    }
}
