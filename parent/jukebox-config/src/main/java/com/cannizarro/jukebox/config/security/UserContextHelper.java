package com.cannizarro.jukebox.config.security;

import com.cannizarro.jukebox.config.dto.SpotifyTokenResponse;
import com.cannizarro.jukebox.config.dto.SpotifyUserDTO;
import com.cannizarro.jukebox.config.entity.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

@UtilityClass
public class UserContextHelper {

    public static Mono<User> getUser(){
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> (User) context.getAuthentication().getPrincipal());
    }

    public static UsernamePasswordAuthenticationToken buildAuthenticationToken(User user){
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    public static User buildUser(SpotifyTokenResponse response, SpotifyUserDTO spotifyUser, User user){
        return User.builder()
                .username(spotifyUser.getUniqueIdentifier())
                .displayName(spotifyUser.getDisplayName())
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .expiresIn((long) response.getExpiresIn())
                .createTimeStamp(user.getCreateTimeStamp())
                .updateTimeStamp(Instant.now())
                .roles(Set.of("ROLE_ADMIN"))
                .online(true)
                .price(user.getPrice())
                .restaurantName(user.getRestaurantName())
                .lastScan(user.getLastScan())
                .build();
    }

    public static User updateUser(SpotifyTokenResponse response, User user){
        user.setAccessToken(response.getAccessToken());
        user.setExpiresIn((long) response.getExpiresIn());
        user.setUpdateTimeStamp(Instant.now());
        return user;
    }
}

