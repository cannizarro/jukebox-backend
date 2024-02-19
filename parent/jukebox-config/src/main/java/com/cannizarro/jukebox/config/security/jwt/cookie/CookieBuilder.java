package com.cannizarro.jukebox.config.security.jwt.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieBuilder {

    @Value("${jwt.max_age_days}")
    private int maxAgeInDays;

    @Value("${server.domain}")
    private String domain;

    public ResponseCookie getCookie(String key, String value){
        return ResponseCookie.from(key, value)
                .httpOnly(true)
                .maxAge(Duration.ofDays(maxAgeInDays))
                .secure(true)
                .path("/jukebox/")
                .domain(domain)
                .sameSite(Cookie.SameSite.NONE.attributeValue())
                .build();
    }
}
