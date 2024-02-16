package com.cannizarro.jukebox.controller;

import com.cannizarro.jukebox.dto.TransactionPageDTO;
import com.cannizarro.jukebox.dto.UserDTO;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import reactor.core.publisher.Mono;


public interface RestaurantAPI {

    @GetMapping(path = "/user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<UserDTO> getUser();

    @DeleteMapping(path="/logout")
    Mono<Void> logout(ServerHttpResponse response);

    @PutMapping(path = "/pause")
    Mono<Void> pausePlayback(String deviceId);

    @PutMapping(path = "/resume")
    Mono<Void> resumePlayback(String deviceId);

    @PostMapping(path = "/skipNext")
    Mono<Void> skipNext(String deviceId);

    @PostMapping(path = "/skipPrevious")
    Mono<Void> skipPrevious(String deviceId);

    @PutMapping(path = "/updateUser")
    Mono<UserDTO> updateUser(UserDTO user);

    @GetMapping(path = "/transactions")
    Mono<TransactionPageDTO> getTransactions(String startKey, Boolean fulfilled, Boolean ascending);
}
