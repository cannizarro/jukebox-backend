package com.cannizarro.jukebox.scheduler;

import com.cannizarro.jukebox.client.SpotifyClient;
import com.cannizarro.jukebox.dao.TransactionDAO;
import com.cannizarro.jukebox.dao.UserDAO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

@Configuration
@Slf4j
public class ScheduledTasks {

    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;
    private final SpotifyClient spotifyClient;
    @Value("${spotify.recently-played.limit}")
    private int recentlyPlayedLimit;
    @Value("${scan-interval-minutes}")
    private int scanInterval;

    public ScheduledTasks(UserDAO userDAO, TransactionDAO transactionDAO, SpotifyClient spotifyClient) {
        this.userDAO = userDAO;
        this.transactionDAO = transactionDAO;
        this.spotifyClient = spotifyClient;
    }


    @PostConstruct
    public void init(){
        Flux.interval(Duration.ofSeconds(1), Duration.ofMinutes(scanInterval))
                .onBackpressureDrop()
                .flatMap(val -> userDAO.getUsers())
                .flatMap(spotifyClient::updateAccessToken)
                .flatMap(user ->
                        {
                            log.info("Starting update job for user: {}", user.getUsername());
                            return Mono.zip(
                                    transactionDAO.getUnfulfilledTracks(user.getUsername()),
                                    spotifyClient.getRecentlyPlayed(user.getLastScan().toEpochMilli(), user.getBearerToken()),
                                    ((transactionPage, recentlyPlayedDTO) -> {
                                        // there's no total returned by spotify api
//                                        if (recentlyPlayedDTO.getTotal() > recentlyPlayedLimit) {
//                                            //TODO take care of the limits and edge cases
//                                            log.error("Recently played DTO size is {} which is bigger than limit of {}.\nFor user {}.", recentlyPlayedDTO.getTotal(), recentlyPlayedLimit, user.getUsername());
//                                        }
                                        List<String> recentlyPlayedIds = recentlyPlayedDTO.getItems().stream().map(item -> item.getTrack().getId()).toList();
                                        return transactionPage.items().stream()
                                                .filter(transaction -> recentlyPlayedIds.contains(transaction.getTrackId()))
                                                .peek(transaction -> transaction.setFulfilled(true))
                                                .toList();
                                    })
                            ).map(transactionDAO::batchUpdateItems)
                            .onErrorResume(throwable -> {
                                log.error("Error occurred while running scheduled task for user: {}.", user.getUsername(), throwable);
                                return Mono.empty();
                            });
                        }
                )
                .onErrorResume(throwable -> {
                    log.error("Some error occurred in scheduler while fetching user.", throwable);
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
