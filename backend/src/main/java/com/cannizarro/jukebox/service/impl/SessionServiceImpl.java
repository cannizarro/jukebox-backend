package com.cannizarro.jukebox.service.impl;

import com.cannizarro.jukebox.client.SpotifyClient;
import com.cannizarro.jukebox.config.constants.Constants;
import com.cannizarro.jukebox.config.dto.SpotifyTokenRequest;
import com.cannizarro.jukebox.config.entity.User;
import com.cannizarro.jukebox.config.security.jwt.cookie.CookieBuilder;
import com.cannizarro.jukebox.config.webclient.SpotifyOAuthClient;
import com.cannizarro.jukebox.dao.UserDAO;
import com.cannizarro.jukebox.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SessionServiceImpl implements SessionService {

    @Value("${spotify.auth.uri}")
    private String spotifyAuthorizeUri;
    @Value("${spotify.client_id}")
    private String spotifyClientId;
    @Value("${restaurant.redirect}")
    private String restaurantUri;
    private final SpotifyOAuthClient spotifyOAuthClient;
    private final UserDAO userDAO;
    private final SpotifyClient spotifyClient;
    private final CookieBuilder cookieBuilder;
    private final Set<String> stateSet;

    public SessionServiceImpl(SpotifyOAuthClient spotifyOAuthClient, UserDAO userDAO, SpotifyClient spotifyClient, CookieBuilder cookieBuilder){
        this.spotifyOAuthClient = spotifyOAuthClient;
        this.userDAO = userDAO;
        this.spotifyClient = spotifyClient;
        this.cookieBuilder = cookieBuilder;
        stateSet = ConcurrentHashMap.newKeySet();
    }

    @Override
    public URI getSpotifyAuthorizationUri(){
        UriBuilder spotifyAUthorizeUriBuilder = UriComponentsBuilder.fromHttpUrl(spotifyAuthorizeUri);
        return spotifyAUthorizeUriBuilder
                .queryParam("client_id", spotifyClientId)
                .queryParam("state", generateState())
                .queryParam("redirect_uri", restaurantUri)
                .build();
    }

    @Override
    public Mono<ResponseCookie> tokenCallBack(String code, String state, String error) {
        return isStateValid(state)
                .then(spotifyOAuthClient.getSpotifyAccessToken(SpotifyTokenRequest.builder()
                        .grant_type("authorization_code")
                        .code(code)
                        .redirect_uri(restaurantUri)
                        .build()))
                .flatMap(token -> {
                    User temp = User.builder().accessToken(token.getAccessToken()).build();
                    return spotifyClient.getUser(temp.getBearerToken())
                            .flatMap(user -> userDAO.registerToken(token, user))
                            .map(cookieValue -> cookieBuilder.getCookie(Constants.USER_COOKIE_KEY, cookieValue));
                }).onErrorMap(exception -> {
                    log.error("Error occurred while authentication.");
                    if(!(exception instanceof ResponseStatusException))
                         return new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
                    return exception;
                });
    }

    private String generateState(){
        String unique = UUID.randomUUID().toString();
        stateSet.add(unique);
        return unique;
    }

    private Mono<Void> isStateValid(String state) {
        if(!stateSet.remove(state))
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid state received in redirect from spotify."));
        return Mono.empty();
    }

}
