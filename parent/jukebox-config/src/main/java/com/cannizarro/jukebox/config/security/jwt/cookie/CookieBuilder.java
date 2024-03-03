package com.cannizarro.jukebox.config.security.jwt.cookie;

import org.springframework.http.ResponseCookie;

public interface CookieBuilder {
    ResponseCookie getCookie(String key, String value);
}
