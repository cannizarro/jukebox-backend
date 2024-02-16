package com.cannizarro.jukebox.controller.impl;

import com.cannizarro.jukebox.controller.SessionController;
import com.cannizarro.jukebox.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "public")
public class SessionControllerImpl implements SessionController {

    private final SessionService sessionService;

    public SessionControllerImpl(SessionService sessionService){
        this.sessionService = sessionService;
    }

    @Override
    public Mono<Void> login(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().setLocation(sessionService.getSpotifyAuthorizationUri());
        return response.setComplete();
    }

    @Override
    public Mono<Void> registerUser(@RequestParam(required = false) String code, @RequestParam String state,
                                   @RequestParam(required = false) String error, ServerHttpResponse response) {
        return sessionService.tokenCallBack(code, state, error)
                .flatMap(cookie -> {
                    response.addCookie(cookie);
                    return response.setComplete();
                });
    }


}
