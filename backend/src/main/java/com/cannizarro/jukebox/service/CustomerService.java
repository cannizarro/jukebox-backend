package com.cannizarro.jukebox.service;

import com.cannizarro.jukebox.dto.SearchDTO;
import com.cannizarro.jukebox.dto.StateDTO;
import com.cannizarro.jukebox.dto.TransactionDTO;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<StateDTO> getState(String username);

    Mono<SearchDTO> search(String search, int page, String username);

    Mono<TransactionDTO> createTransaction(String trackUri, String trackId, int trackLength, String username, String customerName);

}
