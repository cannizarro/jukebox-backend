package com.cannizarro.jukebox.service;

import com.cannizarro.jukebox.dto.TransactionPageDTO;
import com.cannizarro.jukebox.dto.TransactionPageRequestDTO;
import com.cannizarro.jukebox.dto.UserDTO;
import reactor.core.publisher.Mono;

public interface RestaurantService {

    Mono<UserDTO> getUser();

    Mono<Void> pausePlayback(String deviceId);

    Mono<Void> resumePlayback(String deviceId);

    Mono<Void> skipNext(String deviceId);

    Mono<Void> skipPrevious(String deviceId);
    Mono<UserDTO> updateUser(UserDTO user);

    Mono<TransactionPageDTO> getTransactions(TransactionPageRequestDTO requestDTO);
}
