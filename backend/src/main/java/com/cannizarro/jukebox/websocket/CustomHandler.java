package com.cannizarro.jukebox.websocket;

import com.cannizarro.jukebox.client.SpotifyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class CustomHandler implements WebSocketHandler {

    private final SpotifyClient spotifyClient;

    public CustomHandler(SpotifyClient spotifyClient){
        this.spotifyClient = spotifyClient;
    }
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<WebSocketMessage> output = session.receive()
                .flatMap(message -> {
                    if("state".equals(message.getPayloadAsText()))
                        return spotifyClient.getStateString();
                    return Mono.empty();
                })
                .map(session::textMessage);

        return session.send(output);
    }


}
