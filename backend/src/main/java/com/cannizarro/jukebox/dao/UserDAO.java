package com.cannizarro.jukebox.dao;
import com.cannizarro.jukebox.client.SpotifyClient;
import com.cannizarro.jukebox.config.dto.SpotifyTokenResponse;
import com.cannizarro.jukebox.config.dto.SpotifyUserDTO;
import com.cannizarro.jukebox.config.entity.User;
import com.cannizarro.jukebox.config.exception.JukeboxException;
import com.cannizarro.jukebox.config.repository.UserRepository;
import com.cannizarro.jukebox.config.security.UserContextHelper;
import com.cannizarro.jukebox.config.security.jwt.JWTGenerator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class UserDAO {

    private final UserRepository userRepository;
    private final JWTGenerator jwtGenerator;
    private final SpotifyClient spotifyClient;

    public UserDAO(UserRepository userRepository, JWTGenerator jwtGenerator, SpotifyClient spotifyClient){
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.spotifyClient = spotifyClient;
    }

    public Mono<Void> deleteUser(String userId){
        return userRepository.deleteUser(userId)
                .then();
    }

    public Mono<String> registerToken(SpotifyTokenResponse response, SpotifyUserDTO spotifyUser){
        return userRepository.getUser(spotifyUser.getUniqueIdentifier())
                .flatMap(user -> userRepository.save(UserContextHelper.buildUser(response, spotifyUser, user)))
                .switchIfEmpty(userRepository.save(UserContextHelper.buildUser(response, spotifyUser, User.builder().price((float)1).lastScan(Instant.now()).createTimeStamp(Instant.now()).build())))
                .map(user -> jwtGenerator.generateToken(UserContextHelper.buildAuthenticationToken(user)));
    }

    public Mono<User> updateUser(User user){
        return userRepository.updateItem(user);
    }

    public Mono<User> getUser(String username){
        return userRepository.getUser(username)
                .switchIfEmpty(Mono.error(new JukeboxException(String.format("No user found for %s", username))))
                .map(user -> {
                    if(!user.getOnline())
                        throw new JukeboxException("This restaurant is currently not accepting songs.");
                    return user;
                });
    }

    public Mono<User> getUserForCustomer(String username){
        return getUser(username)
                .flatMap(spotifyClient::updateAccessToken);
    }

    public Flux<User> getUsers(){
        return userRepository.getUsers();
    }

}
