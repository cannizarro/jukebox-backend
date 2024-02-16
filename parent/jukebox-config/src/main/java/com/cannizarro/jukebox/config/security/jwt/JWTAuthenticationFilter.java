package com.cannizarro.jukebox.config.security.jwt;

import com.cannizarro.jukebox.config.constants.Constants;
import com.cannizarro.jukebox.config.entity.User;
import com.cannizarro.jukebox.config.security.UserContextHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class JWTAuthenticationFilter implements WebFilter {

    @Autowired
    private JWTGenerator tokenGenerator;
    @Autowired
    private ReactiveUserDetailsService customUserDetailsService;

    private String getJWTFromRequest(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst(Constants.USER_COOKIE_KEY);
        return cookie == null ? null : cookie.getValue();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Mono<User> userDetails = Mono.empty();
        String token = getJWTFromRequest(exchange.getRequest());
        if(StringUtils.hasText(token) && tokenGenerator.validateToken(token)) {
            String username = tokenGenerator.getUsernameFromJWT(token);
            userDetails = customUserDetailsService.findByUsername(username).map(user -> (User) user);
        }
        return userDetails
                .defaultIfEmpty(new User())
                .flatMap(val -> StringUtils.hasText(val.getUsername()) ?
                        chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(UserContextHelper.buildAuthenticationToken(val))) :
                        chain.filter(exchange));
    }
}
