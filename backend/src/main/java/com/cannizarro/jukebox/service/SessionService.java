package com.cannizarro.jukebox.service;



import org.springframework.http.ResponseCookie;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface SessionService {

    URI getSpotifyAuthorizationUri();

    Mono<ResponseCookie> tokenCallBack(String code, String state, String error);
}
