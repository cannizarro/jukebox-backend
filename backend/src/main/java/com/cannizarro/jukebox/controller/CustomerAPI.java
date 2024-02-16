package com.cannizarro.jukebox.controller;

import com.cannizarro.jukebox.dto.SearchDTO;
import com.cannizarro.jukebox.dto.StateDTO;
import com.cannizarro.jukebox.dto.TransactionDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

public interface CustomerAPI {

    @GetMapping(path = "/state", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<StateDTO> getCustomerState(String username);

    @GetMapping(path = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<SearchDTO> search(String search, int page, String username);

    @PostMapping(path = "/createTransaction", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<TransactionDTO> createTransaction(String trackUri, String trackId, int trackLength, String username, String customerName);
}
