package com.cannizarro.jukebox.config.webclient.config;

import com.cannizarro.jukebox.config.dto.SpotifyTokenRequest;
import com.cannizarro.jukebox.config.entity.User;
import com.cannizarro.jukebox.config.repository.UserRepository;
import com.cannizarro.jukebox.config.security.UserContextHelper;
import com.cannizarro.jukebox.config.webclient.SpotifyOAuthClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;


public abstract class SpotifyClientConfig{
    public final UserRepository userRepository;
    private final SpotifyOAuthClient spotifyOAuthClient;
    public WebClient webClient;
    @Value("${spotify.client_id}")
    private String spotifyClientId;
    @Value("${spotify.client.uri}")
    private String baseUri;

    public SpotifyClientConfig(UserRepository userRepository, SpotifyOAuthClient spotifyOAuthClient) {
        this.userRepository = userRepository;
        this.spotifyOAuthClient = spotifyOAuthClient;
    }

    @PostConstruct
    public void init(){
        webClient = WebClient.builder()
                .baseUrl(baseUri)
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(getAuthFilter());
                    exchangeFilterFunctions.add(WebClientCommonConfig.processRequest());
                    exchangeFilterFunctions.add(WebClientCommonConfig.processResponse());
                })
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ExchangeFilterFunction getAuthFilter() {
        return (request, next) -> UserContextHelper.getUser()
                .defaultIfEmpty(new User())
                .flatMap(this::updateAccessToken)
                .flatMap(userLambda -> {
                    if(StringUtils.hasText(userLambda.getUsername())){
                        ClientRequest filtered = ClientRequest.from(request)
                                .header("Authorization", userLambda.getBearerToken())
                                .build();
                        return next.exchange(filtered);
                    }
                    return next.exchange(request);
            }
        );
    }

    public Mono<User> updateAccessToken(User user){
        if(StringUtils.hasText(user.getUsername()) && Instant.now().isAfter(user.getUpdateTimeStamp().plusSeconds(user.getExpiresIn()))){
            SpotifyTokenRequest tokenRequest = SpotifyTokenRequest.builder()
                    .grant_type("refresh_token")
                    .refresh_token(user.getRefreshToken())
                    .client_id(spotifyClientId)
                    .build();
            return spotifyOAuthClient.getSpotifyAccessToken(tokenRequest)
                    .map(response -> UserContextHelper.updateUser(response, user))
                    .flatMap(userRepository::updateItem);
        }
        return Mono.just(user);
    }
}