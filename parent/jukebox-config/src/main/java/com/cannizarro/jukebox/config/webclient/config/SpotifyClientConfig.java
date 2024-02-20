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
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.time.Instant;


public abstract class SpotifyClientConfig{
    public final UserRepository userRepository;
    private final SpotifyOAuthClient spotifyOAuthClient;
    private final ReactorClientHttpConnector clientHttpConnector;
    public WebClient webClient;
    @Value("${spotify.client_id}")
    private String spotifyClientId;
    @Value("${spotify.client.uri}")
    private String baseUri;

    public SpotifyClientConfig(UserRepository userRepository, SpotifyOAuthClient spotifyOAuthClient, ReactorClientHttpConnector clientHttpConnector) {
        this.userRepository = userRepository;
        this.spotifyOAuthClient = spotifyOAuthClient;
        this.clientHttpConnector = clientHttpConnector;
    }

    @PostConstruct
    public void init(){


        webClient = WebClient.builder()
                .clientConnector(clientHttpConnector)
                .baseUrl(baseUri)
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(getAuthFilter());
                    exchangeFilterFunctions.add(WebClientUtils.processRequest());
                    exchangeFilterFunctions.add(WebClientUtils.processResponse());
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