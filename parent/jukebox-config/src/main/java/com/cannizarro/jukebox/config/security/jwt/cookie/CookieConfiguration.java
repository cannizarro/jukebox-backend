package com.cannizarro.jukebox.config.security.jwt.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@Configuration
public class CookieConfiguration {

    @Value("${jwt.max_age_days}")
    private int maxAgeInDays;

    @Value("${server.domain}")
    private String domain;

    @Bean
    @Profile({"dev"})
    public CookieBuilder cookieBuilderForDev(){
        return (key, value) ->
                    getCookie(key, value)
                        .sameSite(Cookie.SameSite.LAX.attributeValue())
                        .secure(false)
                        .build();
    }

    @Bean
    @Profile({"!dev"})
    public CookieBuilder cookieBuilder(){
        return (key, value) ->
                    getCookie(key, value)
                            .secure(true)
                            .sameSite(Cookie.SameSite.NONE.attributeValue())
                            .domain(domain)
                            .build();
    }

    private ResponseCookie.ResponseCookieBuilder getCookie(String key, String value){
        return ResponseCookie.from(key, value)
                .httpOnly(true)
                .maxAge(Duration.ofDays(maxAgeInDays))
                .path("/jukebox/");
    }
}
