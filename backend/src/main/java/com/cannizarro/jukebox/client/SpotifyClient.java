package com.cannizarro.jukebox.client;

import com.cannizarro.jukebox.config.dto.SpotifyUserDTO;
import com.cannizarro.jukebox.config.entity.User;
import com.cannizarro.jukebox.config.exception.JukeboxException;
import com.cannizarro.jukebox.config.repository.UserRepository;
import com.cannizarro.jukebox.config.webclient.SpotifyOAuthClient;
import com.cannizarro.jukebox.config.webclient.config.SpotifyClientConfig;
import com.cannizarro.jukebox.constants.ErrorMessages;
import com.cannizarro.jukebox.constants.JukeboxConstants;
import com.cannizarro.jukebox.dto.QueueDTO;
import com.cannizarro.jukebox.dto.RecentlyPlayedDTO;
import com.cannizarro.jukebox.dto.StateDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SpotifyClient extends SpotifyClientConfig {

    @Value("${spotify.recently-played.limit}")
    private int recentlyPlayedLimit;

    public SpotifyClient(UserRepository userRepository, SpotifyOAuthClient spotifyOAuthClient) {
        super(userRepository, spotifyOAuthClient);
    }

    public Mono<SpotifyUserDTO> getUser(String token){
        return webClient.get()
                .uri("/me")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(SpotifyUserDTO.class);
    }

    public Mono<StateDTO> getState(){
        return webClient.get()
                .uri("/me/player")
                .retrieve()
                .bodyToMono(StateDTO.class);
    }

    public Mono<StateDTO> getState(String token){
        return webClient.get()
                .uri("/me/player")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(StateDTO.class);
    }

    public Mono<QueueDTO> getQueue(){
        return webClient.get()
                .uri("/me/player/queue")
                .retrieve()
                .bodyToMono(QueueDTO.class);
    }

    public Mono<String> getStateString(){
        return Mono.zip(getState(), getQueue(),
                (state, queue) -> {
                    state.setQueue(queue.getQueue());
                    return state;
                })
                .defaultIfEmpty(new StateDTO(ErrorMessages.NO_PLAYBACK_EXCEPTION))
                .onErrorResume(throwable -> {
                    log.error("Exception occurred while fetching state:", throwable);
                    return Mono.just(
                            throwable.getCause() instanceof JukeboxException ?
                                    new StateDTO(throwable.getCause().getMessage()) :
                                    new StateDTO(throwable.getMessage())
                    );
                })
                .map(state -> {
                    try {
                        return new ObjectMapper().writeValueAsString(state);
                    } catch (JsonProcessingException e) {
                        throw new JukeboxException(String.format("Error occurred while serializing: %s", e.getMessage()), e);
                    }
                });
    }

    public Mono<Void> pausePlayback(String deviceId){
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/pause")
                        .queryParam(JukeboxConstants.QUERY_PARAM_DEVICE_ID, deviceId)
                        .build())
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> resumePlayback(String deviceId){
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/play")
                        .queryParam(JukeboxConstants.QUERY_PARAM_DEVICE_ID, deviceId)
                        .build())
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> skipNext(String deviceId){
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/next")
                        .queryParam(JukeboxConstants.QUERY_PARAM_DEVICE_ID, deviceId)
                        .build())
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> skipPrevious(String deviceId){
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/previous")
                        .queryParam(JukeboxConstants.QUERY_PARAM_DEVICE_ID, deviceId)
                        .build())
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<QueueDTO> getTracks(List<String> tracks, String token){
        return tracks.isEmpty() ?
                Mono.just(new QueueDTO(new ArrayList<>())) :
                webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tracks")
                        .queryParam("ids", String.join(",", tracks))
                        .build())
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(QueueDTO.class);
    }

    public Mono<QueueDTO> search(String searchString, int offset, String token){
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", searchString)
                        .queryParam("type", "track")
                        .queryParam("limit", 10)
                        .queryParam("offset", offset)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(QueueDTO.class);
    }

    public Mono<User> addToQ(String trackUri, String token, User user){
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/queue")
                        .queryParam("uri", trackUri)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve().bodyToMono(Void.class)
                .thenReturn(user);
    }

    public Mono<RecentlyPlayedDTO> getRecentlyPlayed(Long time, String token){
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/recently-played")
                        .queryParam("after", time)
                        .queryParam("limit", recentlyPlayedLimit)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(RecentlyPlayedDTO.class);
    }

}
