package com.cannizarro.jukebox.controller;

import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface SessionController {

    @GetMapping(path = "/login")
    Mono<Void> login(ServerHttpResponse response) throws IOException;

    @GetMapping(path="/registerUser")
    Mono<Void> registerUser(String code, String state, String error, ServerHttpResponse response);
}