package com.cannizarro.jukebox.websocket;

import com.cannizarro.jukebox.client.SpotifyClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
class CustomMapper {

    private final SpotifyClient spotifyClient;

    CustomMapper(SpotifyClient spotifyClient) {
        this.spotifyClient = spotifyClient;
    }

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/state", new CustomHandler(spotifyClient));
        int order = -1; // before annotated controllers
        return new SimpleUrlHandlerMapping(map, order);
    }
}
