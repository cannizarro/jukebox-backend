package com.cannizarro.jukebox.controller.impl;

import com.cannizarro.jukebox.config.constants.Constants;
import com.cannizarro.jukebox.config.security.jwt.cookie.CookieBuilder;
import com.cannizarro.jukebox.controller.RestaurantAPI;
import com.cannizarro.jukebox.dto.StateDTO;
import com.cannizarro.jukebox.dto.TransactionPageDTO;
import com.cannizarro.jukebox.dto.TransactionPageRequestDTO;
import com.cannizarro.jukebox.dto.UserDTO;
import com.cannizarro.jukebox.service.RestaurantService;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "admin")
public class RestaurantAPIImpl implements RestaurantAPI {

    private final RestaurantService restaurantService;
    private final CookieBuilder cookieBuilder;

    public RestaurantAPIImpl(RestaurantService restaurantService, CookieBuilder cookieBuilder) {
        this.restaurantService = restaurantService;
        this.cookieBuilder = cookieBuilder;
    }

    @Override
    public Mono<UserDTO> getUser() {
        return restaurantService.getUser();
    }

    @Override
    public Mono<Void> logout(ServerHttpResponse response) {
        response.addCookie(cookieBuilder.getCookie(Constants.USER_COOKIE_KEY, Constants.INVALID_TOKEN));
        return response.setComplete();
    }

    @Override
    public Mono<Void> pausePlayback(@RequestParam String deviceId) {
        return restaurantService.pausePlayback(deviceId);
    }

    @Override
    public Mono<Void> resumePlayback(@RequestParam String deviceId) {
        return restaurantService.resumePlayback(deviceId);
    }

    @Override
    public Mono<Void> skipNext(@RequestParam String deviceId) {
        return restaurantService.skipNext(deviceId);
    }

    @Override
    public Mono<Void> skipPrevious(@RequestParam String deviceId) {
        return restaurantService.skipPrevious(deviceId);
    }

    @Override
    public Mono<UserDTO> updateUser(@RequestBody UserDTO user){
        return restaurantService.updateUser(user);
    }

    @Override
    public Mono<TransactionPageDTO> getTransactions(@RequestParam String startKey, @RequestParam(required = false) Boolean fulfilled, @RequestParam Boolean ascending) {
        return restaurantService.getTransactions(new TransactionPageRequestDTO(startKey, fulfilled, ascending));
    }

    @Override
    public Mono<StateDTO> getState() {
        return restaurantService.getState();
    }
}
