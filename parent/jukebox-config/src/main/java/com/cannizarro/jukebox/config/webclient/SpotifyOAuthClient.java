package com.cannizarro.jukebox.config.webclient;

import com.cannizarro.jukebox.config.dto.SpotifyTokenRequest;
import com.cannizarro.jukebox.config.dto.SpotifyTokenResponse;
import com.cannizarro.jukebox.config.webclient.config.WebClientCommonConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class SpotifyOAuthClient {

    @Value("${spotify.auth.client.uri}")
    private String baseUri;

    @Value("${spotify.client_id}")
    private String spotifyClientId;

    @Value("${spotify.client_secret}")
    private String spotifyClientSecret;

    private WebClient webClient;


    @PostConstruct
    public void init(){
        webClient = WebClient.builder()
                .baseUrl(baseUri)
                .filters(filters -> {
                    filters.add(ExchangeFilterFunctions.basicAuthentication(spotifyClientId, spotifyClientSecret));
                    filters.add(WebClientCommonConfig.processRequest());
                    filters.add(WebClientCommonConfig.processResponse());
                })
                .build();
    }

    public Mono<SpotifyTokenResponse> getSpotifyAccessToken(SpotifyTokenRequest request){
        MultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
        valueMap.setAll((new ObjectMapper()).convertValue(request, new TypeReference<>() {}));

        return webClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue( valueMap)
                .retrieve()
                .bodyToMono(SpotifyTokenResponse.class);
    }
}
